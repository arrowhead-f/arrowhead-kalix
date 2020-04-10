package se.arkalix.net.http.service;

import se.arkalix.ArService;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.descriptor.TransportDescriptor;
import se.arkalix.internal.ArServerRegistry;
import se.arkalix.internal.net.http.service.HttpServer;
import se.arkalix.net.http.HttpMethod;
import se.arkalix.security.access.AccessPolicy;

import java.util.*;

import static se.arkalix.descriptor.TransportDescriptor.HTTP;

/**
 * A concrete Arrowhead service, exposing its functions as HTTP endpoints.
 * <p>
 * Please refer to the {@link se.arkalix.net.http.service package
 * documentation} for more details about how this class can be used.
 */
@SuppressWarnings("unused")
public final class HttpService implements ArService {
    static {
        ArServerRegistry.set(HttpService.class, HttpServer::create);
    }

    private final ArrayList<HttpCatcher<?>> catchers = new ArrayList<>(0);
    private final ArrayList<HttpRoute> routes = new ArrayList<>(0);
    private final ArrayList<HttpFilter> filters = new ArrayList<>(0);

    private String name;
    private String basePath;
    private List<EncodingDescriptor> encodings;
    private AccessPolicy accessPolicy;
    private Map<String, String> metadata;
    private int version = 0;

    private int catcherOrdinal = 0;
    private int filterOrdinal = 0;

    /**
     * Sets name of this service. <b>Must be specified.</b>
     * <p>
     * Service names are also referred to as <i>service definitions</i>.
     * <p>
     * While any characters may be used in the name, it is recommended to stick
     * to the lowercase alphabetic characters, the numeric characters and dash
     * of the ASCII character set ({@code a-z 0-9 -}) to maximize compatibility
     * with existing tools and solutions.
     *
     * @param name Desired service name.
     * @return This service.
     */
    public HttpService name(final String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets service URI, in this case a <i>base path</i>, that must be matched
     * by HTTP requests received by this service. <b>Must be specified.</b>
     * <p>
     * The base path must start with a forward slash ({@code /}), must not end
     * with a forward slash, and may only contain the following characters:
     * <pre>
     *     A–Z a–z 0–9 - . _ ~ ! $ &amp; ' ( ) * + , ; / = : @
     * </pre>
     * Percent encodings may not be used. If the given path is the root path,
     * which consists only of a forward slash, the requirement of there being
     * no trailing slash is ignored.
     * <p>
     * Note that while RFC 3986 explicitly permits all the above listed
     * characters, it is recommended to stick to the lowercase alphabetic
     * characters, the numeric characters and dash ({@code a-z 0-9 -}) to
     * maximize compatibility with existing tools and solutions.
     *
     * @param basePath Target base path.
     * @return This service.
     * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.3">RFC 3986, Section 3.3</a>
     */
    public HttpService basePath(final String basePath) {
        this.basePath = basePath;
        return this;
    }

    /**
     * Declares what data encodings this service can read and write. <b>Must be
     * specified.</b>
     * <p>
     * While this service will prevent messages claimed to be encoded with
     * other encodings from being received, stating that an encoding can be
     * read and written does not itself guarantee it. It is up to the service
     * creator to ensure that such capabilities are indeed available. For most
     * intents and purposes, the most adequate way of achieving this is by
     * using <i>Data Transfer Objects</i> (DTOs), more of which you can read
     * about in the package documentation for the
     * {@code se.arkalix.dto} package.
     *
     * @param encodings Encodings declared to be supported. At least one must
     *                  be provided. The first specified encoding is used by
     *                  default when received requests do not include enough
     *                  details about their bodies.
     * @return This service.
     * @see se.arkalix.dto Data Transfer Object Utilities
     */
    public HttpService encodings(final EncodingDescriptor... encodings) {
        this.encodings = Arrays.asList(encodings.clone());
        return this;
    }

    /**
     * Declares what access policy this service is to use. Unless the service
     * is provided by a system running in {@link se.arkalix.security insecure
     * mode}, an access policy <b>must be specified.</b>
     *
     * @param accessPolicy Desired access policy.
     * @return This service.
     * @see AccessPolicy#cloud() Same-cloud access
     * @see AccessPolicy#whitelist(String...) same-cloud/whitelist access
     * @see AccessPolicy#token() token access
     */
    public HttpService accessPolicy(final AccessPolicy accessPolicy) {
        this.accessPolicy = accessPolicy;
        return this;
    }

    /**
     * Sets metadata to associate with this service, replacing any previously
     * associated metadata. What significance the data have depend on the
     * service. The data could, for example, be distributed to a service
     * registry and be used to help prospective service consumers determine
     * whether this service is the one they should be using.
     *
     * @param metadata Map of metadata.
     * @return This service.
     */
    public HttpService metadata(final Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Saves key/value pair into the {@link #metadata(Map) metadata map}
     * associated with this service.
     *
     * @param key   Desired key.
     * @param value Desired value.
     * @return This service.
     */
    public HttpService metadata(final String key, final String value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
        return this;
    }

    /**
     * Sets service version.
     * <p>
     * If no version is explicitly set, zero will be used by default.
     *
     * @param version Service version.
     * @return This service.
     */
    public HttpService version(final int version) {
        this.version = version;
        return this;
    }

    /**
     * Adds incoming HTTP request route to this service, handling GET requests
     * matching given pattern.
     *
     * @param handler Handler to invoke with matching requests.
     * @return This service.
     * @see #route(HttpRoute)
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-4.3.1">RFC 7231, Section 4.3.1</a>
     */
    public HttpService get(final String path, final HttpRouteHandler handler) {
        return route(HttpMethod.GET, path, handler);
    }

    /**
     * Adds incoming HTTP request route to this service, handling POST requests
     * matching given pattern.
     *
     * @param handler Handler to invoke with matching requests.
     * @return This service.
     * @see #route(HttpRoute)
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-4.3.3">RFC 7231, Section 4.3.3</a>
     */
    public HttpService post(final String path, final HttpRouteHandler handler) {
        return route(HttpMethod.POST, path, handler);
    }

    /**
     * Adds incoming HTTP request route to this service, handling PUT requests
     * matching given pattern.
     *
     * @param handler Handler to invoke with matching requests.
     * @return This service.
     * @see #route(HttpRoute)
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-4.3.4">RFC 7231, Section 4.3.4</a>
     */
    public HttpService put(final String path, final HttpRouteHandler handler) {
        return route(HttpMethod.PUT, path, handler);
    }

    /**
     * Adds incoming HTTP request route to this service, handling DELETE
     * requests matching given pattern.
     *
     * @param handler Handler to invoke with matching requests.
     * @return This service.
     * @see #route(HttpRoute)
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-4.3.5">RFC 7231, Section 4.3.5</a>
     */
    public HttpService delete(final String path, final HttpRouteHandler handler) {
        return route(HttpMethod.DELETE, path, handler);
    }

    /**
     * Adds incoming HTTP request route to this service, handling HEAD requests
     * matching given pattern.
     *
     * @param handler Handler to invoke with matching requests.
     * @return This service.
     * @see #route(HttpRoute)
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-4.3.2">RFC 7231, Section 4.3.2</a>
     */
    public HttpService head(final String path, final HttpRouteHandler handler) {
        return route(HttpMethod.HEAD, path, handler);
    }

    /**
     * Adds incoming HTTP request route to this service, handling OPTIONS
     * requests matching given pattern.
     *
     * @param handler Handler to invoke with matching requests.
     * @return This service.
     * @see #route(HttpRoute)
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-4.3.7">RFC 7231, Section 4.3.7</a>
     */
    public HttpService options(final String path, final HttpRouteHandler handler) {
        return route(HttpMethod.OPTIONS, path, handler);
    }

    /**
     * Adds incoming HTTP request route to this service, handling CONNECT
     * requests matching given pattern.
     *
     * @param handler Handler to invoke with matching requests.
     * @return This service.
     * @see #route(HttpRoute)
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-4.3.6">RFC 7231, Section 4.3.6</a>
     */
    public HttpService connect(final String path, final HttpRouteHandler handler) {
        return route(HttpMethod.CONNECT, path, handler);
    }

    /**
     * Adds incoming HTTP request route to this service, handling PATCH
     * requests matching given pattern.
     *
     * @param handler Handler to invoke with matching requests.
     * @return This service.
     * @see #route(HttpRoute)
     * @see <a href="https://tools.ietf.org/html/rfc5789">RFC 5789</a>
     */
    public HttpService patch(final String path, final HttpRouteHandler handler) {
        return route(HttpMethod.PATCH, path, handler);
    }

    /**
     * Adds incoming HTTP request route to this service, handling TRACE
     * requests matching given pattern.
     *
     * @param handler Handler to invoke with matching requests.
     * @return This service.
     * @see #route(HttpRoute)
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-4.3.8">RFC 7231, Section 4.3.8</a>
     */
    public HttpService trace(final String path, final HttpRouteHandler handler) {
        return route(HttpMethod.TRACE, path, handler);
    }

    /**
     * Adds incoming HTTP request route to this service.
     * <p>
     * A route is a primary handler for a particular set of HTTP requests. If
     * an incoming HTTP request matches its method and path pattern, it is
     * executed with the expectation that it will specify an HTTP response.
     * However, if a {@link HttpFilter filter} executed before the route
     * responds to the request or throws an exception, it is never invoked. In
     * the case of either a filter or a route throwing an exception, any
     * registered {@link HttpCatcher catcher} matching the request and
     * exception will be given a chance to handle the exception.
     *
     * @param route HTTP route to add.
     * @return This service.
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
    public HttpService route(final HttpRoute route) {
        routes.add(route);
        return this;
    }

    /**
     * Adds incoming HTTP request route to this service.
     * <p>
     * A route is a primary handler for a particular set of HTTP requests. If
     * an incoming HTTP request matches its method and path pattern, it is
     * executed with the expectation that it will specify an HTTP response.
     * However, if a {@link HttpFilter filter} executed before the route
     * responds to the request or throws an exception, it is never invoked. In
     * the case of either a filter or a route throwing an exception, any
     * registered {@link HttpCatcher catcher} matching the request and
     * exception will be given a chance to handle the exception.
     *
     * @param method  Request method to match.
     * @param pattern Request path pattern to match.
     * @param handler Handler to invoke with matching requests.
     * @return This service.
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
    public HttpService route(final HttpMethod method, final String pattern, final HttpRouteHandler handler) {
        return route(new HttpRoute(method, pattern != null ? HttpPattern.valueOf(pattern) : null, handler));
    }

    /**
     * Adds incoming HTTP request route to this service.
     * <p>
     * A route is a primary handler for a particular set of HTTP requests. If
     * an incoming HTTP request matches its method and path pattern, it is
     * executed with the expectation that it will specify an HTTP response.
     * However, if a {@link HttpFilter filter} executed before the route
     * responds to the request or throws an exception, it is never invoked. In
     * the case of either a filter or a route throwing an exception, any
     * registered {@link HttpCatcher catcher} matching the request and
     * exception will be given a chance to handle the exception.
     * <p>
     * <i>This method creates a route that matches any method.</i>
     *
     * @param pattern Request path pattern to match.
     * @param handler Handler to invoke with matching requests.
     * @return This service.
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
    public HttpService route(final String pattern, final HttpRouteHandler handler) {
        return route(null, pattern, handler);
    }

    /**
     * Adds incoming HTTP request route to this service.
     * <p>
     * A route is a primary handler for a particular set of HTTP requests. If
     * an incoming HTTP request matches its method and path pattern, it is
     * executed with the expectation that it will specify an HTTP response.
     * However, if a {@link HttpFilter filter} executed before the route
     * responds to the request or throws an exception, it is never invoked. In
     * the case of either a filter or a route throwing an exception, any
     * registered {@link HttpCatcher catcher} matching the request and
     * exception will be given a chance to handle the exception.
     * <p>
     * <i>This method creates a route that matches any path.</i>
     *
     * @param method  Request method to match.
     * @param handler Handler to invoke with matching requests.
     * @return This service.
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
    public HttpService route(final HttpMethod method, final HttpRouteHandler handler) {
        return route(method, null, handler);
    }

    /**
     * Adds incoming HTTP request route to this service.
     * <p>
     * A route is a primary handler for a particular set of HTTP requests. If
     * an incoming HTTP request matches its method and path pattern, it is
     * executed with the expectation that it will specify an HTTP response.
     * However, if a {@link HttpFilter filter} executed before the route
     * responds to the request or throws an exception, it is never invoked. In
     * the case of either a filter or a route throwing an exception, any
     * registered {@link HttpCatcher catcher} matching the request and
     * exception will be given a chance to handle the exception.
     * <p>
     * <i>This method creates a route that matches any method or path.</i>
     *
     * @param handler Handler to invoke with matching requests.
     * @return This service.
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
    public HttpService route(final HttpRouteHandler handler) {
        return route(null, null, handler);
    }

    /**
     * Adds an exception catcher to this service.
     * <p>
     * An exception catcher is invoked whenever an exception occurs in a
     * filter, route handler, or other catcher, while handling an incoming
     * HTTP request that matches its method, path pattern and exception class.
     * <p>
     * If multiple catchers are created with matching methods, path patterns
     * and exception class, the one with the smallest ordinal will be executed
     * first. Execution continues, in order, until each catcher has been given
     * the opportunity to handle the exception.
     *
     * @param catcher Exception catcher to add.
     * @return This service.
     */
    public HttpService catcher(final HttpCatcher<?> catcher) {
        catchers.add(catcher);
        return this;
    }

    /**
     * Adds an exception catcher to this service.
     * <p>
     * An exception catcher is invoked whenever an exception occurs in a
     * filter, route handler, or other catcher, while handling an incoming
     * HTTP request that matches its method, path pattern and exception class.
     *
     * @param method         Request method to match.
     * @param pattern        Request path pattern to match.
     * @param exceptionClass Exception class to be assignable to.
     * @param handler        Handler to invoke with matching exceptions.
     * @return This service.
     * @see #catcher(HttpCatcher)
     */
    public <T extends Throwable> HttpService catcher(
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
     * Adds an exception catcher to this service.
     * <p>
     * An exception catcher is invoked whenever an exception occurs in a
     * filter, route handler, or other catcher, while handling an incoming
     * HTTP request that matches its method, path pattern and exception class.
     * <p>
     * <i>This method creates a catcher that matches any exception.</i>
     *
     * @param method  Request method to match.
     * @param pattern Request path pattern to match.
     * @param handler Handler to invoke with matching exceptions.
     * @return This service.
     * @see #catcher(HttpCatcher)
     */
    public HttpService catcher(
        final HttpMethod method,
        final String pattern,
        final HttpCatcherHandler<Throwable> handler)
    {
        return catcher(method, pattern, Throwable.class, handler);
    }

    /**
     * Adds an exception catcher to this service.
     * <p>
     * An exception catcher is invoked whenever an exception occurs in a
     * filter, route handler, or other catcher, while handling an incoming
     * HTTP request that matches its method, path pattern and exception class.
     * <p>
     * <i>This method creates a catcher that matches any method or
     * exception.</i>
     *
     * @param pattern Request path pattern to match.
     * @param handler Handler to invoke with matching exceptions.
     * @return This service.
     * @see #catcher(HttpCatcher)
     */
    public HttpService catcher(final String pattern, final HttpCatcherHandler<Throwable> handler) {
        return catcher(null, pattern, Throwable.class, handler);
    }

    /**
     * Adds an exception catcher to this service.
     * <p>
     * An exception catcher is invoked whenever an exception occurs in a
     * filter, route handler, or other catcher, while handling an incoming
     * HTTP request that matches its method, path pattern and exception class.
     * <p>
     * <i>This method creates a catcher that matches any method.</i>
     *
     * @param pattern        Request path pattern to match.
     * @param exceptionClass Exception class to be assignable to.
     * @param handler        Handler to invoke with matching exceptions.
     * @return This service.
     * @see #catcher(HttpCatcher)
     */
    public <T extends Throwable> HttpService catcher(
        final String pattern,
        final Class<T> exceptionClass,
        final HttpCatcherHandler<T> handler)
    {
        return catcher(null, pattern, exceptionClass, handler);
    }

    /**
     * Adds an exception catcher to this service.
     * <p>
     * An exception catcher is invoked whenever an exception occurs in a
     * filter, route handler, or other catcher, while handling an incoming
     * HTTP request that matches its method, path pattern and exception class.
     * <p>
     * <i>This method creates a catcher that matches any path or exception.</i>
     *
     * @param method  Request method to match.
     * @param handler Handler to invoke with matching exceptions.
     * @return This service.
     * @see #catcher(HttpCatcher)
     */
    public HttpService catcher(final HttpMethod method, final HttpCatcherHandler<Throwable> handler) {
        return catcher(method, null, Throwable.class, handler);
    }

    /**
     * Adds an exception catcher to this service.
     * <p>
     * An exception catcher is invoked whenever an exception occurs in a
     * filter, route handler, or other catcher, while handling an incoming
     * HTTP request that matches its method, path pattern and exception class.
     * <p>
     * <i>This method creates a catcher that matches any path.</i>
     *
     * @param method         Request method to match.
     * @param exceptionClass Exception class to be assignable to.
     * @param handler        Handler to invoke with matching exceptions.
     * @return This service.
     * @see #catcher(HttpCatcher)
     */
    public <T extends Throwable> HttpService catcher(
        final HttpMethod method,
        final Class<T> exceptionClass,
        final HttpCatcherHandler<T> handler)
    {
        return catcher(method, null, exceptionClass, handler);
    }

    /**
     * Adds an exception catcher to this service.
     * <p>
     * An exception catcher is invoked whenever an exception occurs in a
     * filter, route handler, or other catcher, while handling an incoming
     * HTTP request that matches its method, path pattern and exception class.
     * <p>
     * <i>This method creates a catcher that matches any method or path.</i>
     *
     * @param exceptionClass Exception class to be assignable to.
     * @param handler        Handler to invoke with matching exceptions.
     * @return This service.
     * @see #catcher(HttpCatcher)
     */
    public <T extends Throwable> HttpService catcher(
        final Class<T> exceptionClass,
        final HttpCatcherHandler<T> handler)
    {
        return catcher(null, null, exceptionClass, handler);
    }

    /**
     * Adds an exception catcher to this service.
     * <p>
     * An exception catcher is invoked whenever an exception occurs in a
     * filter, route handler, or other catcher, while handling an incoming
     * HTTP request that matches its method, path pattern and exception class.
     * <p>
     * <i>This method creates a catcher that matches any method, path or
     * exception.</i>
     *
     * @param handler Handler to invoke with matching exceptions.
     * @return This service.
     * @see #catcher(HttpCatcher)
     */
    public HttpService catcher(final HttpCatcherHandler<Throwable> handler) {
        return catcher(null, null, Throwable.class, handler);
    }

    /**
     * Adds incoming HTTP request filter to the created service.
     * <p>
     * Filters are executed with incoming HTTP requests before they end up
     * at their designated routes. Each filter matching the method and path
     * of the request is given the opportunity read the request, modify its
     * headers and, potentially, respond to the request. If a response is
     * generated, no more handler are invoked with the request, unless sending
     * the response fails and a catcher is executed.
     * <p>
     * If multiple filters are created with matching methods or path
     * patterns, the one with the smallest ordinal will be executed first.
     * If no filter responds to the request, all matching filters are
     * executed before the request is provided to its designated route.
     *
     * @param filter HTTP request filter to add.
     * @return This service.
     */
    public HttpService filter(final HttpFilter filter) {
        filters.add(filter);
        return this;
    }

    /**
     * Adds incoming HTTP request filter to the created service.
     * <p>
     * Filters are executed with incoming HTTP requests before they end up
     * at their designated routes. Each filter matching the method and path
     * of the request is given the opportunity read the request, modify its
     * headers and, potentially, respond to the request. If a response is
     * generated, no more handler are invoked with the request, unless sending
     * the response fails and a catcher is executed.
     *
     * @param method  Request method to match.
     * @param pattern Request path pattern to match.
     * @param handler Handler to invoke with matching requests.
     * @return This service.
     * @see #filter(HttpFilter)
     */
    public HttpService filter(
        final HttpMethod method,
        final String pattern,
        final HttpFilterHandler handler)
    {
        return filter(new HttpFilter(filterOrdinal++, method, pattern != null
            ? HttpPattern.valueOf(pattern)
            : null, handler));
    }

    /**
     * Adds incoming HTTP request filter to the created service.
     * <p>
     * Filters are executed with incoming HTTP requests before they end up
     * at their designated routes. Each filter matching the method and path
     * of the request is given the opportunity read the request, modify its
     * headers and, potentially, respond to the request. If a response is
     * generated, no more handler are invoked with the request, unless sending
     * the response fails and a catcher is executed.
     * <p>
     * <i>This method creates a filter that matches any method.</i>
     *
     * @param pattern Request path pattern to match.
     * @param handler Handler to invoke with matching requests.
     * @return This service.
     * @see #filter(HttpFilter)
     */
    public HttpService filter(final String pattern, final HttpFilterHandler handler) {
        return filter(null, pattern, handler);
    }

    /**
     * Adds incoming HTTP request filter to the created service.
     * <p>
     * Filters are executed with incoming HTTP requests before they end up
     * at their designated routes. Each filter matching the method and path
     * of the request is given the opportunity read the request, modify its
     * headers and, potentially, respond to the request. If a response is
     * generated, no more handler are invoked with the request, unless sending
     * the response fails and a catcher is executed.
     * <p>
     * <i>This method creates a filter that matches any path.</i>
     *
     * @param method  Request method to match.
     * @param handler Handler to invoke with matching requests.
     * @return This service.
     * @see #filter(HttpFilter)
     */
    public HttpService filter(final HttpMethod method, final HttpFilterHandler handler) {
        return filter(method, null, handler);
    }

    /**
     * Adds incoming HTTP request filter to the created service.
     * <p>
     * Filters are executed with incoming HTTP requests before they end up
     * at their designated routes. Each filter matching the method and path
     * of the request is given the opportunity read the request, modify its
     * headers and, potentially, respond to the request. If a response is
     * generated, no more handler are invoked with the request, unless sending
     * the response fails and a catcher is executed.
     * <p>
     * <i>This method creates a filter that matches any method or
     * path.</i>
     *
     * @param handler Handler to invoke with matching requests.
     * @return This service.
     * @see #filter(HttpFilter)
     */
    public HttpService filter(final HttpFilterHandler handler) {
        return filter(null, null, handler);
    }

    /**
     * @return Unmodifiable list of all currently set
     * {@link HttpCatcher catchers}.
     * @see #catcher(HttpCatcher)
     */
    public List<HttpCatcher<?>> catchers() {
        return Collections.unmodifiableList(catchers);
    }

    /**
     * {@inheritDoc}
     *
     * @see #name(String)
     */
    @Override
    public String name() {
        return name != null ? name : "";
    }

    /**
     * @return Currently set base path, or an empty string if none has been
     * set.
     * @see #basePath(String)
     */
    public String basePath() {
        return basePath != null ? basePath : "";
    }

    /**
     * {@inheritDoc}
     *
     * @see #basePath(String)
     */
    @Override
    public String uri() {
        return basePath();
    }

    @Override
    public TransportDescriptor transport() {
        return HTTP;
    }

    /**
     * {@inheritDoc}
     *
     * @see #accessPolicy(AccessPolicy)
     */
    @Override
    public AccessPolicy accessPolicy() {
        return accessPolicy != null ? accessPolicy : AccessPolicy.unrestricted();
    }

    /**
     * {@inheritDoc}
     *
     * @see #encodings(EncodingDescriptor...)
     */
    @Override
    public List<EncodingDescriptor> encodings() {
        return Collections.unmodifiableList(encodings);
    }

    /**
     * {@inheritDoc}
     *
     * @see #metadata(Map)
     */
    @Override
    public Map<String, String> metadata() {
        return metadata != null
            ? Collections.unmodifiableMap(metadata)
            : Collections.emptyMap();
    }

    /**
     * Gets value from service {@link #metadata(Map) metadata map} associated
     * with given {@code key}, if any.
     *
     * @param key Key associated with desired metadata value.
     * @return Value associated with {@code key}, if any.
     */
    public Optional<String> metadata(final String key) {
        return Optional.ofNullable(metadata().get(key));
    }

    /**
     * @return Currently set service version.
     * @see #version(int)
     */
    public int version() {
        return version;
    }

    /**
     * @return Unmodifiable list of all currently set {@link HttpRoute routes}.
     * @see #route(HttpRoute)
     */
    public List<HttpRoute> routes() {
        return Collections.unmodifiableList(routes);
    }

    /**
     * @return Unmodifiable list of all currently set {@link HttpFilter
     * filters}.
     * @see #filter(HttpFilter)
     */
    public List<HttpFilter> filters() {
        return Collections.unmodifiableList(filters);
    }
}
