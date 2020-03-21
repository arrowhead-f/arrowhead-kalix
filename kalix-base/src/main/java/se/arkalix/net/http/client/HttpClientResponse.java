package se.arkalix.net.http.client;

import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadable;
import se.arkalix.net.http.*;
import se.arkalix.util.concurrent.Future;

import java.util.List;
import java.util.Optional;

/**
 * An incoming  HTTP response.
 */
public interface HttpClientResponse extends HttpBodyReceiver {
    /**
     * Retrieves the body of this response, if its status code is in the range
     * 200-299.
     *
     * @param encoding Encoding to use if decoding incoming HTTP body.
     * @param class_   Class to decode incoming HTTP body into.
     * @param <R>      Type of {@code class_}.
     * @return Future completed immediately with an exception if the status
     * code is outside the success range, or when the incoming HTTP body has been
     * fully received and decoded into an instance of {@code class_}.
     * @throws IllegalStateException If the body has already been requested.
     */
    default <R extends DtoReadable> Future<R> bodyAsClassIfSuccess(
        final DtoEncoding encoding,
        final Class<R> class_)
    {
        if (status().isSuccess()) {
            return bodyAs(encoding, class_);
        }
        return Future.failure(reject());
    }

    /**
     * Gets a response header value by name.
     *
     * @param name Name of header. Case insensitive. Prefer lowercase.
     * @return Header value, if any.
     */
    default Optional<String> header(final CharSequence name) {
        return headers().get(name);
    }

    /**
     * Gets all header values associated with given {@code name}, if any.
     *
     * @param name Name of header. Case is ignored. Prefer lowercase.
     * @return Header values. May be an empty list.
     */
    default List<String> headers(final CharSequence name) {
        return headers().getAll(name);
    }

    /**
     * @return Response headers.
     */
    HttpHeaders headers();

    /**
     * Creates an exception containing this response.
     * <p>
     * This method is primarily intended to be used when receiving messages
     * that contain unexpected status codes. If the reason behind the rejection
     * requires more explanation, please use {@link #reject(String)} instead.
     *
     * @return Exception wrapping this response.
     */
    default HttpClientResponseRejectedException reject() {
        return new HttpClientResponseRejectedException(this);
    }

    /**
     * Creates an exception containing this response and a description of why
     * it was rejected.
     *
     * @param reason Description of what expectations this request fails to
     *               fulfill.
     * @return Exception wrapping this response.
     */
    default HttpClientResponseRejectedException reject(final String reason) {
        return new HttpClientResponseRejectedException(this, reason);
    }

    /**
     * Returns a {@code Future} that contains the exception returned by
     * {@link #reject()}, if its status code is not in the range 200-299.
     * Otherwise a successful future containing {@code null} is returned.
     * <p>
     * This method is primarily intended to be used when receiving messages
     * that contain unexpected status codes and no response body is expected.
     * If a response body <i>is</i> expected, please use
     * {@link #bodyAsClassIfSuccess(DtoEncoding, Class)} instead. If the
     * reason behind the rejection requires more explanation, please use
     * {@link #rejectIfNotSuccess(String)} instead.
     *
     * @return Future completed with exception only if this response contains a
     * status code outside the range 200-299.
     */
    default Future<?> rejectIfNotSuccess() {
        return status().isSuccess()
            ? Future.done()
            : Future.failure(reject());
    }

    /**
     * Returns a {@code Future} that contains the exception returned by
     * {@link #reject(String)}, if its status code is not in the range 200-299.
     * Otherwise a successful future containing {@code null} is returned.
     * <p>
     * This method is primarily intended to be used when receiving messages
     * that contain unexpected status codes and no response body is expected.
     * If a response body <i>is</i> expected, please use
     * {@link #bodyAsClassIfSuccess(DtoEncoding, Class)} instead.
     *
     * @param reason Description of what expectations this request fails to
     *               fulfill.
     * @return Future completed with exception only if this response contains a
     * status code outside the range 200-299.
     */
    default Future<?> rejectIfNotSuccess(final String reason) {
        return status().isSuccess()
            ? Future.done()
            : Future.failure(reject(reason));
    }

    /**
     * @return Response status.
     */
    HttpStatus status();

    /**
     * @return Response version.
     */
    HttpVersion version();
}
