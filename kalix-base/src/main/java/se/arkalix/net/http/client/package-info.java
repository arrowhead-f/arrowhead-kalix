/**
 * <h1>HTTP Client Utilities</h1>
 * This package most significantly contains the {@link
 * se.arkalix.net.http.client.HttpClient HttpClient} class, which is useful for
 * sending HTTP requests to remote servers or services.
 * <p>
 * It should be noted that all functionality provided by this package is
 * <i>non-blocking</i> by virtue of all I/O operations being handled by the
 * {@link se.arkalix.util.concurrent.Schedulers Kalix schedulers}.
 *
 * @see se.arkalix.net.http.client.HttpClient HttpClient
 * @see se.arkalix.net.http.client.HttpClientRequest HttpClientRequest
 * @see se.arkalix.net.http.client.HttpClientResponse HttpClientResponse
 */
package se.arkalix.net.http.client;