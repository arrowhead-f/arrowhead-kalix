/**
 * <h1>HTTP Transport Utilities</h1>
 * This package contains classes and other utilities useful for accessing and
 * providing access to resources via the HTTP transport protocols. Currently,
 * only HTTP/1.0 and HTTP/1.1 are supported.
 * <p>
 * The package is organized into four distinct parts,
 * <ol>
 *     <li>protocol utilities,</li>
 *     <li>{@link se.arkalix.net.http.client client utilities},</li>
 *     <li>{@link se.arkalix.net.http.consumer consumer utilities}, and</li>
 *     <li>{@link se.arkalix.net.http.service service utilities}.</li>
 * </ol>
 * <h2>Protocol Utilities</h2>
 * The protocol utilities, which reside in this package, are used for
 * enumerating HTTP {@link se.arkalix.net.http.HttpStatus status codes}, {@link
 * se.arkalix.net.http.HttpMethod methods} and {@link
 * se.arkalix.net.http.HttpVersion versions}, as well as for organizing HTTP
 * {@link se.arkalix.net.http.HttpHeaders headers}.
 *
 * <h2>Client and Consumer Utilities</h2>
 * These are used to initiate HTTP connections to remote hosts. The
 * {@link se.arkalix.net.http.client client utilities} are primarily useful for
 * establishing connections to regular HTTP servers, while the {@link
 * se.arkalix.net.http.consumer consumer utilities} assist in fulfilling the
 * additional requirements for connecting to Arrowhead HTTP services.
 *
 * <h2>Service Utilities</h2>
 * These are useful for providing Arrowhead services over HTTP. Refer to the
 * {@link se.arkalix.net.http.service package documentation} for more details.
 *
 * @see se.arkalix.net.http.client HTTP Client Utilities
 * @see se.arkalix.net.http.consumer HTTP Consumer Utilities
 * @see se.arkalix.net.http.service HTTP Service Utilities
 */
package se.arkalix.net.http;