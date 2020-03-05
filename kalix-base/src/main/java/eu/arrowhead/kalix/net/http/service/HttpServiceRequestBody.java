package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.dto.DataReadable;
import eu.arrowhead.kalix.util.concurrent.Future;

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
    <R extends DataReadable> Future<R> bodyAs(final Class<R> class_);
}
