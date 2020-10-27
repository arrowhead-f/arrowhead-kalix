package se.arkalix.net.http;

import se.arkalix.dto.DtoReadable;
import se.arkalix.net.MessageException;
import se.arkalix.net.ToEncoding;
import se.arkalix.util.concurrent.Future;

import java.util.List;

/**
 * An incoming HTTP response.
 *
 * @param <Self> Implementing class.
 */
@SuppressWarnings("unused")
public interface HttpIncomingResponse<Self, Request extends HttpOutgoingRequest<?>> extends HttpIncoming<Self> {
    /**
     * Retrieves the body of this response, if its status code is in the range
     * 200-299.
     * <p>
     * If the body is to be decoded, an attempt will be made to automatically
     * resolve a supported DTO encoding. If the attempt fails an exception is
     * thrown.
     *
     * @param class_ Class to decode incoming HTTP body into.
     * @param <R>    Type of {@code class_}.
     * @return Future completed immediately with an exception if the status
     * code is outside the success range, or when the incoming HTTP body has
     * been fully received and decoded into an instance of {@code class_}.
     * @throws MessageException If resolving a default encoding
     *                                         failed.
     * @throws IllegalStateException           If the body has already been
     *                                         requested.
     */
    default <R extends DtoReadable> Future<R> bodyAsIfSuccess(final Class<R> class_) {
        if (status().isSuccess()) {
            return bodyAs(class_);
        }
        return Future.failure(reject());
    }

    /**
     * Retrieves the body of this response, if its status code is in the range
     * 200-299.
     *
     * @param encoding Encoding to use if decoding incoming HTTP body.
     * @param class_   Class to decode incoming HTTP body into.
     * @param <R>      Type of {@code class_}.
     * @return Future completed immediately with an exception if the status
     * code is outside the success range, or when the incoming HTTP body has
     * been fully received and decoded into an instance of {@code class_}.
     * @throws IllegalStateException If the body has already been requested.
     */
    default <R extends DtoReadable> Future<R> bodyAsIfSuccess(final ToEncoding encoding, final Class<R> class_) {
        if (status().isSuccess()) {
            return bodyAs(encoding, class_);
        }
        return Future.failure(reject());
    }

    /**
     * Retrieves the body of this response as a list of instances of the
     * specified class, if its status code is in the range 200-299.
     * <p>
     * If the body is to be decoded, an attempt will be made to automatically
     * resolve a supported DTO encoding. If the attempt fails an exception is
     * thrown.
     *
     * @param class_ Class to decode incoming HTTP body into.
     * @param <R>    Type of {@code class_}.
     * @return Future completed immediately with an exception if the status
     * code is outside the success range, or when the incoming HTTP body has
     * been fully received and decoded into an instance of {@code class_}.
     * @throws MessageException If resolving a default encoding
     *                                         failed.
     * @throws IllegalStateException           If the body has already been
     *                                         requested.
     */
    default <R extends DtoReadable> Future<List<R>> bodyAsListIfSuccess(final Class<R> class_) {
        if (status().isSuccess()) {
            return bodyAsList(class_);
        }
        return Future.failure(reject());
    }

    /**
     * Retrieves the body of this response as a list of instances of the
     * specified class, if its status code is in the range 200-299.
     *
     * @param encoding Encoding to use if decoding incoming HTTP body.
     * @param class_   Class to decode incoming HTTP body into.
     * @param <R>      Type of {@code class_}.
     * @return Future completed immediately with an exception if the status
     * code is outside the success range, or when the incoming HTTP body has
     * been fully received and decoded into an instance of {@code class_}.
     * @throws IllegalStateException If the body has already been requested.
     */
    default <R extends DtoReadable> Future<List<R>> bodyAsListIfSuccess(final ToEncoding encoding, final Class<R> class_) {
        if (status().isSuccess()) {
            return bodyAsList(encoding, class_);
        }
        return Future.failure(reject());
    }

    /**
     * Creates an exception containing this response.
     * <p>
     * This method is primarily intended to be used when receiving messages
     * that contain unexpected status codes. If the reason behind the rejection
     * requires more explanation, please use {@link #reject(String)} instead.
     *
     * @return Exception wrapping this response.
     */
    default HttpIncomingResponseUnexpected reject() {
        return new HttpIncomingResponseUnexpected(this);
    }

    /**
     * Creates an exception containing this response and a description of why
     * it was rejected.
     *
     * @param reason Description of what expectations this request fails to
     *               fulfill.
     * @return Exception wrapping this response.
     */
    default HttpIncomingResponseUnexpected reject(final String reason) {
        return new HttpIncomingResponseUnexpected(this, reason);
    }

    /**
     * Creates an exception containing this response and a description of why
     * it was rejected.
     *
     * @param reason Description of what expectations this response fails to
     *               fulfill.
     * @param cause  Exception thrown due to this response not fulfilling some
     *               arbitrary requirement.
     * @return Exception wrapping this response.
     */
    default HttpIncomingResponseUnexpected reject(final String reason, final Throwable cause) {
        return new HttpIncomingResponseUnexpected(this, reason, cause);
    }

    /**
     * Returns a {@code Future} that contains the exception returned by
     * {@link #reject()}, if its status code is not in the range 200-299.
     * Otherwise a successful future containing {@code null} is returned.
     * <p>
     * This method is primarily intended to be used when receiving messages
     * that contain unexpected status codes and no response body is expected.
     * If a response body <i>is</i> expected, please use
     * {@link #bodyAsIfSuccess(ToEncoding, Class)} instead. If the
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
     * {@link #bodyAsIfSuccess(ToEncoding, Class)} instead.
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
     * Gets request that was responded to with this message.
     *
     * @return Original response request.
     */
    Request request();

    /**
     * Gets HTTP status code associated with this response.
     *
     * @return Response status.
     */
    HttpStatus status();
}
