package se.arkalix.net.http.service;

import se.arkalix.description.SystemIdentityDescription;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadable;
import se.arkalix.net.http.HttpHeaders;
import se.arkalix.net.http.HttpIncoming;
import se.arkalix.net.http.HttpMethod;
import se.arkalix.net.http.HttpVersion;
import se.arkalix.security.SecurityDisabled;
import se.arkalix.util.concurrent.FutureProgress;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The head and body of an incoming HTTP request.
 */
@SuppressWarnings("unused")
public interface HttpServiceRequest extends HttpIncoming {
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
     * @return Information about the Arrowhead system that sent this request.
     * @throws SecurityDisabled If the system providing the service receiving
     *                            this request is not running in {@link
     *                            se.arkalix.security secure mode}.
     */
    SystemIdentityDescription consumer();

    /**
     * @return HTTP version used by request.
     */
    HttpVersion version();

    /**
     * Creates a shallow copy of this {@code HttpServiceRequest} that contains
     * the given {@code pathParameters}.
     *
     * @param pathParameters Path parameters to include in request clone.
     * @return Clone of request object that includes given path parameters.
     */
    default HttpServiceRequest cloneAndSet(final List<String> pathParameters) {
        final var self = this;
        return new HttpServiceRequest() {
            @Override
            public Optional<Charset> charset() {
                return self.charset();
            }

            @Override
            public Optional<EncodingDescriptor> encoding() {
                return self.encoding();
            }

            @Override
            public <R extends DtoReadable> FutureProgress<R> bodyAs(
                final DtoEncoding encoding,
                final Class<R> class_)
            {
                return self.bodyAs(encoding, class_);
            }

            @Override
            public FutureProgress<byte[]> bodyAsByteArray() {
                return self.bodyAsByteArray();
            }

            @Override
            public <R extends DtoReadable> FutureProgress<List<R>> bodyAsList(final DtoEncoding encoding, final Class<R> class_) {
                return self.bodyAsList(encoding, class_);
            }

            @Override
            public FutureProgress<? extends InputStream> bodyAsStream() {
                return self.bodyAsStream();
            }

            @Override
            public FutureProgress<String> bodyAsString(final Charset charset) {
                return self.bodyAsString(charset);
            }

            @Override
            public FutureProgress<Path> bodyTo(final Path path, final boolean append) {
                return self.bodyTo(path, append);
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
            public SystemIdentityDescription consumer() {
                return self.consumer();
            }

            @Override
            public HttpVersion version() {
                return self.version();
            }
        };
    }
}
