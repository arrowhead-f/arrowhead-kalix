package eu.arrowhead.kalix.internal.net.http.client;

import eu.arrowhead.kalix.dto.DataEncoding;
import eu.arrowhead.kalix.dto.DataReadable;
import eu.arrowhead.kalix.internal.net.http.NettyHttpBodyReceiver;
import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpStatus;
import eu.arrowhead.kalix.net.http.HttpVersion;
import eu.arrowhead.kalix.net.http.client.HttpClientResponse;
import eu.arrowhead.kalix.util.annotation.Internal;
import eu.arrowhead.kalix.util.concurrent.FutureProgress;
import io.netty.handler.codec.http.HttpResponse;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;

import static eu.arrowhead.kalix.internal.net.http.NettyHttpAdapters.adapt;

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
    public <R extends DataReadable> FutureProgress<R> bodyAs(final DataEncoding encoding, final Class<R> class_) {
        return body.bodyAs(encoding, class_);
    }

    @Override
    public FutureProgress<byte[]> bodyAsByteArray() {
        return body.bodyAsByteArray();
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
            status = adapt(response.status());
        }
        return status;
    }

    @Override
    public HttpVersion version() {
        if (version == null) {
            version = adapt(response.protocolVersion());
        }
        return version;
    }
}
