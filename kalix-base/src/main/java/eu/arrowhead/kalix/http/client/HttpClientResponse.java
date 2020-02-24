package eu.arrowhead.kalix.http.client;

import eu.arrowhead.kalix.http.HttpHeaders;
import eu.arrowhead.kalix.util.concurrent.Future;
import eu.arrowhead.kalix.http.HttpStatus;
import eu.arrowhead.kalix.http.HttpVersion;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * An incoming  HTTP response.
 */
public class HttpClientResponse {
    /**
     * Requests the body of the response to be serialized into an instance of
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

    /**
     * Gets value of named header, if set.
     *
     * @param name Name of header. Case is ignored.
     * @return Header value, if set.
     */
    public Optional<String> header(final String name) {
        return null;
    }

    /**
     * @return Map of all request headers.
     */
    public HttpHeaders headers() {
        return null;
    }

    /**
     * @return Information about the response sender.
     */
    public HttpResponder responder() {
        return null;
    }

    /**
     * @return Response {@link HttpStatus}.
     */
    public HttpStatus status() {
        return null;
    }

    /**
     * @return HTTP version used by request.
     */
    public HttpVersion version() {
        return null;
    }
}
