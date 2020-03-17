package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.AhfService;
import eu.arrowhead.kalix.AhfSystem;
import eu.arrowhead.kalix.description.ServiceDescription;
import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.descriptor.InterfaceDescriptor;
import eu.arrowhead.kalix.descriptor.SecurityDescriptor;
import eu.arrowhead.kalix.descriptor.TransportDescriptor;
import eu.arrowhead.kalix.net.http.HttpMethod;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A concrete Arrowhead service, exposing its functions as HTTP endpoints.
 * <p>
 * TODO: Write more extensive documentation, including about validators, routes
 * and catchers, as well as about patterns and matching.
 */
public class HttpService implements AhfService {
    private final ArrayList<HttpCatcher<?>> catchers = new ArrayList<>(0);
    private final ArrayList<HttpRoute> routes = new ArrayList<>(0);
    private final ArrayList<HttpValidator> validators = new ArrayList<>(0);

    private String name;
    private String basePath;
    private List<EncodingDescriptor> encodings;
    private SecurityDescriptor security;
    private Map<String, String> metadata;
    private int version = 0;

    private int catcherOrdinal = 0;
    private int filterOrdinal = 0;

    /**
     * Sets name of this service. <b>Must be specified.</b>
     * <p>
     * Service names are also referred to as <i>service definitions</i>.
     *
     * @param name Desired service name.
     * @return This service.
     */
    public HttpService name(final String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets service qualifier, in this case a <i>base path</i>, that must be
     * matched by HTTP requests received by this service. <b>Must be
     * specified.</b>
     * <p>
     * The base path must start with a forward slash ({@code /}), must not end
     * with a forward slash, and may only contain the following characters:
     * <pre>
     *     A–Z a–z 0–9 - . _ ~ ! $ & ' ( ) * + , ; / = : @
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
     * {@code eu.arrowhead.kalix.dto} package.
     *
     * @param encodings Encodings declared to be supported. At least one must
     *                  be provided. The first specified encoding is used by
     *                  default when received requests do not include enough
     *                  details about their bodies.
     * @return This service.
     * @see eu.arrowhead.kalix.dto
     */
    public HttpService encodings(final EncodingDescriptor... encodings) {
        this.encodings = Arrays.asList(encodings.clone());
        return this;
    }

    /**
     * Declares what security mode this service is to use. <b>Must be
     * specified.</b>
     * <p>
     * It is an error to specify {@link SecurityDescriptor#NOT_SECURE
     * NOT_SECURE} if the {@link AhfSystem system}
     * that is to run the service is running in secure mode. The opposite is
     * also the case. If the system is running in insecure mode, then must
     * {@link SecurityDescriptor#NOT_SECURE NOT_SECURE} be specified here.
     *
     * @param security Desired security mode.
     * @return This service.
     */
    public HttpService security(final SecurityDescriptor security) {
        this.security = security;
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
     * Saves key/value pair into metadata map associated with this service.
     *
     * @param key   Desired key.
     * @param value Desired value.
     * @return This service.
     * @see #metadata(Map)
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
     * However, if a {@link HttpValidator validator} executed before the route
     * responds to the request or throws an exception, it is never invoked. In
     * the case of either a validator or a route throwing an exception, any
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
     * However, if a {@link HttpValidator validator} executed before the route
     * responds to the request or throws an exception, it is never invoked. In
     * the case of either a validator or a route throwing an exception, any
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
     * However, if a {@link HttpValidator validator} executed before the route
     * responds to the request or throws an exception, it is never invoked. In
     * the case of either a validator or a route throwing an exception, any
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
     * However, if a {@link HttpValidator validator} executed before the route
     * responds to the request or throws an exception, it is never invoked. In
     * the case of either a validator or a route throwing an exception, any
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
     * However, if a {@link HttpValidator validator} executed before the route
     * responds to the request or throws an exception, it is never invoked. In
     * the case of either a validator or a route throwing an exception, any
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
     * validator, route handler, or other catcher, while handling an incoming
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
     * validator, route handler, or other catcher, while handling an incoming
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
     * validator, route handler, or other catcher, while handling an incoming
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
     * validator, route handler, or other catcher, while handling an incoming
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
     * validator, route handler, or other catcher, while handling an incoming
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
     * validator, route handler, or other catcher, while handling an incoming
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
     * validator, route handler, or other catcher, while handling an incoming
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
     * validator, route handler, or other catcher, while handling an incoming
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
     * validator, route handler, or other catcher, while handling an incoming
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
     * Adds incoming HTTP request validator to the created service.
     * <p>
     * Validators are executed with incoming HTTP requests before they end up
     * at their designated routes. Each validator matching the method and path
     * of the request is given the opportunity read the request, modify its
     * headers and, potentially, respond to the request. If a response is
     * generated, no more handler are invoked with the request, unless sending
     * the response fails and a catcher is executed.
     * <p>
     * If multiple validators are created with matching methods or path
     * patterns, the one with the smallest ordinal will be executed first.
     * If no validator responds to the request, all matching validators are
     * executed before the request is provided to its designated route.
     *
     * @param validator HTTP request validator to add.
     * @return This service.
     */
    public HttpService validator(final HttpValidator validator) {
        validators.add(validator);
        return this;
    }

    /**
     * Adds incoming HTTP request validator to the created service.
     * <p>
     * Validators are executed with incoming HTTP requests before they end up
     * at their designated routes. Each validator matching the method and path
     * of the request is given the opportunity read the request, modify its
     * headers and, potentially, respond to the request. If a response is
     * generated, no more handler are invoked with the request, unless sending
     * the response fails and a catcher is executed.
     *
     * @param method  Request method to match.
     * @param pattern Request path pattern to match.
     * @param handler Handler to invoke with matching requests.
     * @return This service.
     * @see #validator(HttpValidator)
     */
    public HttpService validator(
        final HttpMethod method,
        final String pattern,
        final HttpValidatorHandler handler)
    {
        return validator(new HttpValidator(filterOrdinal++, method, pattern != null
            ? HttpPattern.valueOf(pattern)
            : null, handler));
    }

    /**
     * Adds incoming HTTP request validator to the created service.
     * <p>
     * Validators are executed with incoming HTTP requests before they end up
     * at their designated routes. Each validator matching the method and path
     * of the request is given the opportunity read the request, modify its
     * headers and, potentially, respond to the request. If a response is
     * generated, no more handler are invoked with the request, unless sending
     * the response fails and a catcher is executed.
     * <p>
     * <i>This method creates a validator that matches any method.</i>
     *
     * @param pattern Request path pattern to match.
     * @param handler Handler to invoke with matching requests.
     * @return This service.
     * @see #validator(HttpValidator)
     */
    public HttpService validator(final String pattern, final HttpValidatorHandler handler) {
        return validator(null, pattern, handler);
    }

    /**
     * Adds incoming HTTP request validator to the created service.
     * <p>
     * Validators are executed with incoming HTTP requests before they end up
     * at their designated routes. Each validator matching the method and path
     * of the request is given the opportunity read the request, modify its
     * headers and, potentially, respond to the request. If a response is
     * generated, no more handler are invoked with the request, unless sending
     * the response fails and a catcher is executed.
     * <p>
     * <i>This method creates a validator that matches any path.</i>
     *
     * @param method  Request method to match.
     * @param handler Handler to invoke with matching requests.
     * @return This service.
     * @see #validator(HttpValidator)
     */
    public HttpService validator(final HttpMethod method, final HttpValidatorHandler handler) {
        return validator(method, null, handler);
    }

    /**
     * Adds incoming HTTP request validator to the created service.
     * <p>
     * Validators are executed with incoming HTTP requests before they end up
     * at their designated routes. Each validator matching the method and path
     * of the request is given the opportunity read the request, modify its
     * headers and, potentially, respond to the request. If a response is
     * generated, no more handler are invoked with the request, unless sending
     * the response fails and a catcher is executed.
     * <p>
     * <i>This method creates a validator that matches any method or
     * path.</i>
     *
     * @param handler Handler to invoke with matching requests.
     * @return This service.
     * @see #validator(HttpValidator)
     */
    public HttpService validator(final HttpValidatorHandler handler) {
        return validator(null, null, handler);
    }

    @Override
    public ServiceDescription describe() {
        final var isSecure = security != SecurityDescriptor.NOT_SECURE;
        return new ServiceDescription.Builder()
            .name(name)
            .qualifier(basePath)
            .security(security)
            .metadata(metadata != null ? metadata : Collections.emptyMap())
            .version(version)
            .supportedInterfaces(encodings.stream()
                .map(encoding -> InterfaceDescriptor.getOrCreate(TransportDescriptor.HTTP, isSecure, encoding))
                .collect(Collectors.toList()))
            .build();
    }

    /**
     * @return Currently set base path, if any.
     * @see #basePath(String)
     */
    public Optional<String> basePath() {
        return Optional.ofNullable(basePath);
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
     * @return Unmodifiable list of currently set encodings, if any.
     * @see #encodings(EncodingDescriptor...)
     */
    public List<EncodingDescriptor> encodings() {
        return Collections.unmodifiableList(encodings);
    }

    /**
     * @return Unmodifiable map of currently set service metadata.
     * @see #metadata(Map)
     */
    public Map<String, String> metadata() {
        return metadata != null
            ? Collections.unmodifiableMap(metadata)
            : Collections.emptyMap();
    }

    /**
     * Gets value from service metadata map associated with given {@code key},
     * if any.
     *
     * @param key Key associated with desired metadata value.
     * @return Value associated with {@code key}, if any.
     */
    public Optional<String> metadata(final String key) {
        return Optional.ofNullable(metadata.get(key));
    }

    /**
     * @return Currently set service name, if any.
     * @see #name(String)
     */
    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    /**
     * @return Unmodifiable list of all currently set
     * {@link HttpRoute routes}.
     * @see #route(HttpRoute)
     */
    public List<HttpRoute> routes() {
        return Collections.unmodifiableList(routes);
    }

    /**
     * @return Currently set security mode.
     * @see #security(SecurityDescriptor)
     */
    public Optional<SecurityDescriptor> security() {
        return Optional.ofNullable(security);
    }

    /**
     * @return Unmodifiable list of all currently set
     * {@link HttpValidator validators}.
     * @see #validator(HttpValidator)
     */
    public List<HttpValidator> validators() {
        return Collections.unmodifiableList(validators);
    }

    /**
     * @return Currently set service version.
     * @see #version(int)
     */
    public int version() {
        return version;
    }
}
