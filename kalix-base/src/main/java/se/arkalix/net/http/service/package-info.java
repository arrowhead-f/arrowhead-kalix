/**
 * <h1>HTTP Service Utilities</h1>
 * The class of primary relevance for specifying Arrowhead HTTP {@link
 * se.arkalix services} is the {@link se.arkalix.net.http.service.HttpService
 * HttpService} class. After creating an instance of it, four details about the
 * Arrowhead service it represents must be specified, which are
 * <ol>
 *     <li>its {@link se.arkalix.net.http.service.HttpService#name(java.lang.String)
 *         name},</li>
 *     <li>its {@link se.arkalix.net.http.service.HttpService#basePath(java.lang.String)
 *         base path},</li>
 *     <li>what {@link se.arkalix.net.http.service.HttpService#encodings(se.arkalix.descriptor.EncodingDescriptor...)
 *         encodings} it supports, as well as</li>
 *     <li>what {@link se.arkalix.net.http.service.HttpService#accessPolicy(se.arkalix.security.access.AccessPolicy)
 *         access policy} it should use, unless the {@link se.arkalix.ArSystem
 *         system} that is to {@link
 *         se.arkalix.ArSystem#provide(se.arkalix.ArService) provide it} is
 *         running in {@link se.arkalix.ArSystem.Builder#insecure() insecure
 *         mode}.</li>
 * </ol>
 * After having provided these details, each created service must be given at
 * at least one {@link se.arkalix.net.http.service.HttpRoute route}, and may
 * also be given additional {@link se.arkalix.net.http.service.HttpFilter
 * filters} and {@link se.arkalix.net.http.service.HttpCatcher catchers}.
 * Routes, filters and catchers are collectively referred to as
 * {@link se.arkalix.net.http.service.HttpRoutable routables}, and are what
 * will receive and handle any incoming {@link
 * se.arkalix.net.http.service.HttpServiceRequest service requests}.
 *
 * <h2>Route Sequences</h2>
 * Routables are organized into <i>route sequences</i>, which can be visualized
 * as follows:
 * <pre>
 *     REQUEST RECEIVED
 *            |
 *            V
 *     +------------+ responds
 *     | Filter  1  |---------------------------------+
 *     | Filter  2  |                                 |
 *     | Filter  3  |        +-------------+          |
 *     | Filter ... | throws | Catcher  1  |          |
 *     | Filter  N  |-------&gt;| Catcher  2  |          |
 *     +-----+------+        | Catcher  3  | responds |
 *           | delegates     | Catcher ... |----------+
 *           |               | Catcher  M  |          |
 *           |               +-------------+          |
 *           |                      A                 |
 *           V               throws |                 |
 *     +------------+               | responds        V
 *     |   Route    |---------------+---------&gt; RESPONSE SENT
 *     +------------+
 * </pre>
 * When an incoming request is received, a route sequence with a matching route
 * is identified. If the sequence contains any filters, those are executed
 * in order from most to least specialized (i.e. filters with more
 * requirements for being matched are always executed first). If any filter
 * decides to respond to the request, the response is returned immediately to
 * the sender and the route never receives the request. If a filter or the
 * route throws an exception, the catcher sequence is executed in order, from
 * the most to the least specialized. If no catcher is available, no catcher
 * decided to respond to the request, or a catcher threw an exception and no
 * other catcher caught it, any exceptions are logged and the request is
 * replied to with a <i>500 INTERNAL SERVER ERROR</i> message. Note that
 * certificate validation and {@link se.arkalix.security.access.AccessPolicy
 * access control} are performed <i>before</i> any filters are called.
 * <p>
 * Routes, filters and catchers are given to a service via the {@link
 * se.arkalix.net.http.service.HttpService#route(se.arkalix.net.http.service.HttpRoute)
 * #route(HttpRoute)}, {@link
 * se.arkalix.net.http.service.HttpService#filter(HttpFilter)
 * #filter(HttpFilter)} and {@link
 * se.arkalix.net.http.service.HttpService#catcher(se.arkalix.net.http.service.HttpCatcher)
 * #catcher(HttpCatcher)} methods of the {@link
 * se.arkalix.net.http.service.HttpService HttpService} class, and the many
 * helper methods that exist to make it more convenient to use those three
 * methods.
 *
 * <h2>Route Matching</h2>
 * When a {@link se.arkalix.net.http.service.HttpService HttpService} receives
 * a request, it must be able to tell what route sequence, as described above,
 * to provide the request to. This is done by matching the request against the
 * {@link se.arkalix.net.http.HttpMethod methods} and {@link
 * se.arkalix.net.http.service.HttpPattern patterns} of its {@link
 * se.arkalix.net.http.service.HttpRoute routes}. While matching methods is
 * rather straightforward, either it matches or it doesn't, matching the path
 * of a request against a path pattern is more involved. How path pattern
 * matching is performed is explained in detail {@link
 * se.arkalix.net.http.service.HttpPattern here}.
 */
package se.arkalix.net.http.service;