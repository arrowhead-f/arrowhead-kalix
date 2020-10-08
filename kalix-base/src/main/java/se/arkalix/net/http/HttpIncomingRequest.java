package se.arkalix.net.http;

import se.arkalix.net.http.service.HttpPattern;
import se.arkalix.util.InternalException;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An incoming HTTP request.
 */
@SuppressWarnings("unused")
public interface HttpIncomingRequest extends HttpIncoming {
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
        return Optional.ofNullable(values != null && values.size() > 0 ? values.get(0) : null);
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
     * Gets request URL data and assembles it into a {@link URI}.
     *
     * @return Request {@link URI}, if any.
     */
    default URI uri() {
        try {
            return new URI(null, null, path(), queryParameters()
                .entrySet()
                .stream()
                .flatMap(entry -> entry.getValue()
                    .stream()
                    .map(value -> urlEncode(entry.getKey()) + "=" + urlEncode(value)))
                .collect(Collectors.joining("&")), null);
        }
        catch (final URISyntaxException exception) {
            throw new InternalException("Failed to guarantee that " +
                "outgoing request uri is valid", exception);
        }
    }

    private String urlEncode(final String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }
}
