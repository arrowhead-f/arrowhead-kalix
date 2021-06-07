package se.arkalix.net.http;

import se.arkalix.codec.*;
import se.arkalix.net.MessageCodecMisspecified;
import se.arkalix.net.MessageCodecUnspecified;
import se.arkalix.net.MessageCodecUnsupported;
import se.arkalix.net.MessageHasTrailingData;
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
     * Collects and then converts the incoming message body using the provided
     * {@code decoder}, if its status code of this message is between 200 and
     * 299.
     * <p>
     * Calling this method consumes the body associated with this message. Any
     * further attempts to consume the body will cause exceptions to be thrown.
     *
     * @param <T>     Type produced by given {@code decoder}, if successful.
     * @param decoder Function to use for decoding the message body.
     * @return Future completed when the incoming message body has been fully
     * received and decoded.
     * @throws DecoderException       If the body does not conform to the codec
     *                                of the given {@code decoder}.
     * @throws IllegalStateException  If the body has already been consumed.
     * @throws MessageHasTrailingData If trailing data is discovered in the
     *                                message body.
     * @throws NullPointerException   If {@code decoder} is {@code null}.
     */
    default <T> Future<T> bodyToIfSuccess(final Decoder<T> decoder) {
        if (status().isSuccess()) {
            return bodyTo(decoder);
        }
        return Future.failure(reject());
    }

    /**
     * If the status code of this message is between 200 and 299, this method
     * collects and then converts the incoming message body using the provided
     * {@code decoder}, which will attempt to select an appropriate decoder
     * function from any {@link #codecType() codec} specified in the message.
     * <p>
     * Calling this method consumes the body associated with this message. Any
     * further attempts to consume the body will cause exceptions to be thrown.
     *
     * @param <T>     Type produced by given {@code decoder}, if
     *                successful.
     * @param decoder Function to use for decoding the message body.
     * @return Future completed when the incoming message body has been fully
     * received and decoded.
     * @throws DecoderException         If the body does not conform to the
     *                                  codec it states.
     * @throws IllegalStateException    If the body has already been consumed.
     * @throws MessageCodecMisspecified If a codec is specified in the
     *                                  message, but it cannot be interpreted.
     * @throws MessageCodecUnspecified  If no codec is specified in this
     *                                  message.
     * @throws MessageCodecUnsupported  If the codec specified in the
     *                                  message is not supported by the given
     *                                  {@code decoder}.
     * @throws MessageHasTrailingData   If trailing data is discovered in the
     *                                  message body.
     * @throws NullPointerException     If {@code decoder} is {@code null}.
     */
    default <T> Future<T> bodyToIfSuccess(final MultiDecoder<T> decoder) {
        if (status().isSuccess()) {
            return bodyTo(decoder);
        }
        return Future.failure(reject());
    }

    /**
     * If the status code of this message is between 200 and 299, this method
     * collects and then converts the incoming message body using the provided
     * {@code decoder}, which will attempt to select an appropriate decoder
     * function from any {@link #codecType() codec} specified in the message.
     * <p>
     * Calling this method consumes the body associated with this message. Any
     * further attempts to consume the body will cause exceptions to be thrown.
     *
     * @param <T>         Type produced by given {@code decoder}, if
     *                    successful.
     * @param decoder     Function to use for decoding the message body.
     * @param toCodecType Codec to use when invoking {@code decoder}.
     * @return Future completed when the incoming message body has been fully
     * received and decoded.
     * @throws DecoderException        If the body does not conform to the
     *                                 codec it states.
     * @throws IllegalStateException   If the body has already been consumed.
     * @throws MessageCodecUnsupported If the given codec is not supported by
     *                                 the given {@code decoder}.
     * @throws MessageHasTrailingData  If trailing data is discovered in the
     *                                 message body.
     * @throws NullPointerException    If {@code decoder} or {@code toCodecType}
     *                                 is {@code null}.
     */
    default <T> Future<T> bodyToIfSuccess(final MultiDecoder<T> decoder, final ToCodecType toCodecType) {
        if (status().isSuccess()) {
            return bodyTo(decoder, toCodecType);
        }
        return Future.failure(reject());
    }

    /**
     * If the status code of this message is between 200 and 299, this method
     * collects and then converts the individual list items of the incoming
     * message body using the provided {@code decoder}, which will attempt to
     * select an appropriate decoder function from any {@link #codecType()
     * codec} specified in the message.
     * <p>
     * This method can only succeed for codecs that both support anonymous
     * lists and are explicitly listed as supported by {@link
     * MultiDecoderForLists#supportedEncodings()}.
     * <p>
     * Calling this method consumes the body associated with this message. Any
     * further attempts to consume the body will cause exceptions to be thrown.
     *
     * @param <T>     Type produced by given {@code decoder}, if successful.
     * @param decoder Function to use for decoding the message body.
     * @return Future completed when the incoming message body has been fully
     * received and decoded.
     * @throws DecoderException         If the body does not conform to the
     *                                  codec it states.
     * @throws IllegalStateException    If the body has already been consumed.
     * @throws MessageCodecMisspecified If a codec is specified in the
     *                                  message, but it cannot be interpreted.
     * @throws MessageCodecUnspecified  If no codec is specified in this
     *                                  message.
     * @throws MessageCodecUnsupported  If the codec specified in the message
     *                                  is not supported by the given {@code
     *                                  decoder}.
     * @throws MessageHasTrailingData   If trailing data is discovered in the
     *                                  message body.
     * @throws NullPointerException     If {@code decoder} is {@code null}.
     */
    default <T> Future<List<T>> bodyListItemsToIfSuccess(final MultiDecoder<T> decoder) {
        if (status().isSuccess()) {
            return bodyListItemsTo(decoder);
        }
        return Future.failure(reject());
    }

    /**
     * If the status code of this message is between 200 and 299, this method
     * collects and then converts the individual list items of the incoming
     * message body using the provided {@code decoder}, which will attempt to
     * select a decoder function named by {@code toCodecType}.
     * <p>
     * This method can only succeed for codecs that both support anonymous
     * lists and are explicitly listed as supported by {@link
     * MultiDecoderForLists#supportedEncodings()}.
     * <p>
     * Calling this method consumes the body associated with this message. Any
     * further attempts to consume the body will cause exceptions to be thrown.
     *
     * @param <T>         Type produced by given {@code decoder}, if successful.
     * @param decoder     Function to use for decoding the message body.
     * @param toCodecType Codec to use when invoking {@code decoder}.
     * @return Future completed when the incoming message body has been fully
     * received and decoded.
     * @throws DecoderException        If the body does not conform to the
     *                                 codec it states.
     * @throws IllegalStateException   If the body has already been consumed.
     * @throws MessageCodecUnsupported If the given codec is not supported by
     *                                 the given {@code decoder}.
     * @throws MessageHasTrailingData  If trailing data is discovered in the
     *                                 message body.
     * @throws NullPointerException    If {@code decoder} or {@code
     *                                 toCodecType} is {@code null}.
     */
    default <T> Future<List<T>> bodyListItemsToIfSuccess(
        final MultiDecoder<T> decoder,
        final ToCodecType toCodecType
    ) {
        if (status().isSuccess()) {
            return bodyListItemsTo(decoder, toCodecType);
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
     * {@link #bodyToIfSuccess(MultiDecoder)} instead. If the reason behind
     * the rejection requires more explanation, please use
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
     * {@link #bodyToIfSuccess(MultiDecoder)} instead.
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
