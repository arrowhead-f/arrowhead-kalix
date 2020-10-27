package se.arkalix.net;

import se.arkalix.dto.DtoReadable;
import se.arkalix.util.concurrent.FutureProgress;

import java.io.InputStream;
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
     * Requests that the incoming message body be collected and parsed as an
     * instance of the provided {@code class_}.
     * <p>
     * An attempt will be made to automatically resolve a default DTO encoding.
     * If the attempt fails the method throws.
     * <p>
     * Note that only so-called Data Transfer Object (DTO) types may be decoded
     * using this method. More details about such types can be read in the
     * documentation for the {@link se.arkalix.dto} package.
     * <p>
     * Note also that a body can typically only be requested once via this
     * interface. Any further requests will likely cause exceptions to be
     * thrown.
     *
     * @param class_ Class to decode incoming message body into.
     * @param <R>    Type of {@code class_}.
     * @return Future completed when the incoming message body has been fully
     * received and then decoded into an instance of {@code class_}.
     * @throws MessageException      If no default encoding can be acquired.
     * @throws IllegalStateException If the body has already been requested.
     */
    default <R extends DtoReadable> FutureProgress<R> bodyAs(final Class<R> class_) {
        return bodyAs(encoding().orElseThrow(() -> new MessageEncodingUnspecified(this)), class_);
    }

    /**
     * Requests that the incoming message body be collected and parsed using
     * {@code encoding} as an instance of the provided {@code class_}.
     * <p>
     * Note that only so-called Data Transfer Object (DTO) types may be decoded
     * using this method. More details about such types can be read in the
     * documentation for the {@link se.arkalix.dto} package.
     * <p>
     * Note also that a body can typically only be requested once via this
     * interface. Any further requests will likely cause exceptions to be
     * thrown.
     *
     * @param encoding Encoding to use when decoding incoming message body.
     * @param class_   Class to decode incoming message body into.
     * @param <R>      Type of {@code class_}.
     * @return Future completed when the incoming message body has been fully
     * received and then decoded into an instance of {@code class_}.
     * @throws IllegalStateException If the body has already been requested.
     */
    <R extends DtoReadable> FutureProgress<R> bodyAs(final ToEncoding encoding, final Class<R> class_);

    /**
     * Requests that the incoming message body be collected into a regular Java
     * byte array ({@code byte[]}).
     * <p>
     * Note that a body can typically only be requested once via this
     * interface. Any further requests will likely cause exceptions to be
     * thrown.
     *
     * @return Future completed when the incoming message body has been fully
     * collected into a single byte array.
     * @throws IllegalStateException If the body has already been requested.
     */
    FutureProgress<byte[]> bodyAsByteArray();

    /**
     * Requests that the incoming message body be collected and parsed as a
     * list of instances of the provided {@code class_}.
     * <p>
     * An attempt will be made to automatically resolve a default DTO encoding.
     * If the attempt fails the method throws.
     * <p>
     * Note that only so-called Data Transfer Object (DTO) types may be decoded
     * using this method. More details about such types can be read in the
     * documentation for the {@link se.arkalix.dto} package.
     * <p>
     * Note also that a body can typically only be requested once via this
     * interface. Any further requests will likely cause exceptions to be
     * thrown.
     *
     * @param class_ Class to decode incoming message body into.
     * @param <R>    Type of {@code class_}.
     * @return Future completed when the incoming message body has been fully
     * received and then decoded into an instance of {@code class_}.
     * @throws MessageException      If no default encoding can be acquired.
     * @throws IllegalStateException If the body has already been requested.
     */
    default <R extends DtoReadable> FutureProgress<List<R>> bodyAsList(final Class<R> class_) {
        return bodyAsList(encoding().orElseThrow(() -> new MessageEncodingUnspecified(this)), class_);
    }

    /**
     * Requests that the incoming message body be collected and parsed using
     * {@code encoding} as a list of instances of the provided {@code class_}.
     * <p>
     * Note that only so-called Data Transfer Object (DTO) types may be decoded
     * using this method. More details about such types can be read in the
     * documentation for the {@link se.arkalix.dto} package.
     * <p>
     * Note also that a body can typically only be requested once via this
     * interface. Any further requests will likely cause exceptions to be
     * thrown.
     *
     * @param encoding Encoding to use when decoding incoming message body.
     * @param class_   Class to decode list elements of incoming message body
     *                 into.
     * @param <R>      Type of {@code class_}.
     * @return Future completed when the incoming message body has been fully
     * received and then decoded into instances of {@code class_}.
     * @throws IllegalStateException If the body has already been requested.
     */
    <R extends DtoReadable> FutureProgress<List<R>> bodyAsList(final ToEncoding encoding, final Class<R> class_);

    /**
     * Requests that the incoming message body be collected into a regular Java
     * {@code InputStream}.
     * <p>
     * The returned {@code Future} is not completed with the stream until the
     * incoming body has been received in full. However, in comparison to
     * {@link #bodyAsByteArray()}, this method does not necessitate copying the
     * body into a contiguous byte array, which in some cases can lead to
     * performance gains.
     * <p>
     * While it would be technically possible to return an {@code InputStream}
     * without waiting for the body to have arrived in full, reading from that
     * stream would require blocking the current thread until more of the body
     * arrives. That behavior does not rhyme well with the concurrency model of
     * the Kalix library, which tries to promote avoiding blocking I/O as far
     * as possible. If expecting to receive a very large incoming message body,
     * consider using the {@link #bodyTo(Path, boolean)} method, which writes
     * the body directly to a file, as it arrives.
     * <p>
     * Note that a body can typically only be requested once via this
     * interface. Any further requests will likely cause exceptions to be
     * thrown.
     *
     * @return Future completed when the incoming message body has been fully
     * collected and can be presented as an input stream.
     * @throws IllegalStateException If the body has already been requested.
     */
    FutureProgress<? extends InputStream> bodyAsStream();

    /**
     * Requests that the incoming message body be decoded into a regular Java
     * {@code String}.
     * <p>
     * If a character encoding supported by Java is {@link #encoding()}
     * specified in the message, it will be used when decoding the body into
     * a {@code String}. In any other case, UTF-8 will be assumed to be
     * adequate.
     * <p>
     * Note that a body can typically only be requested once via this
     * interface. Any further requests will likely cause exceptions to be
     * thrown.
     *
     * @return Future completed when the incoming message body becomes has been
     * fully collected into a {@code String}.
     * @throws IllegalStateException If the body has already been requested.
     */
    default FutureProgress<String> bodyAsString() {
        return bodyAsString(charset().orElse(StandardCharsets.UTF_8));
    }

    /**
     * Requests that the incoming message body be decoded into a regular Java
     * {@code String} using the specified character set.
     * <p>
     * Note that a body can typically only be requested once via this
     * interface. Any further requests will likely cause exceptions to be
     * thrown.
     *
     * @return Future completed when the incoming message body becomes has been
     * fully collected into a {@code String}.
     * @throws IllegalStateException If the body has already been requested.
     */
    FutureProgress<String> bodyAsString(final Charset charset);

    /**
     * Requests that the incoming message body be written to the file at the
     * specified file system path, overwriting it if it already exists.
     * <p>
     * Note that a body can typically only be requested once via this
     * interface. Any further requests will likely cause exceptions to be
     * thrown.
     *
     * @param path Path to file to contain incoming message body.
     * @return Future completed successfully with given path only if the
     * incoming message body is fully received and written to the file at that
     * path.
     * @throws IllegalStateException If the body has already been requested.
     * @see #bodyTo(Path, boolean)
     */
    default FutureProgress<Path> bodyTo(final Path path) {
        return bodyTo(path, false);
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
     * receiving data objects that are too large to realistically fit in
     * primary memory. This as received data is written directly to the target
     * file as it is received, rather than being buffered until all of it
     * becomes available.
     * <p>
     * Note that a body can typically only be requested once via this
     * interface. Any further requests will likely cause exceptions to be
     * thrown.
     *
     * @param path   Path to file to contain incoming message body.
     * @param append If {@code true}, any existing file at {@code path} will
     *               not be overwritten, but have the incoming message body
     *               appended to it.
     * @return Future completed successfully with given path only if the
     * incoming message body is fully received and written to the file at that
     * path.
     * @throws IllegalStateException If the body has already been requested.
     */
    FutureProgress<Path> bodyTo(final Path path, boolean append);
}
