package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.net.http.HttpPeer;
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
    default Optional<String> header(final CharSequence name) {
        return headers().get(name);
    }

    /**
     * Gets all header values associated with given {@code name}, if any.
     *
     * @param name Name of header. Case is ignored. Prefer lowercase.
     * @return Header values. May be an empty list.
     */
    default List<String> headers(final CharSequence name) {
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
    default Optional<String> queryParameter(final CharSequence name) {
        final var values = queryParameters().get(name.toString());
        return Optional.ofNullable(values.size() > 0 ? values.get(0) : null);
    }

    /**
     * Gets all query parameters with given name.
     *
     * @param name Name of query parameter. Case sensitive.
     * @return Unmodifiable list of query parameter values. May be empty.
     */
    default List<String> queryParameters(final CharSequence name) {
        final var parameters = queryParameters().get(name.toString());
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
    HttpPeer requester();

    /**
     * @return HTTP version used by request.
     */
    HttpVersion version();

    /**
     * Creates a shallow copy of this {@code HttpServiceRequestHead} that
     * contains the given {@code pathParameters}.
     *
     * @param pathParameters Path parameters to include in request copy.
     * @return Copy of this object that includes given path parameters.
     */
    default HttpServiceRequestHead newWithPathParameters(final List<String> pathParameters) {
        final var self = this;
        return new HttpServiceRequestHead() {
            @Override
            public EncodingDescriptor encoding() {
                return self.encoding();
            }

            @Override
            public HttpHeaders headers() {
                return self.headers();
            }

            @Override
            public HttpMethod method() {
                return self.method();
            }

            @Override
            public String path() {
                return self.path();
            }

            @Override
            public List<String> pathParameters() {
                return pathParameters;
            }

            @Override
            public Map<String, List<String>> queryParameters() {
                return self.queryParameters();
            }

            @Override
            public HttpPeer requester() {
                return self.requester();
            }

            @Override
            public HttpVersion version() {
                return self.version();
            }
        };
    }
}
