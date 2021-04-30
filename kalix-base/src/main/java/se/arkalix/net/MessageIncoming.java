package se.arkalix.net;

import se.arkalix.codec.*;
import se.arkalix.util.concurrent.Future;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * An incoming network message.
 */
@SuppressWarnings("unused")
public interface MessageIncoming extends Message {
    /**
     * Gets handle representing the payload of this message.
     *
     * @return Message payload handle.
     */
    BodyIncoming body();

    /**
     * Requests that the incoming message body be collected into a regular Java
     * byte array ({@code byte[]}).
     * <p>
     * Calling this method consumes the body associated with this message. Any
     * further attempts to consume the body will cause exceptions to be thrown.
     *
     * @return Future completed when the incoming message body has been fully
     * collected into a single byte array.
     * @throws IllegalStateException If the body has already been requested.
     */
    default Future<byte[]> bodyAsByteArray() {
        return body()
            .buffer()
            .map(reader -> {
                final var buffer = new byte[reader.readableBytes()];
                reader.readBytes(buffer);
                return buffer;
            });
    }

    /**
     * Requests that the incoming message body be decoded into a regular Java
     * {@code String}.
     * <p>
     * If a character codec supported by Java is specified in the message,
     * it will be used when decoding the body into a {@code String}. In any
     * other case, UTF-8 will be assumed to be adequate.
     * <p>
     * Calling this method consumes the body associated with this message. Any
     * further attempts to consume the body will cause exceptions to be thrown.
     *
     * @return Future completed when the incoming message body becomes has been
     * fully collected into a {@code String}.
     * @throws IllegalStateException If the body has already been requested.
     */
    default Future<String> bodyAsString() {
        return bodyAsString(codecType()
            .map(ToCodecType::toCodecType)
            .flatMap(CodecType::charset)
            .orElse(StandardCharsets.UTF_8));
    }

    /**
     * Requests that the incoming message body be decoded into a regular Java
     * {@code String} using the specified character set.
     * <p>
     * Calling this method consumes the body associated with this message. Any
     * further attempts to consume the body will cause exceptions to be thrown.
     *
     * @param charset Charset to use when decoding body.
     * @return Future completed when the incoming message body becomes has been
     * fully collected into a {@code String}.
     * @throws IllegalStateException If the body has already been requested.
     * @throws NullPointerException  If {@code charset} is {@code null}.
     */
    default Future<String> bodyAsString(final Charset charset) {
        if (charset == null) {
            throw new NullPointerException("charset");
        }
        return bodyAsByteArray().map(bytes -> new String(bytes, charset));
    }

    /**
     * Collects and then converts the incoming message body using the provided
     * {@code decoder}.
     * <p>
     * Calling this method consumes the body associated with this message. Any
     * further attempts to consume the body will cause exceptions to be thrown.
     *
     * @param <T>     Type produced by given {@code decoder}, if successful.
     * @param decoder Function to use for decoding the message body.
     * @return Future completed when the incoming message body has been fully
     * received and decoded.
     * @throws IllegalStateException If the body has already been consumed.
     * @throws NullPointerException  If {@code decoder} is {@code null}.
     */
    default <T> Future<T> bodyTo(final Decoder<T> decoder) {
        if (decoder == null) {
            throw new NullPointerException("decoder");
        }
        return body()
            .buffer()
            .map(decoder::decode);
        // TODO: Should we throw an exception if there is more in the body?
    }

    /**
     * Collects and then converts the incoming message body using the provided
     * {@code decoder}, which will attempt to select an appropriate
     * decoder function from any {@link #codecType() codec} specified in the
     * message.
     * <p>
     * Calling this method consumes the body associated with this message. Any
     * further attempts to consume the body will cause exceptions to be thrown.
     *
     * @param <T>     Type produced by given {@code decoder}, if successful.
     * @param decoder Function to use for decoding the message body.
     * @return Future completed when the incoming message body has been fully
     * received and decoded.
     * @throws MessageCodecMisspecified If a codec is specified in the
     *                                  message, but it cannot be interpreted.
     * @throws MessageCodecUnspecified  If no codec is specified in this
     *                                  message.
     * @throws MessageCodecUnsupported  If the codec specified in the
     *                                  message is not supported by the given
     *                                  {@code decoder}.
     * @throws IllegalStateException    If the body has already been consumed.
     * @throws NullPointerException     If {@code decoder} is {@code null}.
     */
    default <T> Future<T> bodyTo(final MultiDecoder<T> decoder) {
        return bodyTo(decoder, codecType().orElseThrow(() -> new MessageCodecUnspecified(this)));
    }

    /**
     * Collects and then converts the incoming message body using the provided
     * {@code decoder}, which will attempt to select a decoder
     * function named by {@code toCodecType}.
     * <p>
     * Calling this method consumes the body associated with this message. Any
     * further attempts to consume the body will cause exceptions to be thrown.
     *
     * @param <T>         Type produced by given {@code decoder}, if successful.
     * @param decoder     Function to use for decoding the message body.
     * @param toCodecType Codec to use when invoking {@code decoder}.
     * @return Future completed when the incoming message body has been fully
     * received and decoded.
     * @throws MessageCodecUnsupported If the given codec is not supported by
     *                                 the given {@code decoder}.
     * @throws IllegalStateException   If the body has already been consumed.
     * @throws NullPointerException    If {@code decoder} or {@code toCodecType}
     *                                 is {@code null}.
     */
    default <T> Future<T> bodyTo(final MultiDecoder<T> decoder, final ToCodecType toCodecType) {
        if (decoder == null) {
            throw new NullPointerException("decoder");
        }
        if (toCodecType == null) {
            throw new NullPointerException("toCodecType");
        }
        final var codecType = toCodecType.toCodecType();
        return body()
            .buffer()
            .map(reader -> decoder.decoderFor(codecType)
                .decode(reader));
        // TODO: Should we throw an exception if there is more in the body?
    }

    /**
     * Requests that the incoming message body be written to the file at the
     * specified file system path.
     * <p>
     * The file will be created if it does not exist. If the {@code append}
     * parameter is {@code true}, the file is appended to rather than being
     * overwritten.
     * <p>
     * Using this method, or {@link #bodyTo(Path)}, is the preferred way of
     * receiving data objects that are too large to handle in-memory. This as
     * received data is written directly to the target file as it is received,
     * rather than being buffered until all of it becomes available.
     * <p>
     * Calling this method consumes the body associated with this message. Any
     * further attempts to consume the body will cause exceptions to be thrown.
     *
     * @param path   Path to file to contain incoming message body.
     * @param append If {@code true}, any existing file at {@code path} will
     *               not be overwritten, but have the incoming message body
     *               appended to it.
     * @return Future completed successfully with given path only if the
     * incoming message body is fully received and written to the file at that
     * path.
     * @throws IllegalStateException If the body has already been requested.
     * @throws NullPointerException  If {@code path} is {@code null}.
     */
    default Future<?> bodyTo(final Path path, boolean append) {
        return body().writeTo(path, append);
    }

    /**
     * Requests that the incoming message body be written to the file at the
     * specified file system path.
     * <p>
     * The file will be created if it does not exist, or overwritten if it does
     * exist.
     * <p>
     * Using this method, or {@link #bodyTo(Path, boolean)}, is the preferred
     * way of receiving data objects that are too large to handle in-memory.
     * This as received data is written directly to the target file as it is
     * received, rather than being buffered until all of it becomes available.
     * <p>
     * Calling this method consumes the body associated with this message. Any
     * further attempts to consume the body will cause exceptions to be thrown.
     *
     * @param path Path to file to contain incoming message body.
     * @return Future completed successfully with given path only if the
     * incoming message body is fully received and written to the file at that
     * path.
     * @throws IllegalStateException If the body has already been requested.
     * @throws NullPointerException  If {@code path} is {@code null}.
     */
    default Future<?> bodyTo(final Path path) {
        return body().writeTo(path);
    }

    /**
     * Collects and then converts the individual list items of the incoming
     * message body using the provided {@code decoder}, which will
     * attempt to select an appropriate decoder function from any {@link
     * #codecType() codec} specified in the message.
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
     * @throws MessageCodecMisspecified If a codec is specified in the
     *                                  message, but it cannot be interpreted.
     * @throws MessageCodecUnspecified  If no codec is specified in this
     *                                  message.
     * @throws MessageCodecUnsupported  If the codec specified in the message
     *                                  is not supported by the given {@code
     *                                  decoder}.
     * @throws IllegalStateException    If the body has already been consumed.
     * @throws NullPointerException     If {@code decoder} is {@code null}.
     */
    default <T> Future<List<T>> bodyListItemsTo(final MultiDecoder<T> decoder) {
        return bodyListItemsTo(decoder, codecType().orElseThrow(() -> new MessageCodecUnspecified(this)));
    }

    /**
     * Collects and then converts the individual list items of the incoming
     * message body using the provided {@code decoder}, which will
     * attempt to select a decoder function named by {@code toCodecType}.
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
     * @throws MessageCodecUnsupported If the given codec is not supported by
     *                                 the given {@code decoder}.
     * @throws IllegalStateException   If the body has already been consumed.
     * @throws NullPointerException    If {@code decoder} or {@code
     *                                 toCodecType} is {@code null}.
     */
    default <T> Future<List<T>> bodyListItemsTo(
        final MultiDecoder<T> decoder,
        final ToCodecType toCodecType
    ) {
        if (toCodecType == null) {
            throw new NullPointerException("toCodecType");
        }
        return bodyTo(MultiDecoderForLists.of(decoder), toCodecType.toCodecType());
    }
}
