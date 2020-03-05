package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.dto.DataReadable;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.nio.file.Path;

/**
 * The body of an incoming HTTP request.
 */
public interface HttpServiceRequestBody {
    /**
     * Requests that the body of the HTTP request be collected into an instance
     * of the provided {@code class_}.
     * <p>
     * There are two major ways of using this method. The first way is to
     * provide the class of a Data Transfer Object (DTO) type, as described in
     * the {@link eu.arrowhead.kalix.dto} package. This strategy, essentially,
     * boils down to that the body is received in full and then serialized into
     * an instance of the specified type. The second way is to provide a
     * special data receiver class, which allows the response body to be
     * received as a raw byte array or be written directly to a file system
     * file, among other examples. All supported special data receiver classes
     * are listed in the {@link DataReadable} interface.
     *
     * @param class_ Desired class of collected request body.
     * @param <R>    Type of {@code class_}.
     * @return Future completed when request body becomes available for usage.
     */
    <R extends DataReadable> Future<? extends R> bodyAs(final Class<R> class_);

    /**
     * Requests that the body of the HTTP request be written to the file at the
     * specified file system path, overwriting it if it already exists.
     *
     * @param path Path to file to contain request body.
     * @return Future completed successfully with given path only if the
     * request body is fully received and written to the file at that path.
     */
    default Future<Path> bodyTo(final Path path) {
        return bodyTo(path, false);
    }

    /**
     * Requests that the body of the HTTP request be written to the file at the
     * specified file system path.
     * <p>
     * The file will be created if it does not exist. If the {@code append}
     * parameter is provided as  {@code true}, the file is appended to rather
     * than overwritten.
     *
     * @param path   Path to file to contain request body.
     * @param append If {@code true}, any existing file at {@code path} will
     *               not be overwritten, but have the contents of the HTTP
     *               request appended to it.
     * @return Future completed successfully with given path only if the
     * request body is fully received and written to the file at that path.
     */
    Future<Path> bodyTo(final Path path, boolean append);
}
