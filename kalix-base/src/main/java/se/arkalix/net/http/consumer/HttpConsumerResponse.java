package se.arkalix.net.http.consumer;

import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadable;
import se.arkalix.net.http.HttpHeaders;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.HttpVersion;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.net.http.client.HttpClientResponse;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.concurrent.FutureProgress;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * An incoming HTTP response, received from a {@link se.arkalix consumed
 * service}.
 */
@SuppressWarnings("unused")
public class HttpConsumerResponse implements HttpClientResponse {
    private final HttpClientResponse response;
    private final EncodingDescriptor encoding;

    HttpConsumerResponse(final HttpClientResponse response, final EncodingDescriptor encoding) {
        this.response = Objects.requireNonNull(response, "Expected response");
        this.encoding = Objects.requireNonNull(encoding, "Expected encoding");
    }

    /**
     * Requests that the incoming HTTP body be collected and parsed as an
     * instance of the provided {@code class_}.
     * <p>
     * Note that only so-called Data Transfer Object (DTO) types may be decoded
     * using this method. More details about such types can be read in the
     * documentation for the {@link se.arkalix.dto} package.
     * <p>
     * Note also that a body can typically only be requested once via this
     * interface. Any further requests will likely cause exceptions to be
     * thrown.
     * <p>
     * The body encoding is resolved automatically by the {@link HttpConsumer}
     * through which this response was received.
     *
     * @param class_ Class to decode incoming HTTP body into.
     * @param <R>    Type of {@code class_}.
     * @return Future completed when the incoming HTTP body has been fully
     * received and then decoded into an instance of {@code class_}.
     * @throws IllegalStateException If the body has already been requested.
     */
    public <R extends DtoReadable> FutureProgress<R> bodyAs(final Class<R> class_) {
        final var encoding0 = encoding.asDtoEncoding().orElseThrow(() ->
            new IllegalStateException("No DTO support is available for the " +
                "encoding \"" + encoding + "\"; receive the body as a string," +
                "byte array or stream and then decode it some other way"));
        return response.bodyAs(encoding0, class_);
    }

    @Override
    public <R extends DtoReadable> FutureProgress<R> bodyAs(final DtoEncoding encoding, final Class<R> class_) {
        return response.bodyAs(encoding, class_);
    }

    @Override
    public FutureProgress<byte[]> bodyAsByteArray() {
        return response.bodyAsByteArray();
    }

    /**
     * Requests that the incoming HTTP body be collected and parsed as a list
     * of instances of the provided {@code class_}.
     * <p>
     * Note that only so-called Data Transfer Object (DTO) types may be decoded
     * using this method. More details about such types can be read in the
     * documentation for the {@link se.arkalix.dto} package.
     * <p>
     * Note also that a body can typically only be requested once via this
     * interface. Any further requests will likely cause exceptions to be
     * thrown.
     * <p>
     * The body encoding is resolved automatically by the {@link HttpConsumer}
     * through which this response was received.
     *
     * @param class_ Class to decode list elements of incoming HTTP body into.
     * @param <R>    Type of {@code class_}.
     * @return Future completed when the incoming HTTP body has been fully
     * received and then decoded into instances of {@code class_}.
     * @throws IllegalStateException If the body has already been requested.
     */
    public <R extends DtoReadable> FutureProgress<List<R>> bodyAsList(final Class<R> class_) {
        final var encoding0 = encoding.asDtoEncoding().orElseThrow(() ->
            new IllegalStateException("No DTO support is available for the " +
                "encoding \"" + encoding + "\"; receive the body as a string," +
                "byte array or stream and then decode it some other way"));
        return response.bodyAsList(encoding0, class_);
    }

    @Override
    public <R extends DtoReadable> FutureProgress<List<R>> bodyAsList(
        final DtoEncoding encoding,
        final Class<R> class_)
    {
        return response.bodyAsList(encoding, class_);
    }

    @Override
    public FutureProgress<? extends InputStream> bodyAsStream() {
        return response.bodyAsStream();
    }

    @Override
    public FutureProgress<String> bodyAsString() {
        return response.bodyAsString();
    }

    @Override
    public FutureProgress<Path> bodyTo(final Path path, final boolean append) {
        return response.bodyTo(path, append);
    }

    /**
     * Retrieves the body of this response, if its status code is in the range
     * 200-299.
     * <p>
     * The body encoding is resolved automatically by the {@link HttpConsumer}
     * through which this response was received.
     *
     * @param class_ Class to decode incoming HTTP body into.
     * @param <R>    Type of {@code class_}.
     * @return Future completed immediately with an exception if the status
     * code is outside the success range, or when the incoming HTTP body has been
     * fully received and decoded into an instance of {@code class_}.
     * @throws IllegalStateException If the body has already been requested.
     */
    public <R extends DtoReadable> Future<R> bodyAsClassIfSuccess(final Class<R> class_) {
        if (status().isSuccess()) {
            return bodyAs(class_);
        }
        return Future.failure(reject());
    }

    @Override
    public HttpHeaders headers() {
        return response.headers();
    }

    /**
     * Gets the {@link HttpConsumerRequest#asClientRequest() client request}
     * associated with the {@link HttpConsumerRequest} that caused this
     * response to be retrieved.
     *
     * @return Client request associated with this response.
     */
    @Override
    public HttpClientRequest request() {
        return response.request();
    }

    @Override
    public HttpStatus status() {
        return response.status();
    }

    @Override
    public HttpVersion version() {
        return response.version();
    }
}
