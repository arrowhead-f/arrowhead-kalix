package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.dto.DataReadable;
import eu.arrowhead.kalix.util.concurrent.FutureProgress;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * The body of an incoming HTTP request.
 */
public interface HttpServiceRequestBody {
    /**
     * Requests that the body of the HTTP request be collected into an instance
     * of the provided {@code class_}.
     * <p>
     * Note that only so-called Data Transfer Object (DTO) types may be decoded
     * using this method. More details about such types can be read in the
     * documentation for the {@link eu.arrowhead.kalix.dto} package.
     *
     * @param class_ Class to decode request body into.
     * @param <R>    Type of {@code class_}.
     * @return Future completed when the request body has been fully received
     * and then decoded into an instance of {@code class_}.
     * @throws IllegalStateException If more than one method with a name
     *                               starting with "body" is called.
     */
    <R extends DataReadable> FutureProgress<? extends R> bodyAs(final Class<R> class_);

    /**
     * Requests that the body of the HTTP request be collected into a regular
     * Java byte array ({@code byte[]}).
     *
     * @return Future completed when the request body has been fully collected
     * into a single byte array.
     * @throws IllegalStateException If more than one method with a name
     *                               starting with "body" is called.
     */
    FutureProgress<byte[]> bodyAsByteArray();

    /**
     * Requests that the body of the HTTP request be collected into a regular
     * Java {@code InputStream}.
     * <p>
     * The returned {@code Future} is not completed with the stream until the
     * request body has been received in full. However, in comparison to
     * {@link #bodyAsByteArray()}, this method does not necessitate copying the
     * request body into a contiguous byte array, which in some cases can lead
     * to performance gains.
     * <p>
     * While it is technically possible to return an {@code InputStream}
     * without waiting for the body to have arrived in full, reading from that
     * stream would require blocking the current thread until more of the body
     * arrives. That behavior does rhyme well with the concurrency model of the
     * Kalix library, which tries to promote avoiding blocking I/O as far as
     * possible. If expecting to receive a very large request body, consider
     * using the {@link #bodyTo(Path, boolean)} method, which writes the body
     * directly to a file, as it arrives.
     *
     * @return Future completed when the request body has been fully
     * collected and can be presented as an input stream.
     * @throws IllegalStateException If more than one method with a name
     *                               starting with "body" is called.
     */
    FutureProgress<? extends InputStream> bodyAsStream();

    /**
     * Requests that the body of the HTTP request be collected into a regular
     * Java {@code String}.
     * <p>
     * If a character encoding supported by Java is mentioned in the
     * {@code "content-type"} of the request, that will be used when decoding
     * the body into a string. In any other case, UTF-8 will be assumed to be
     * adequate.
     *
     * @return Future completed when the request body becomes has been fully
     * collected into a string.
     * @throws IllegalStateException If more than one method with a name
     *                               starting with "body" is called.
     */
    FutureProgress<String> bodyAsString();

    /**
     * Requests that the body of the HTTP request be written to the file at the
     * specified file system path, overwriting it if it already exists.
     *
     * @param path Path to file to contain request body.
     * @return Future completed successfully with given path only if the
     * request body is fully received and written to the file at that path.
     * @see #bodyTo(Path, boolean)
     */
    default FutureProgress<Path> bodyTo(final Path path) {
        return bodyTo(path, false);
    }

    /**
     * Requests that the body of the HTTP request be written to the file at the
     * specified file system path.
     * <p>
     * The file will be created if it does not exist. If the {@code append}
     * parameter is {@code true}, the file is appended to rather than
     * overwritten.
     *
     * Using this method is the preferred way of receiving larger data objects,
     * as it does not require buffering all of it in
     *
     * @param path   Path to file to contain request body.
     * @param append If {@code true}, any existing file at {@code path} will
     *               not be overwritten, but have the body of the HTTP request
     *               appended to it.
     * @return Future completed successfully with given path only if the
     * request body is fully received and written to the file at that path.
     */
    FutureProgress<Path> bodyTo(final Path path, boolean append);
}
