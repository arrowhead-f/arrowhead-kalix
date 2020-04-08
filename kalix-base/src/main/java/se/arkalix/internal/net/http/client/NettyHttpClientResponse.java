package se.arkalix.internal.net.http.client;

import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadable;
import se.arkalix.internal.net.http.NettyHttpBodyReceiver;
import se.arkalix.net.http.HttpHeaders;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.HttpVersion;
import se.arkalix.net.http.client.HttpClientResponse;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.FutureProgress;
import io.netty.handler.codec.http.HttpResponse;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static se.arkalix.internal.net.http.NettyHttpConverters.convert;

@Internal
public class NettyHttpClientResponse implements HttpClientResponse {
    private final NettyHttpBodyReceiver body;
    private final HttpResponse response;

    private HttpHeaders headers = null;
    private HttpStatus status = null;
    private HttpVersion version = null;

    public NettyHttpClientResponse(final NettyHttpBodyReceiver body, final HttpResponse response) {
        this.body = Objects.requireNonNull(body, "Expected body");
        this.response = Objects.requireNonNull(response, "Expected response");
    }

    @Override
    public <R extends DtoReadable> FutureProgress<R> bodyAs(final DtoEncoding encoding, final Class<R> class_) {
        return body.bodyAs(encoding, class_);
    }

    @Override
    public FutureProgress<byte[]> bodyAsByteArray() {
        return body.bodyAsByteArray();
    }

    @Override
    public <R extends DtoReadable> FutureProgress<List<R>> bodyAsList(
        final DtoEncoding encoding,
        final Class<R> class_)
    {
        return body.bodyAsList(encoding, class_);
    }

    @Override
    public FutureProgress<? extends InputStream> bodyAsStream() {
        return body.bodyAsStream();
    }

    @Override
    public FutureProgress<String> bodyAsString() {
        return body.bodyAsString();
    }

    @Override
    public FutureProgress<Path> bodyTo(final Path path, final boolean append) {
        return body.bodyTo(path, append);
    }

    @Override
    public HttpHeaders headers() {
        if (headers == null) {
            headers = new HttpHeaders(response.headers());
        }
        return headers;
    }

    @Override
    public HttpStatus status() {
        if (status == null) {
            status = convert(response.status());
        }
        return status;
    }

    @Override
    public HttpVersion version() {
        if (version == null) {
            version = convert(response.protocolVersion());
        }
        return version;
    }
}
