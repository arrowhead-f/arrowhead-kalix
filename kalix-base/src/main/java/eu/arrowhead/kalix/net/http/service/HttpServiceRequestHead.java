package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.net.http.HttpVersion;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The head part of an incoming HTTP request.
 */
public interface HttpServiceRequestHead {
    /**
     * @return Encoding used to encode the body, if any, of this request.
     * Note that the encoding is a reflection of what Arrowhead service
     * interface was selected for the request rather than any specifics about
     * the request itself. This means that an encoding descriptor will be
     * available even if the request has no body.
     */
    EncodingDescriptor encoding();

    /**
     * Gets value of first header with given {@code name}, if any such.
     *
     * @param name Name of header. Case is ignored. Prefer lowercase.
     * @return Header value, or {@code null}.
     */
    default Optional<String> header(final String name) {
        return headers().get(name);
    }

    /**
     * Gets all header values associated with given {@code name}, if any.
     *
     * @param name Name of header. Case is ignored. Prefer lowercase.
     * @return Header values. May be an empty list.
     */
    default List<String> headers(final String name) {
        return headers().getAll(name);
    }

    /**
     * @return <i>Modifiable</i> map of all request headers.
     */
    HttpHeaders headers();

    /**
     * @return Request HTTP method.
     */
    HttpMethod method();

    /**
     * @return Request URL path. Will always start with a leading forward
     * slash ({@code /}).
     */
    String path();

    /**
     * Gets value of identified path parameter.
     * <p>
     * This operation accesses an arbitrary list that has exactly the same size
     * as the number of path parameters of the {@link HttpPattern} matched
     * prior to this request becoming available.
     * <p>
     * Note that it is possible to match a path parameter with an empty string.
     * It should never be assumed that a non-empty value returned by this
     * method has a length larger than 0.
     *
     * @param index Position of path parameter in original pattern.
     * @return Path parameter value, if any.
     * @throws IndexOutOfBoundsException If provided index is out of the bounds
     *                                   of the request path parameter list.
     */
    default String pathParameter(final int index) {
        return pathParameters().get(index);
    }

    /**
     * @return Unmodifiable list of all path parameters.
     */
    List<String> pathParameters();

    /**
     * Gets first query parameter with given name, if any such.
     *
     * @param name Name of query parameter. Case sensitive.
     * @return Query parameter value, if a corresponding parameter name exists.
     */
    default Optional<String> queryParameter(final String name) {
        final var values = queryParameters().get(name);
        return Optional.ofNullable(values.size() > 0 ? values.get(0) : null);
    }

    /**
     * Gets all query parameters with given name.
     *
     * @param name Name of query parameter. Case sensitive.
     * @return Unmodifiable list of query parameter values. May be empty.
     */
    default List<String> queryParameters(final String name) {
        final var parameters = queryParameters().get(name);
        if (parameters == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(parameters);
    }

    /**
     * @return Unmodifiable map of all query parameters.
     */
    Map<String, List<String>> queryParameters();

    /**
     * @return Information about the request sender.
     */
    HttpRequester requester();

    /**
     * @return HTTP version used by request.
     */
    HttpVersion version();
}
