package se.arkalix.net.http.service._internal;

import se.arkalix.net._internal.DefaultMessageOutgoing;
import se.arkalix.net.http.HttpHeaders;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.HttpVersion;
import se.arkalix.net.http._internal.NettyHttpHeaders;
import se.arkalix.net.http.service.HttpServiceResponse;
import se.arkalix.util.annotation.Internal;

import java.util.Optional;

@Internal
public class DefaultHttpServiceResponse
    extends DefaultMessageOutgoing<HttpServiceResponse>
    implements HttpServiceResponse
{
    private final HttpHeaders headers = new NettyHttpHeaders();

    private HttpStatus status = null;
    private HttpVersion version = null;

    @Override
    protected HttpServiceResponse self() {
        return this;
    }

    @Override
    public HttpServiceResponse header(final CharSequence name, final CharSequence value) {
        headers.set(name, value);
        return this;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    @Override
    public HttpServiceResponse clearHeaders() {
        headers.clear();
        return this;
    }

    @Override
    public Optional<HttpStatus> status() {
        return Optional.ofNullable(status);
    }

    @Override
    public HttpServiceResponse status(final HttpStatus status) {
        this.status = status;
        return this;
    }

    @Override
    public Optional<HttpVersion> version() {
        return Optional.ofNullable(version);
    }

    @Override
    public HttpServiceResponse version(final HttpVersion version) {
        this.version = version;
        return this;
    }
}
