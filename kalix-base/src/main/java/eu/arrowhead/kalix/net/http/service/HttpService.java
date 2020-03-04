package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.dto.Format;
import eu.arrowhead.kalix.dto.Readable;
import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.net.http.HttpStatus;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A concrete Arrowhead service, exposing its functions as HTTP endpoints.
 */
public class HttpService {
    private final String basePath;
    private final Format[] formats;
    private final List<HttpRouteSequence> routeSequences;

    private HttpService(final Builder builder) {
        basePath = Objects.requireNonNull(builder.basePath, "Expected basePath");
        if (!HttpPaths.isValidPathWithoutPercentEncodings(basePath)) {
            throw new IllegalArgumentException("basePath \"" + basePath +
                "\" must start with a forward slash (/) and then contain " +
                "only the following characters: A–Z a–z 0–9 " +
                "-._~!$%&'()*+,;/=:@");
        }
        if (basePath.length() > 1 && basePath.charAt(basePath.length() - 1) == '/') {
            throw new IllegalArgumentException("basePath may not end with a " +
                "forward slash (/) unless it is the root path \"/\"");
        }

        formats = Objects.requireNonNullElseGet(builder.formats, () -> new Format[]{Format.JSON});

        final var routeSequenceFactory = new HttpRouteSequenceFactory(builder.catchers, builder.validators);
        routeSequences = builder.routes.stream()
            .sorted()
            .map(routeSequenceFactory::createRouteSequenceFor)
            .collect(Collectors.toList());
    }

    /**
     * @return Base path that the paths of all requests targeted at this
     * service must begin with.
     */
    public String basePath() {
        return basePath;
    }

    /**
     * @return Data formats supported by this service.
     */
    public Format[] formats() {
        return formats.clone();
    }

    /**
     * Delegates handling of an {@link HttpServiceRequest} to this service.
     *
     * @param request  Incoming HTTP request.
     * @param response Modifiable HTTP response object, destined to be sent
     *                 back to the original request sender.
     * @return Future completed with a {@code null} value when handling has
     * finished.
     */
    public Future<?> handle(final HttpServiceRequest request, final HttpServiceResponse response) {
        return Future
            .flatReducePlain(routeSequences, false, (isHandled, routeSequence) -> {
                if (isHandled) {
                    return Future.success(true);
                }
                return routeSequence.tryHandle(request, response);
            })
            .flatMap(isHandled -> {
                if (!isHandled) {
                    response
                        .status(HttpStatus.NOT_FOUND)
                        .headers(new HttpHeaders())
                        .body(new byte[0]);
                }
                return null;
            });
    }

    /**
     * Builder useful for constructing {@link HttpService} instances.
     */
    public static class Builder {
        private final ArrayList<HttpCatcher<?>> catchers = new ArrayList<>(0);
        private final ArrayList<HttpRoute> routes = new ArrayList<>(0);
        private final ArrayList<HttpValidator> validators = new ArrayList<>(0);

        private String basePath;
        private Format[] formats;

        private int catcherOrdinal = 0;
        private int filterOrdinal = 0;

        /**
         * Sets base path that must be matched by HTTP requests received by the
         * created {@link HttpService}. <b>Must be specified.</b>
         * <p>
         * The base path must start with a forward slash ({@code /}), must not
         * end with a forward slash, and may only contain the following
         * characters:
         * <pre>
         *     A–Z a–z 0–9 - . _ ~ ! $ & ' ( ) * + , ; / = : @
         * </pre>
         * Percent encodings may not be used. If the given path is the root
         * path, which consists only of a forward slash, the requirement of
         * there being no trailing slash is ignored.
         * <p>
         * Note that while RFC 3986 explicitly permits all the above listed
         * characters, it is recommended to stick to the lowercase alphabetic
         * characters, the numeric characters and dash ({@code a-z 0-9 -}) to
         * maximize compatibility with existing tools and solutions.
         *
         * @param basePath Target base path.
         * @return This builder.
         * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.3">RFC 3986, Section 3.3</a>
         */
        public Builder basePath(final String basePath) {
            this.basePath = basePath;
            return this;
        }

        /**
         * Declares what data encodings this service can read and write.
         * <i>JSON is declared by default if this method is never called.</i>
         * <p>
         * While the {@code HttpArrowheadSystem} that will own this service
         * will prevent messages claimed to be encoded with other formats from
         * being received, stating that an encoding can be read and written
         * does not itself guarantee it. It is up to the {@link HttpService}
         * creator to ensure that such capabilities are indeed available. For
         * most intents and purposes, the most adequate way of achieving this
         * is by using Data Transfer Objects (DTOs), more of which you can read
         * about in the package documentation for the
         * {@code eu.arrowhead.kalix.dto} package.
         *
         * @param formats Encoding formats declared to be supported.
         * @return This builder.
         * @see eu.arrowhead.kalix.net.http.HttpArrowheadSystem HttpArrowheadSystem
         * @see eu.arrowhead.kalix.dto
         */
        public Builder formats(final Format... formats) {
            this.formats = formats;
            return this;
        }

        /**
         * Adds an exception catcher to the created {@link HttpService}.
         * <p>
         * An exception catcher is invoked whenever an exception occurs in a
         * validator, route handler, or other catcher, while handling an
         * incoming HTTP request that matches its method, path pattern and
         * exception class.
         * <p>
         * If multiple catchers are created with matching methods, path
         * patterns or exception class, the one with the smallest ordinal will
         * be executed first. Execution continues, in order, until each catcher
         * has been given the opportunity to handle the exception.
         *
         * @param catcher Exception catcher to add.
         * @return This builder.
         */
        public Builder catcher(final HttpCatcher<?> catcher) {
            catchers.add(catcher);
            return this;
        }

        /**
         * Adds an exception catcher to the created {@link HttpService}.
         * <p>
         * An exception catcher is invoked whenever an exception occurs in a
         * validator, route handler, or other catcher, while handling an
         * incoming HTTP request that matches its method, path pattern and
         * exception class.
         *
         * @param method         Request method to match.
         * @param pattern        Request path pattern to match.
         * @param exceptionClass Exception class to be assignable to.
         * @param handler        Handler to invoke with matching exceptions.
         * @return This builder.
         * @see #catcher(HttpCatcher)
         */
        public <T extends Throwable> Builder catcher(
            final HttpMethod method,
            final String pattern,
            final Class<T> exceptionClass,
            final HttpCatcherHandler<T> handler)
        {
            return catcher(new HttpCatcher<>(
                catcherOrdinal++,
                method,
                pattern != null
                    ? HttpPattern.valueOf(pattern)
                    : null,
                exceptionClass,
                handler));
        }

        /**
         * Adds an exception catcher to the created {@link HttpService}.
         * <p>
         * An exception catcher is invoked whenever an exception occurs in a
         * validator, route handler, or other catcher, while handling an
         * incoming HTTP request that matches its method, path pattern and
         * exception class.
         * <p>
         * <i>This method creates a catcher that matches any exception.</i>
         *
         * @param method  Request method to match.
         * @param pattern Request path pattern to match.
         * @param handler Handler to invoke with matching exceptions.
         * @return This builder.
         * @see #catcher(HttpCatcher)
         */
        public Builder catcher(final HttpMethod method, final String pattern, final HttpCatcherHandler<?> handler) {
            return catcher(method, pattern, null, handler);
        }

        /**
         * Adds an exception catcher to the created {@link HttpService}.
         * <p>
         * An exception catcher is invoked whenever an exception occurs in a
         * validator, route handler, or other catcher, while handling an
         * incoming HTTP request that matches its method, path pattern and
         * exception class.
         * <p>
         * <i>This method creates a catcher that matches any method or
         * exception.</i>
         *
         * @param pattern Request path pattern to match.
         * @param handler Handler to invoke with matching exceptions.
         * @return This builder.
         * @see #catcher(HttpCatcher)
         */
        public Builder catcher(final String pattern, final HttpCatcherHandler<?> handler) {
            return catcher(null, pattern, null, handler);
        }

        /**
         * Adds an exception catcher to the created {@link HttpService}.
         * <p>
         * An exception catcher is invoked whenever an exception occurs in a
         * validator, route handler, or other catcher, while handling an
         * incoming HTTP request that matches its method, path pattern and
         * exception class.
         * <p>
         * <i>This method creates a catcher that matches any method.</i>
         *
         * @param pattern        Request path pattern to match.
         * @param exceptionClass Exception class to be assignable to.
         * @param handler        Handler to invoke with matching exceptions.
         * @return This builder.
         * @see #catcher(HttpCatcher)
         */
        public <T extends Throwable> Builder catcher(
            final String pattern,
            final Class<T> exceptionClass,
            final HttpCatcherHandler<T> handler)
        {
            return catcher(null, pattern, exceptionClass, handler);
        }

        /**
         * Adds an exception catcher to the created {@link HttpService}.
         * <p>
         * An exception catcher is invoked whenever an exception occurs in a
         * validator, route handler, or other catcher, while handling an
         * incoming HTTP request that matches its method, path pattern and
         * exception class.
         * <p>
         * <i>This method creates a catcher that matches any path or
         * exception.</i>
         *
         * @param method  Request method to match.
         * @param handler Handler to invoke with matching exceptions.
         * @return This builder.
         * @see #catcher(HttpCatcher)
         */
        public Builder catcher(final HttpMethod method, final HttpCatcherHandler<?> handler) {
            return catcher(method, null, null, handler);
        }

        /**
         * Adds an exception catcher to the created {@link HttpService}.
         * <p>
         * An exception catcher is invoked whenever an exception occurs in a
         * validator, route handler, or other catcher, while handling an
         * incoming HTTP request that matches its method, path pattern and
         * exception class.
         * <p>
         * <i>This method creates a catcher that matches any path.</i>
         *
         * @param method         Request method to match.
         * @param exceptionClass Exception class to be assignable to.
         * @param handler        Handler to invoke with matching exceptions.
         * @return This builder.
         * @see #catcher(HttpCatcher)
         */
        public <T extends Throwable> Builder catcher(
            final HttpMethod method,
            final Class<T> exceptionClass,
            final HttpCatcherHandler<T> handler)
        {
            return catcher(method, null, exceptionClass, handler);
        }

        /**
         * Adds an exception catcher to the created {@link HttpService}.
         * <p>
         * An exception catcher is invoked whenever an exception occurs in a
         * validator, route handler, or other catcher, while handling an
         * incoming HTTP request that matches its method, path pattern and
         * exception class.
         * <p>
         * <i>This method creates a catcher that matches any method or
         * path.</i>
         *
         * @param exceptionClass Exception class to be assignable to.
         * @param handler        Handler to invoke with matching exceptions.
         * @return This builder.
         * @see #catcher(HttpCatcher)
         */
        public <T extends Throwable> Builder catcher(
            final Class<T> exceptionClass,
            final HttpCatcherHandler<T> handler)
        {
            return catcher(null, null, exceptionClass, handler);
        }

        /**
         * Adds an exception catcher to the created {@link HttpService}.
         * <p>
         * An exception catcher is invoked whenever an exception occurs in a
         * validator, route handler, or other catcher, while handling an
         * incoming HTTP request that matches its method, path pattern and
         * exception class.
         * <p>
         * <i>This method creates a catcher that matches any method, path or
         * exception.</i>
         *
         * @param handler Handler to invoke with matching exceptions.
         * @return This builder.
         * @see #catcher(HttpCatcher)
         */
        public Builder catcher(final HttpCatcherHandler<?> handler) {
            return catcher(null, null, null, handler);
        }

        public Builder validator(final HttpValidator validator) {
            validators.add(validator);
            return this;
        }

        public Builder validator(final HttpMethod method, final String pattern, final HttpValidatorHandler handler) {
            return validator(new HttpValidator(filterOrdinal++, method, pattern != null
                ? HttpPattern.valueOf(pattern)
                : null, handler));
        }

        public Builder validator(final String pattern, final HttpValidatorHandler handler) {
            return validator(null, pattern, handler);
        }

        public Builder validator(final HttpMethod method, final HttpValidatorHandler handler) {
            return validator(method, null, handler);
        }

        public Builder validator(final HttpValidatorHandler handler) {
            return validator(null, null, handler);
        }

        public Builder route(final HttpRoute route) {
            routes.add(route);
            return this;
        }

        public Builder route(final HttpMethod method, final String pattern, final HttpRouteHandler handler) {
            return route(new HttpRoute(method, pattern != null ? HttpPattern.valueOf(pattern) : null, handler));
        }

        public Builder route(final String pattern, final HttpRouteHandler handler) {
            return route(null, pattern, handler);
        }

        public Builder route(final HttpMethod method, final HttpRouteHandler handler) {
            return route(method, null, handler);
        }

        public Builder route(final HttpRouteHandler handler) {
            return route(null, null, handler);
        }

        public Builder get(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.GET, path, handler);
        }

        public Builder post(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.POST, path, handler);
        }

        public Builder put(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.PUT, path, handler);
        }

        public Builder delete(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.DELETE, path, handler);
        }

        public Builder head(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.HEAD, path, handler);
        }

        public Builder options(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.OPTIONS, path, handler);
        }

        public Builder connect(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.CONNECT, path, handler);
        }

        public Builder patch(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.PATCH, path, handler);
        }

        public Builder trace(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.TRACE, path, handler);
        }

        public HttpService build() {
            return new HttpService(this);
        }
    }
}
