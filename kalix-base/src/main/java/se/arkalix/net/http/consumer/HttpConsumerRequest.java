package se.arkalix.net.http.consumer;

import se.arkalix.net.Encoding;
import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoWritable;
import se.arkalix.net.MessageOutgoingWithImplicitEncoding;
import se.arkalix.net.http.HttpHeaders;
import se.arkalix.net.http.HttpMethod;
import se.arkalix.net.http.HttpOutgoingRequest;
import se.arkalix.net.http.HttpVersion;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.util.annotation.Internal;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An HTTP request that can be sent to an HTTP server via an {@link
 * HttpConsumer}.
 */
@SuppressWarnings("unused")
public class HttpConsumerRequest
    implements HttpOutgoingRequest<HttpConsumerRequest>, MessageOutgoingWithImplicitEncoding<HttpConsumerRequest>
{
    private final HttpClientRequest inner = new HttpClientRequest();

    @Override
    public Optional<HttpMethod> method() {
        return inner.method();
    }

    @Override
    public HttpConsumerRequest method(final HttpMethod method) {
        inner.method(method);
        return this;
    }

    @Override
    public HttpConsumerRequest queryParameter(final String name, final Object value) {
        inner.queryParameter(name, value);
        return this;
    }

    @Override
    public Map<String, List<String>> queryParameters() {
        return inner.queryParameters();
    }

    @Override
    public Optional<String> path() {
        return inner.path();
    }

    @Override
    public HttpConsumerRequest path(final String path) {
        inner.path(path);
        return this;
    }

    @Override
    public HttpConsumerRequest header(final CharSequence name, final CharSequence value) {
        inner.header(name, value);
        return this;
    }

    @Override
    public Optional<HttpVersion> version() {
        return inner.version();
    }

    @Override
    public HttpConsumerRequest version(final HttpVersion version) {
        inner.version(version);
        return this;
    }

    @Override
    public Optional<Object> body() {
        return inner.body();
    }

    @Override
    public HttpConsumerRequest body(final byte[] byteArray) {
        inner.body(byteArray);
        return this;
    }

    @Override
    public HttpConsumerRequest body(final DtoEncoding encoding, final DtoWritable data) {
        inner.body(encoding, data);
        return this;
    }

    @Override
    public <L extends List<? extends DtoWritable>> HttpConsumerRequest body(final DtoEncoding encoding, final L data) {
        inner.body(encoding, data);
        return this;
    }

    @Override
    public HttpConsumerRequest body(final Path path) {
        inner.body(path);
        return this;
    }

    @Override
    public HttpConsumerRequest body(final Charset charset, final String string) {
        inner.body(charset, string);
        return this;
    }

    @Override
    public HttpConsumerRequest clearBody() {
        inner.clearBody();
        return this;
    }

    @Override
    public HttpHeaders headers() {
        return inner.headers();
    }

    @Override
    public HttpConsumerRequest clearHeaders() {
        inner.clearHeaders();
        return this;
    }

    @Override
    public Optional<Charset> charset() {
        return inner.charset();
    }

    @Override
    public Optional<Encoding> encoding() {
        return inner.encoding();
    }

    /**
     * <i>Internal API</i>. Might change in breaking ways between patch
     * versions of the Kalix library. Use is not advised.
     */
    @Internal
    public HttpClientRequest unwrap() {
        return inner;
    }
}
