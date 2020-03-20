package se.arkalix.http;

import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadable;
import se.arkalix.util.concurrent.FutureProgress;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * Some handler useful for receiving an incoming HTTP body.
 */
public interface HttpBodyReceiver {
    /**
     * Requests that the incoming HTTP body be collected and parsed as an
     * instance of the provided {@code class_}.
     * <p>
     * Note that only so-called Data Transfer Object (DTO) types may be decoded
     * using this method. More details about such types can be read in the
     * documentation for the {@link se.arkalix.dto} package.
     * <p>
     * Note also that a body can typically only be requested once via this
     * interface. Any further requests will likely cause exceptions to be
     * thrown.
     *
     * @param encoding Encoding to use when decoding incoming HTTP body.
     * @param class_   Class to decode incoming HTTP body into.
     * @param <R>      Type of {@code class_}.
     * @return Future completed when the incoming HTTP body has been fully
     * received and then decoded into an instance of {@code class_}.
     * @throws IllegalStateException If the body has already been requested.
     */
    <R extends DtoReadable> FutureProgress<R> bodyAs(final DtoEncoding encoding, final Class<R> class_);

    /**
     * Requests that the incoming HTTP body be collected into a regular Java
     * byte array ({@code byte[]}).
     * <p>
     * Note that a body can typically only be requested once via this
     * interface. Any further requests will likely cause exceptions to be
     * thrown.
     *
     * @return Future completed when the incoming HTTP body has been fully
     * collected into a single byte array.
     * @throws IllegalStateException If the body has already been requested.
     */
    FutureProgress<byte[]> bodyAsByteArray();

    /**
     * Requests that the incoming HTTP body be collected into a regular Java
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
     * as possible. If expecting to receive a very large incoming HTTP body,
     * consider using the {@link #bodyTo(Path, boolean)} method, which writes
     * the body directly to a file, as it arrives.
     * <p>
     * Note that a body can typically only be requested once via this
     * interface. Any further requests will likely cause exceptions to be
     * thrown.
     *
     * @return Future completed when the incoming HTTP body has been fully
     * collected and can be presented as an input stream.
     * @throws IllegalStateException If the body has already been requested.
     */
    FutureProgress<? extends InputStream> bodyAsStream();

    /**
     * Requests that the incoming HTTP body be collected into a regular Java
     * {@code String}.
     * <p>
     * If a character encoding supported by Java is mentioned in the
     * {@code "content-type"} of the HTTP message containing the body,
     * that will be used when decoding the body into a string. In any other
     * case, UTF-8 will be assumed to be adequate.
     * <p>
     * Note that a body can typically only be requested once via this
     * interface. Any further requests will likely cause exceptions to be
     * thrown.
     *
     * @return Future completed when the incoming HTTP body becomes has been
     * fully collected into a string.
     * @throws IllegalStateException If the body has already been requested.
     */
    FutureProgress<String> bodyAsString();

    /**
     * Requests that the incoming HTTP body be written to the file at the
     * specified file system path, overwriting it if it already exists.
     * <p>
     * Note that a body can typically only be requested once via this
     * interface. Any further requests will likely cause exceptions to be
     * thrown.
     *
     * @param path Path to file to contain incoming HTTP body.
     * @return Future completed successfully with given path only if the
     * incoming HTTP body is fully received and written to the file at that path.
     * @throws IllegalStateException If the body has already been requested.
     * @see #bodyTo(Path, boolean)
     */
    default FutureProgress<Path> bodyTo(final Path path) {
        return bodyTo(path, false);
    }

    /**
     * Requests that the incoming HTTP body be written to the file at the
     * specified file system path.
     * <p>
     * The file will be created if it does not exist. If the {@code append}
     * parameter is {@code true}, the file is appended to rather than
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
     * @param path   Path to file to contain incoming HTTP body.
     * @param append If {@code true}, any existing file at {@code path} will
     *               not be overwritten, but have the incoming HTTP body
     *               appended to it.
     * @return Future completed successfully with given path only if the
     * incoming HTTP body is fully received and written to the file at that path.
     * @throws IllegalStateException If the body has already been requested.
     */
    FutureProgress<Path> bodyTo(final Path path, boolean append);
}
