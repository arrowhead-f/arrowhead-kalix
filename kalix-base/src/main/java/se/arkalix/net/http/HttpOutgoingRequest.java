package se.arkalix.net.http;

import se.arkalix.util.InternalException;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An outgoing HTTP request.
 *
 * @param <Self> Implementing class.
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public interface HttpOutgoingRequest<Self> extends HttpOutgoing<Self> {
    /**
     * @return Currently set HTTP method, if any.
     */
    Optional<HttpMethod> method();

    /**
     * Sets HTTP method. <b>Must be specified.</b>
     *
     * @param method Desired method.
     * @return This request.
     */
    Self method(final HttpMethod method);

    /**
     * Gets first query parameter with given name, if any such.
     *
     * @param name Name of query parameter. Case sensitive.
     * @return Query parameter value, if a corresponding parameter name exists.
     */
    default Optional<String> queryParameter(final String name) {
        final var values = queryParameters().get(name);
        return Optional.ofNullable(values != null && values.size() > 0 ? values.get(0) : null);
    }

    /**
     * Sets query parameter pair, replacing all previous such with the same
     * name.
     *
     * @param name  Name of query parameter. Case sensitive.
     * @param value Desired parameter value.
     * @return This request.
     */
    Self queryParameter(final String name, final Object value);

    /**
     * @return Modifiable map of query parameters.
     */
    Map<String, List<String>> queryParameters();

    /**
     * @return Currently set request path, if any.
     */
    Optional<String> path();

    /**
     * Sets absolute request path. <b>Must be specified.</b>
     * <p>
     * An absolute path must start with a forward slash and must not contain
     * any query parameters of fragment.
     *
     * @param path Desired absolute request path.
     * @return This request.
     * @throws IllegalArgumentException If {@code path} is both not {@code
     *                                  null} and not an absolute path.
     */
    Self path(final String path);

    /**
     * Gets any current request URL data and assembles it into a {@link URI}.
     * <p>
     * The method returns {@link Optional#empty()} only if no {@link
     * #path(String) path} it set.
     *
     * @return Current request {@link URI}, if any.
     */
    default Optional<URI> uri() {
        return path()
            .map(path -> {
                try {
                    return new URI(null, null, path, queryParameters()
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
            });
    }

    private String urlEncode(final String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }

    /**
     * Sets path and query parameters directly from given URI string.
     * <p>
     * Any other parts of the URI, such as scheme, authority or fragment, are
     * ignored.
     *
     * @param uri URI to set.
     * @return This request.
     */
    default Self uri(final String uri) {
        return uri(URI.create(uri));
    }

    /**
     * Sets path and query parameters directly from given {@link URI}.
     * <p>
     * Any other parts of the URI, such as scheme, authority or fragment, are
     * ignored.
     *
     * @param uri URI to set.
     * @return This request.
     */
    default Self uri(final URI uri) {
        queryParameters().clear();
        final var query = uri.getRawQuery();
        if (query != null) {
            for (final var pair : query.split("&")) {
                final var parts = pair.split("=", 2);
                queryParameter(urlDecode(parts[0]), urlDecode(parts[1]));
            }
        }
        final var path = uri.getPath();
        return path(path != null ? path : "/");
    }

    private String urlDecode(final String string) {
        return URLDecoder.decode(string, StandardCharsets.UTF_8);
    }
}
