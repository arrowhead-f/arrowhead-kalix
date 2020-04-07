package se.arkalix.net.http.service;

import se.arkalix.description.ConsumerDescription;
import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadable;
import se.arkalix.net.http.*;
import se.arkalix.util.concurrent.FutureProgress;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The head and body of an incoming HTTP request.
 */
public interface HttpServiceRequest extends HttpBodyReceiver {
    /**
     * Requests that the incoming HTTP body be collected and parsed, using an
     * automatically chosen encoding, as an instance of the provided
     * {@code class_}.
     * <p>
     * Note that only so-called Data Transfer Object (DTO) types may be decoded
     * using this method. More details about such types can be read in the
     * documentation for the {@link se.arkalix.dto} package.
     * <p>
     * Note also that a body can typically only be requested once via this
     * interface. Any further requests will likely cause exceptions to be
     * thrown.
     *
     * @param class_ Class to decode incoming HTTP body into.
     * @param <R>    Type of {@code class_}.
     * @return Future completed when the incoming HTTP body has been fully
     * received and then decoded into an instance of {@code class_}.
     * @throws IllegalStateException If the body has already been requested.
     */
    <R extends DtoReadable> FutureProgress<R> bodyAs(final Class<R> class_);

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
     */
    ConsumerDescription consumer();

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
            public <R extends DtoReadable> FutureProgress<R> bodyAs(final Class<R> class_) {
                return self.bodyAs(class_);
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
            public FutureProgress<? extends InputStream> bodyAsStream() {
                return self.bodyAsStream();
            }

            @Override
            public FutureProgress<String> bodyAsString() {
                return self.bodyAsString();
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
            public ConsumerDescription consumer() {
                return self.consumer();
            }

            @Override
            public HttpVersion version() {
                return self.version();
            }
        };
    }
}
