package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpVersion;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * An incoming HTTP request, handled by a {@link HttpService}.
 */
public class HttpServiceRequest extends HttpServiceRequestHead {
    /**
     * Requests the body of the request to be serialized into an instance of
     * given target class.
     * <p>
     * As the body may not have been fully received when this method is called,
     * a {@link Future} is returned whose {@link Future#onResult(Consumer)}
     * method will be called when the body is either ready or it is known that
     * it cannot be received and/or serialized.
     *
     * @param class_ Class to serialize request body into.
     * @param <C>    Type of {@code class_}.
     * @return Future of serialized request body.
     */
    public <C> Future<C> bodyAs(final Class<C> class_) {
        return null;
    }
}
