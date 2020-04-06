package se.arkalix.net.http.consumer;

import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoWritable;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.net.http.HttpBodySender;
import se.arkalix.net.http.HttpHeaders;
import se.arkalix.net.http.HttpMethod;
import se.arkalix.net.http.HttpVersion;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.util.annotation.Internal;

import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

/**
 * An outgoing HTTP request, to be sent to a consumed service.
 */
public class HttpConsumerRequest implements HttpBodySender<HttpConsumerRequest> {
    private final HttpClientRequest request = new HttpClientRequest();

    @Override
    public Optional<Object> body() {
        return request.body();
    }

    @Override
    public HttpConsumerRequest body(final byte[] byteArray) {
        request.body(byteArray);
        return this;
    }

    /**
     * Sets outgoing HTTP body, replacing any previously set such.
     * <p>
     * The provided writable data transfer object is scheduled for encoding and
     * transmission to the receiver of the body. Please refer to the Javadoc
     * for the {@code @DtoWritableAs} annotation for more information about
     * writable data transfer objects.
     * <p>
     * This particular method differs from {@link
     * #body(DtoEncoding, DtoWritable)} in that the {@link DtoEncoding} is
     * selected automatically from those supported by both the
     * {@link HttpConsumer} sending the request and the service it is used to
     * consume.
     *
     * @param data Data transfer object to send to consumed service.
     * @return This.
     * @throws NullPointerException If {@code body} is {@code null}.
     * @see DtoWritableAs @DtoWritableAs
     */
    public HttpConsumerRequest body(final DtoWritable data) {
        return body(null, data);
    }

    @Override
    public HttpConsumerRequest body(final DtoEncoding encoding, final DtoWritable data) {
        request.body(encoding, data);
        return this;
    }

    @Override
    public HttpConsumerRequest body(final Path path) {
        request.body(path);
        return this;
    }

    @Override
    public HttpConsumerRequest body(final String string) {
        request.body(string);
        return this;
    }

    @Override
    public HttpConsumerRequest clearBody() {
        request.clearBody();
        return this;
    }

    /**
     * @return Encoding set with the most recent call to
     * {@link #body(DtoEncoding, DtoWritable)}, if any.
     */
    public Optional<DtoEncoding> encoding() {
        return request.encoding();
    }

    /**
     * Gets value of first header with given {@code name}, if any such.
     *
     * @param name Name of header. Case is ignored. Prefer lowercase.
     * @return Header value, or {@code null}.
     */
    public Optional<String> header(final CharSequence name) {
        return request.header(name);
    }

    /**
     * Sets header with {@code name} to given value.
     *
     * @param name  Name of header. Case is ignored. Prefer lowercase.
     * @param value Desired header value.
     * @return This request.
     */
    public HttpConsumerRequest header(final CharSequence name, final CharSequence value) {
        request.header(name, value);
        return this;
    }

    /**
     * Gets all header values associated with given {@code name}, if any.
     *
     * @param name Name of header. Case is ignored. Prefer lowercase.
     * @return Header values. May be an empty list.
     */
    public List<String> headers(final CharSequence name) {
        return request.headers(name);
    }

    /**
     * @return <i>Modifiable</i> map of all request headers.
     */
    public HttpHeaders headers() {
        return request.headers();
    }

    /**
     * @return Currently set HTTP method, if any.
     */
    public Optional<HttpMethod> method() {
        return request.method();
    }

    /**
     * Sets HTTP method. <b>Must be specified.</b>
     *
     * @param method Desired method.
     * @return This request.
     */
    public HttpConsumerRequest method(final HttpMethod method) {
        request.method(method);
        return this;
    }

    /**
     * Gets first query parameter with given name, if any such.
     *
     * @param name Name of query parameter. Case sensitive.
     * @return Query parameter value, if a corresponding parameter name exists.
     */
    public Optional<String> queryParameter(final String name) {
        return request.queryParameter(name);
    }

    /**
     * Sets query parameter pair, replacing all previous such with the same
     * name.
     *
     * @param name  Name of query parameter. Case sensitive.
     * @param value Desired parameter value.
     * @return Query parameter value, if a corresponding parameter name exists.
     */
    public HttpConsumerRequest queryParameter(final String name, final CharSequence value) {
        request.queryParameter(name, value);
        return this;
    }

    /**
     * @return Modifiable map of query parameters.
     */
    public Map<String, List<String>> queryParameters() {
        return request.queryParameters();
    }

    /**
     * @return Currently set request URI, if any.
     */
    public Optional<String> uri() {
        return request.uri();
    }

    /**
     * Sets request URI. <b>Must be specified.</b>
     *
     * @param uri Desired URI.
     * @return This request.
     */
    public HttpConsumerRequest uri(final String uri) {
        request.uri(uri);
        return this;
    }

    /**
     * Sets request URI. <b>Must be specified.</b>
     *
     * @param uri Desired URI.
     * @return This request.
     */
    public HttpConsumerRequest uri(final URI uri) {
        return uri(uri.toString());
    }

    /**
     * @return Currently set HTTP version, if any.
     */
    public Optional<HttpVersion> version() {
        return request.version();
    }

    /**
     * Sets HTTP version.
     *
     * @param version Desired HTTP version.
     * @return This request.
     */
    public HttpConsumerRequest version(final HttpVersion version) {
        request.version(version);
        return this;
    }

    /**
     * @return This request as an {@link HttpClientRequest}.
     */
    public HttpClientRequest asClientRequest() {
        return request;
    }

    @Internal
    void setEncodingIfRequired(final Supplier<DtoEncoding> encoding) {
        if (encoding().isPresent()) {
            return;
        }
        final var body = request.body().orElse(null);
        if (body instanceof DtoWritable) {
            request.body(encoding.get(), (DtoWritable) body);
        }
    }
}
