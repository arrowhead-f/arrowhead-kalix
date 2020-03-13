package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.ArrowheadService;
import eu.arrowhead.kalix.ArrowheadServiceBuilder;
import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.descriptor.InterfaceDescriptor;
import eu.arrowhead.kalix.descriptor.SecurityDescriptor;
import eu.arrowhead.kalix.descriptor.TransportDescriptor;
import eu.arrowhead.kalix.internal.net.http.service.HttpPaths;
import eu.arrowhead.kalix.internal.net.http.service.HttpRoutables;
import eu.arrowhead.kalix.internal.net.http.service.HttpRouteSequence;
import eu.arrowhead.kalix.internal.net.http.service.HttpRouteSequenceFactory;
import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.net.http.HttpStatus;
import eu.arrowhead.kalix.util.concurrent.Future;
import eu.arrowhead.kalix.util.concurrent.Futures;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A concrete Arrowhead service, exposing its functions as HTTP endpoints.
 * <p>
 * TODO: Write more extensive documentation, including about validators, routes
 * and catchers, as well as about patterns and matching.
 */
public class HttpArrowheadService implements ArrowheadService {
    private final String name;
    private final String basePath;
    private final List<EncodingDescriptor> encodings;
    private final List<HttpRouteSequence> routeSequences;

    private List<InterfaceDescriptor> cachedSupportedInterfaces = null;

    private HttpArrowheadService(final Builder builder) {
        name = Objects.requireNonNull(builder.name, "Expected name");
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

        encodings = Collections.unmodifiableList(Objects.requireNonNull(builder.encodings, "Expected encodings"));
        if (encodings.size() == 0) {
            throw new IllegalArgumentException("Expected encodings.size() > 0");
        }

        final var routeSequenceFactory = new HttpRouteSequenceFactory(builder.catchers, builder.validators);
        routeSequences = builder.routes.stream()
            .sorted(HttpRoutables::compare)
            .map(routeSequenceFactory::createRouteSequenceFor)
            .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String qualifier() {
        return basePath;
    }

    @Override
    public SecurityDescriptor security() {
        return null;
    }

    @Override
    public Map<String, String> metadata() {
        return null;
    }

    @Override
    public int version() {
        return 0;
    }

    @Override
    public List<InterfaceDescriptor> supportedInterfaces() {
        if (cachedSupportedInterfaces == null) {
            final var isSecure = security() != SecurityDescriptor.NOT_SECURE;
            cachedSupportedInterfaces = encodings.stream()
                .map(encoding -> InterfaceDescriptor.getOrCreate(TransportDescriptor.HTTP, isSecure, encoding))
                .collect(Collectors.toUnmodifiableList());
        }
        return cachedSupportedInterfaces;
    }

    /**
     * @return Base path that the paths of all requests targeted at this
     * service.
     */
    public String basePath() {
        return basePath;
    }

    /**
     * @return The encoding to use by default.
     */
    public EncodingDescriptor defaultEncoding() {
        return encodings.get(0);
    }

    /**
     * @return Data encodings supported by this service.
     */
    public List<EncodingDescriptor> encodings() {
        return encodings;
    }

    /**
     * Delegates handling of an {@link HttpServiceRequest} to this service.
     *
     * @param request  Incoming HTTP request.
     * @param response Modifiable HTTP response object, destined to be sent
     *                 back to the original request sender.
     * @return Future completed with {@code null} value when handling has
     * finished.
     */
    public Future<?> handle(final HttpServiceRequest request, final HttpServiceResponse response) {
        final var task = new HttpRouteTask.Builder()
            .basePath(basePath)
            .request(request)
            .response(response)
            .build();

        return Futures.flatReducePlain(routeSequences, false,
            (isHandled, routeSequence) -> {
                if (isHandled) {
                    return Future.success(true);
                }
                return routeSequence.tryHandle(task);
            })
            .map(isHandled -> {
                if (!isHandled) {
                    response
                        .status(HttpStatus.NOT_FOUND)
                        .clearHeaders()
                        .clearBody();
                }
                return null;
            });
    }

    /**
     * Builder useful for constructing {@link HttpArrowheadService} instances.
     */
    public static class Builder implements ArrowheadServiceBuilder {
        private final ArrayList<HttpCatcher<?>> catchers = new ArrayList<>(0);
        private final ArrayList<HttpRoute> routes = new ArrayList<>(0);
        private final ArrayList<HttpValidator> validators = new ArrayList<>(0);

        private String name;
        private String basePath;
        private List<EncodingDescriptor> encodings;

        private int catcherOrdinal = 0;
        private int filterOrdinal = 0;

        /**
         * Sets service name. <b>Must be specified.</b>
         * <p>
         * Service names are also referred to as <i>service definitions</i>.
         *
         * @param name Desired service name.
         * @return This builder.
         */
        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets base path that must be matched by HTTP requests received by the
         * created {@link HttpArrowheadService}. <b>Must be specified.</b>
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
         * <b>Must be specified.</b>
         * <p>
         * While the created {@link HttpArrowheadService} will prevent messages claimed
         * to be encoded with other encodings from being received, stating that
         * an encoding can be read and written does not itself guarantee it.
         * It is up to the {@link HttpArrowheadService} creator to ensure that such
         * capabilities are indeed available. For most intents and purposes,
         * the most adequate way of achieving this is by using Data Transfer
         * Objects (DTOs), more of which you can read about in the package
         * documentation for the {@code eu.arrowhead.kalix.dto} package.
         *
         * @param encodings Encodings declared to be supported. At least one
         *                  must be provided. The first specified encoding is
         *                  used by default when received requests do not
         *                  include enough details about their bodies.
         * @return This builder.
         * @see eu.arrowhead.kalix.dto
         */
        public Builder encodings(final EncodingDescriptor... encodings) {
            this.encodings = Arrays.asList(encodings.clone());
            return this;
        }

        /**
         * Adds an exception catcher to the created {@link HttpArrowheadService}.
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
         * Adds an exception catcher to the created {@link HttpArrowheadService}.
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
         * Adds an exception catcher to the created {@link HttpArrowheadService}.
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
        public Builder catcher(
            final HttpMethod method,
            final String pattern,
            final HttpCatcherHandler<Throwable> handler)
        {
            return catcher(method, pattern, Throwable.class, handler);
        }

        /**
         * Adds an exception catcher to the created {@link HttpArrowheadService}.
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
        public Builder catcher(final String pattern, final HttpCatcherHandler<Throwable> handler) {
            return catcher(null, pattern, Throwable.class, handler);
        }

        /**
         * Adds an exception catcher to the created {@link HttpArrowheadService}.
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
         * Adds an exception catcher to the created {@link HttpArrowheadService}.
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
        public Builder catcher(final HttpMethod method, final HttpCatcherHandler<Throwable> handler) {
            return catcher(method, null, Throwable.class, handler);
        }

        /**
         * Adds an exception catcher to the created {@link HttpArrowheadService}.
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
         * Adds an exception catcher to the created {@link HttpArrowheadService}.
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
         * Adds an exception catcher to the created {@link HttpArrowheadService}.
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
        public Builder catcher(final HttpCatcherHandler<Throwable> handler) {
            return catcher(null, null, Throwable.class, handler);
        }

        /**
         * Adds incoming HTTP request validator to the created
         * {@link HttpArrowheadService}.
         * <p>
         * Validators are executed with incoming HTTP requests before they
         * end up at their designated routes. Each validator matching the
         * method and path of the request is given the opportunity read the
         * request, modify its headers and, potentially, respond to the
         * request. If a response is generated, no more handler are invoked
         * with the request, unless sending the response fails and a catcher is
         * executed.
         * <p>
         * If multiple validators are created with matching methods or path
         * patterns, the one with the smallest ordinal will be executed first.
         * If no validator responds to the request, all matching validators are
         * executed before the request is provided to its designated route.
         *
         * @param validator HTTP request validator to add.
         * @return This builder.
         */
        public Builder validator(final HttpValidator validator) {
            validators.add(validator);
            return this;
        }

        /**
         * Adds incoming HTTP request validator to the created
         * {@link HttpArrowheadService}.
         * <p>
         * Validators are executed with incoming HTTP requests before they
         * end up at their designated routes. Each validator matching the
         * method and path of the request is given the opportunity read the
         * request, modify its headers and, potentially, respond to the
         * request. If a response is generated, no more handler are invoked
         * with the request, unless sending the response fails and a catcher is
         * executed.
         *
         * @param method  Request method to match.
         * @param pattern Request path pattern to match.
         * @param handler Handler to invoke with matching requests.
         * @return This builder.
         * @see #validator(HttpValidator)
         */
        public Builder validator(final HttpMethod method, final String pattern, final HttpValidatorHandler handler) {
            return validator(new HttpValidator(filterOrdinal++, method, pattern != null
                ? HttpPattern.valueOf(pattern)
                : null, handler));
        }

        /**
         * Adds incoming HTTP request validator to the created
         * {@link HttpArrowheadService}.
         * <p>
         * Validators are executed with incoming HTTP requests before they
         * end up at their designated routes. Each validator matching the
         * method and path of the request is given the opportunity read the
         * request, modify its headers and, potentially, respond to the
         * request. If a response is generated, no more handler are invoked
         * with the request, unless sending the response fails and a catcher is
         * executed.
         * <p>
         * <i>This method creates a validator that matches any method.</i>
         *
         * @param pattern Request path pattern to match.
         * @param handler Handler to invoke with matching requests.
         * @return This builder.
         * @see #validator(HttpValidator)
         */
        public Builder validator(final String pattern, final HttpValidatorHandler handler) {
            return validator(null, pattern, handler);
        }

        /**
         * Adds incoming HTTP request validator to the created
         * {@link HttpArrowheadService}.
         * <p>
         * Validators are executed with incoming HTTP requests before they
         * end up at their designated routes. Each validator matching the
         * method and path of the request is given the opportunity read the
         * request, modify its headers and, potentially, respond to the
         * request. If a response is generated, no more handler are invoked
         * with the request, unless sending the response fails and a catcher is
         * executed.
         * <p>
         * <i>This method creates a validator that matches any path.</i>
         *
         * @param method  Request method to match.
         * @param handler Handler to invoke with matching requests.
         * @return This builder.
         * @see #validator(HttpValidator)
         */
        public Builder validator(final HttpMethod method, final HttpValidatorHandler handler) {
            return validator(method, null, handler);
        }

        /**
         * Adds incoming HTTP request validator to the created
         * {@link HttpArrowheadService}.
         * <p>
         * Validators are executed with incoming HTTP requests before they
         * end up at their designated routes. Each validator matching the
         * method and path of the request is given the opportunity read the
         * request, modify its headers and, potentially, respond to the
         * request. If a response is generated, no more handler are invoked
         * with the request, unless sending the response fails and a catcher is
         * executed.
         * <p>
         * <i>This method creates a validator that matches any method or
         * path.</i>
         *
         * @param handler Handler to invoke with matching requests.
         * @return This builder.
         * @see #validator(HttpValidator)
         */
        public Builder validator(final HttpValidatorHandler handler) {
            return validator(null, null, handler);
        }

        /**
         * Adds incoming HTTP request route to the created {@link HttpArrowheadService}.
         * <p>
         * A route is a primary handler for a particular set of HTTP requests.
         * If an incoming HTTP request matches its method and path pattern, it
         * is executed with the expectation that it will specify an HTTP
         * response. However, if a {@link HttpValidator validator} executed
         * before the route responds to the request or throws an exception, it
         * is never invoked. In the case of either a validator or a route
         * throwing an exception, any registered {@link HttpCatcher catcher}
         * matching the request and exception will be given a chance to handle
         * the exception.
         *
         * @param route HTTP route to add.
         * @return This builder.
         * @see #get(String, HttpRouteHandler) GET
         * @see #post(String, HttpRouteHandler) POST
         * @see #put(String, HttpRouteHandler) PUT
         * @see #delete(String, HttpRouteHandler) DELETE
         * @see #head(String, HttpRouteHandler) HEAD
         * @see #options(String, HttpRouteHandler) OPTIONS
         * @see #connect(String, HttpRouteHandler) CONNECT
         * @see #patch(String, HttpRouteHandler) PATCH
         * @see #trace(String, HttpRouteHandler) TRACE
         */
        public Builder route(final HttpRoute route) {
            routes.add(route);
            return this;
        }

        /**
         * Adds incoming HTTP request route to the created {@link HttpArrowheadService}.
         * <p>
         * A route is a primary handler for a particular set of HTTP requests.
         * If an incoming HTTP request matches its method and path pattern, it
         * is executed with the expectation that it will specify an HTTP
         * response. However, if a {@link HttpValidator validator} executed
         * before the route responds to the request or throws an exception, it
         * is never invoked. In the case of either a validator or a route
         * throwing an exception, any registered {@link HttpCatcher catcher}
         * matching the request and exception will be given a chance to handle
         * the exception.
         *
         * @param method  Request method to match.
         * @param pattern Request path pattern to match.
         * @param handler Handler to invoke with matching requests.
         * @return This builder.
         * @see #get(String, HttpRouteHandler) GET
         * @see #post(String, HttpRouteHandler) POST
         * @see #put(String, HttpRouteHandler) PUT
         * @see #delete(String, HttpRouteHandler) DELETE
         * @see #head(String, HttpRouteHandler) HEAD
         * @see #options(String, HttpRouteHandler) OPTIONS
         * @see #connect(String, HttpRouteHandler) CONNECT
         * @see #patch(String, HttpRouteHandler) PATCH
         * @see #trace(String, HttpRouteHandler) TRACE
         */
        public Builder route(final HttpMethod method, final String pattern, final HttpRouteHandler handler) {
            return route(new HttpRoute(method, pattern != null ? HttpPattern.valueOf(pattern) : null, handler));
        }

        /**
         * Adds incoming HTTP request route to the created {@link HttpArrowheadService}.
         * <p>
         * A route is a primary handler for a particular set of HTTP requests.
         * If an incoming HTTP request matches its method and path pattern, it
         * is executed with the expectation that it will specify an HTTP
         * response. However, if a {@link HttpValidator validator} executed
         * before the route responds to the request or throws an exception, it
         * is never invoked. In the case of either a validator or a route
         * throwing an exception, any registered {@link HttpCatcher catcher}
         * matching the request and exception will be given a chance to handle
         * the exception.
         * <p>
         * <i>This method creates a route that matches any method.</i>
         *
         * @param pattern Request path pattern to match.
         * @param handler Handler to invoke with matching requests.
         * @return This builder.
         * @see #get(String, HttpRouteHandler) GET
         * @see #post(String, HttpRouteHandler) POST
         * @see #put(String, HttpRouteHandler) PUT
         * @see #delete(String, HttpRouteHandler) DELETE
         * @see #head(String, HttpRouteHandler) HEAD
         * @see #options(String, HttpRouteHandler) OPTIONS
         * @see #connect(String, HttpRouteHandler) CONNECT
         * @see #patch(String, HttpRouteHandler) PATCH
         * @see #trace(String, HttpRouteHandler) TRACE
         */
        public Builder route(final String pattern, final HttpRouteHandler handler) {
            return route(null, pattern, handler);
        }

        /**
         * Adds incoming HTTP request route to the created {@link HttpArrowheadService}.
         * <p>
         * A route is a primary handler for a particular set of HTTP requests.
         * If an incoming HTTP request matches its method and path pattern, it
         * is executed with the expectation that it will specify an HTTP
         * response. However, if a {@link HttpValidator validator} executed
         * before the route responds to the request or throws an exception, it
         * is never invoked. In the case of either a validator or a route
         * throwing an exception, any registered {@link HttpCatcher catcher}
         * matching the request and exception will be given a chance to handle
         * the exception.
         * <p>
         * <i>This method creates a route that matches any path.</i>
         *
         * @param method  Request method to match.
         * @param handler Handler to invoke with matching requests.
         * @return This builder.
         * @see #get(String, HttpRouteHandler) GET
         * @see #post(String, HttpRouteHandler) POST
         * @see #put(String, HttpRouteHandler) PUT
         * @see #delete(String, HttpRouteHandler) DELETE
         * @see #head(String, HttpRouteHandler) HEAD
         * @see #options(String, HttpRouteHandler) OPTIONS
         * @see #connect(String, HttpRouteHandler) CONNECT
         * @see #patch(String, HttpRouteHandler) PATCH
         * @see #trace(String, HttpRouteHandler) TRACE
         */
        public Builder route(final HttpMethod method, final HttpRouteHandler handler) {
            return route(method, null, handler);
        }

        /**
         * Adds incoming HTTP request route to the created {@link HttpArrowheadService}.
         * <p>
         * A route is a primary handler for a particular set of HTTP requests.
         * If an incoming HTTP request matches its method and path pattern, it
         * is executed with the expectation that it will specify an HTTP
         * response. However, if a {@link HttpValidator validator} executed
         * before the route responds to the request or throws an exception, it
         * is never invoked. In the case of either a validator or a route
         * throwing an exception, any registered {@link HttpCatcher catcher}
         * matching the request and exception will be given a chance to handle
         * the exception.
         * <p>
         * <i>This method creates a route that matches any method or path.</i>
         *
         * @param handler Handler to invoke with matching requests.
         * @return This builder.
         * @see #get(String, HttpRouteHandler) GET
         * @see #post(String, HttpRouteHandler) POST
         * @see #put(String, HttpRouteHandler) PUT
         * @see #delete(String, HttpRouteHandler) DELETE
         * @see #head(String, HttpRouteHandler) HEAD
         * @see #options(String, HttpRouteHandler) OPTIONS
         * @see #connect(String, HttpRouteHandler) CONNECT
         * @see #patch(String, HttpRouteHandler) PATCH
         * @see #trace(String, HttpRouteHandler) TRACE
         */
        public Builder route(final HttpRouteHandler handler) {
            return route(null, null, handler);
        }

        /**
         * Adds incoming HTTP request route to the created {@link HttpArrowheadService},
         * handling GET requests matching given pattern.
         *
         * @param handler Handler to invoke with matching requests.
         * @return This builder.
         * @see #route(HttpRoute)
         * @see <a href="https://tools.ietf.org/html/rfc7231#section-4.3.1">RFC 7231, Section 4.3.1</a>
         */
        public Builder get(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.GET, path, handler);
        }

        /**
         * Adds incoming HTTP request route to the created {@link HttpArrowheadService},
         * handling POST requests matching given pattern.
         *
         * @param handler Handler to invoke with matching requests.
         * @return This builder.
         * @see #route(HttpRoute)
         * @see <a href="https://tools.ietf.org/html/rfc7231#section-4.3.3">RFC 7231, Section 4.3.3</a>
         */
        public Builder post(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.POST, path, handler);
        }

        /**
         * Adds incoming HTTP request route to the created {@link HttpArrowheadService},
         * handling PUT requests matching given pattern.
         *
         * @param handler Handler to invoke with matching requests.
         * @return This builder.
         * @see #route(HttpRoute)
         * @see <a href="https://tools.ietf.org/html/rfc7231#section-4.3.4">RFC 7231, Section 4.3.4</a>
         */
        public Builder put(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.PUT, path, handler);
        }

        /**
         * Adds incoming HTTP request route to the created {@link HttpArrowheadService},
         * handling DELETE requests matching given pattern.
         *
         * @param handler Handler to invoke with matching requests.
         * @return This builder.
         * @see #route(HttpRoute)
         * @see <a href="https://tools.ietf.org/html/rfc7231#section-4.3.5">RFC 7231, Section 4.3.5</a>
         */
        public Builder delete(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.DELETE, path, handler);
        }

        /**
         * Adds incoming HTTP request route to the created {@link HttpArrowheadService},
         * handling HEAD requests matching given pattern.
         *
         * @param handler Handler to invoke with matching requests.
         * @return This builder.
         * @see #route(HttpRoute)
         * @see <a href="https://tools.ietf.org/html/rfc7231#section-4.3.2">RFC 7231, Section 4.3.2</a>
         */
        public Builder head(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.HEAD, path, handler);
        }

        /**
         * Adds incoming HTTP request route to the created {@link HttpArrowheadService},
         * handling OPTIONS requests matching given pattern.
         *
         * @param handler Handler to invoke with matching requests.
         * @return This builder.
         * @see #route(HttpRoute)
         * @see <a href="https://tools.ietf.org/html/rfc7231#section-4.3.7">RFC 7231, Section 4.3.7</a>
         */
        public Builder options(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.OPTIONS, path, handler);
        }

        /**
         * Adds incoming HTTP request route to the created {@link HttpArrowheadService},
         * handling CONNECT requests matching given pattern.
         *
         * @param handler Handler to invoke with matching requests.
         * @return This builder.
         * @see #route(HttpRoute)
         * @see <a href="https://tools.ietf.org/html/rfc7231#section-4.3.6">RFC 7231, Section 4.3.6</a>
         */
        public Builder connect(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.CONNECT, path, handler);
        }

        /**
         * Adds incoming HTTP request route to the created {@link HttpArrowheadService},
         * handling PATCH requests matching given pattern.
         *
         * @param handler Handler to invoke with matching requests.
         * @return This builder.
         * @see #route(HttpRoute)
         * @see <a href="https://tools.ietf.org/html/rfc5789">RFC 5789</a>
         */
        public Builder patch(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.PATCH, path, handler);
        }

        /**
         * Adds incoming HTTP request route to the created {@link HttpArrowheadService},
         * handling TRACE requests matching given pattern.
         *
         * @param handler Handler to invoke with matching requests.
         * @return This builder.
         * @see #route(HttpRoute)
         * @see <a href="https://tools.ietf.org/html/rfc7231#section-4.3.8">RFC 7231, Section 4.3.8</a>
         */
        public Builder trace(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.TRACE, path, handler);
        }

        @Override
        public HttpArrowheadService build() {
            return new HttpArrowheadService(this);
        }
    }
}
