package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.dto.DataReadable;
import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.net.http.HttpVersion;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * An incoming HTTP request, handled by a {@link HttpService}.
 */
public interface HttpServiceRequestFull extends HttpServiceRequestHead {
    /**
     * Requests that the body of the HTTP request to be serialized into an
     * instance of the specified data transfer object type.
     * <p>
     * As the body may not have been fully received when this method is called,
     * a {@link Future} is returned whose {@link Future#onResult(Consumer)}
     * method will be called when the body is either ready or it is known that
     * it cannot be received and/or decoded.
     *
     * @param class_ Class to serialize request body into.
     * @param <R>    Type of {@code class_}.
     * @return Future of serialized request body.
     */
    <R extends DataReadable> Future<R> bodyAs(final Class<R> class_);

    /**
     * Requests that the body of the HTTP request to be collected into a byte
     * array.
     * <p>
     * As the body may not have been fully received when this method is called,
     * a {@link Future} is returned whose {@link Future#onResult(Consumer)}
     * method will be called when the body is either ready or it is known that
     * it cannot be received and/or serialized.
     *
     * @return Future of byte array request body.
     */
    Future<byte[]> bodyAsBytes();

    /**
     * Requests for the body of the HTTP request to be readable, as it becomes
     * available, via an {@link InputStream}.
     *
     * @return Input stream.
     */
    InputStream bodyAsStream();

    /**
     * Requests that the body of the HTTP request to be collected into a
     * string.
     * <p>
     * As the body may not have been fully received when this method is called,
     * a {@link Future} is returned whose {@link Future#onResult(Consumer)}
     * method will be called when the body is either ready or it is known that
     * it cannot be received and/or serialized.
     *
     * @return Future of string request body.
     */
    Future<String> bodyAsString();

    /**
     * Requests that the body of the HTTP request be written into the file at
     * the specified file system path.
     * <p>
     * The file will be created if it does not exist, and appended to if it
     * does exist.
     *
     * @param path Path to file to contain request body.
     * @return Future completed successfully only when request body has been
     * fully received and written to the file at the designated path.
     */
    Future<?> bodyToPath(final Path path);
}
