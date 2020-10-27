package se.arkalix.net.http.consumer._internal;

import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadable;
import se.arkalix.net.http.HttpHeaders;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.HttpVersion;
import se.arkalix.net.http.client.HttpClientResponse;
import se.arkalix.net.http.consumer.HttpConsumerConnection;
import se.arkalix.net.http.consumer.HttpConsumerRequest;
import se.arkalix.net.http.consumer.HttpConsumerResponse;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.FutureProgress;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Internal
public class DefaultHttpConsumerResponse implements HttpConsumerResponse {
    private final HttpConsumerConnection connection;
    private final HttpConsumerRequest request;
    private final HttpClientResponse inner;

    public DefaultHttpConsumerResponse(
        final HttpConsumerConnection connection,
        final HttpConsumerRequest request,
        final HttpClientResponse inner
    ) {
        this.connection = Objects.requireNonNull(connection, "connection");
        this.request = Objects.requireNonNull(request, "request");
        this.inner = inner;
    }

    @Override
    public HttpHeaders headers() {
        return inner.headers();
    }

    @Override
    public DefaultHttpConsumerResponse clearHeaders() {
        inner.headers().clear();
        return this;
    }

    @Override
    public HttpConsumerRequest request() {
        return request;
    }

    @Override
    public HttpStatus status() {
        return inner.status();
    }

    @Override
    public HttpVersion version() {
        return inner.version();
    }

    @Override
    public HttpConsumerConnection connection() {
        return connection;
    }

    @Override
    public <R extends DtoReadable> FutureProgress<R> bodyAs(
        final DtoEncoding encoding, final Class<R> class_
    ) {
        return inner.bodyAs(encoding, class_);
    }

    @Override
    public FutureProgress<byte[]> bodyAsByteArray() {
        return inner.bodyAsByteArray();
    }

    @Override
    public <R extends DtoReadable> FutureProgress<List<R>> bodyAsList(
        final DtoEncoding encoding, final Class<R> class_
    ) {
        return inner.bodyAsList(encoding, class_);
    }

    @Override
    public FutureProgress<? extends InputStream> bodyAsStream() {
        return inner.bodyAsStream();
    }

    @Override
    public FutureProgress<String> bodyAsString(final Charset charset) {
        return inner.bodyAsString(charset);
    }

    @Override
    public FutureProgress<Path> bodyTo(final Path path, final boolean append) {
        return inner.bodyTo(path, append);
    }
}
