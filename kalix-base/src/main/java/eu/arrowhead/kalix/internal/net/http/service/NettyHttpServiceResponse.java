package eu.arrowhead.kalix.internal.net.http.service;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.dto.DataEncoding;
import eu.arrowhead.kalix.dto.DataWritable;
import eu.arrowhead.kalix.dto.DataWriter;
import eu.arrowhead.kalix.dto.WriteException;
import eu.arrowhead.kalix.internal.dto.binary.ByteBufWriter;
import eu.arrowhead.kalix.internal.net.http.HttpMediaTypes;
import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpStatus;
import eu.arrowhead.kalix.net.http.HttpVersion;
import eu.arrowhead.kalix.net.http.service.HttpServiceResponse;
import eu.arrowhead.kalix.util.annotation.Internal;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

import static eu.arrowhead.kalix.internal.net.http.NettyHttpAdapters.adapt;
import static io.netty.handler.codec.http.HttpHeaderNames.*;

@Internal
public class NettyHttpServiceResponse implements HttpServiceResponse {
    private final EncodingDescriptor encoding;
    private final HttpRequest request;
    private final io.netty.handler.codec.http.HttpHeaders nettyHeaders;

    private Object body = null;
    private DataEncoding dataEncoding = null;
    private HttpHeaders headers = null;
    private HttpStatus status = null;
    private HttpVersion version = null;

    public NettyHttpServiceResponse(
        final HttpRequest request,
        final io.netty.handler.codec.http.HttpHeaders headers,
        final EncodingDescriptor encoding)
    {
        this.encoding = encoding;
        this.nettyHeaders = headers;
        this.request = request;
    }

    public ChannelFuture write(final Channel channel)
        throws WriteException, IOException
    {
        final var nettyStatus = adapt(status);
        final var nettyVersion = request.protocolVersion();

        final ByteBuf content;
        if (body == null) {
            content = Unpooled.EMPTY_BUFFER;
        }
        else if (body instanceof byte[]) {
            content = Unpooled.wrappedBuffer((byte[]) body);
        }
        else if (body instanceof DataWritable) {
            if (dataEncoding == null) {
                dataEncoding = encoding.asDataEncoding().orElseThrow(() -> new UnsupportedOperationException("" +
                    "There is no DTO support for the \"" + encoding +
                    "\" encoding; response body cannot be encoded"));
            }
            content = channel.alloc().buffer();
            DataWriter.write((DataWritable) body, dataEncoding, new ByteBufWriter(content));
        }
        else if (body instanceof Path) {
            final var file = new RandomAccessFile(((Path) body).toFile(), "r");
            final var length = file.length();

            nettyHeaders.set(CONTENT_LENGTH, length);

            channel.write(new DefaultHttpResponse(nettyVersion, nettyStatus, nettyHeaders));
            channel.write(new DefaultFileRegion(file.getChannel(), 0, length));
            return channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        }
        else if (body instanceof String) {
            final var charset = HttpUtil.getCharset(nettyHeaders.get("content-type"), StandardCharsets.UTF_8);
            content = Unpooled.wrappedBuffer(((String) body).getBytes(charset));
        }
        else {
            throw new IllegalStateException("Invalid response body supplied \"" + body + "\"");
        }
        nettyHeaders.set(CONTENT_LENGTH, content.readableBytes());

        if (!nettyHeaders.contains(CONTENT_TYPE)) {
            nettyHeaders.set(CONTENT_TYPE, HttpMediaTypes.toMediaType(encoding));
        }

        return channel.writeAndFlush(new DefaultFullHttpResponse(nettyVersion, nettyStatus, content, nettyHeaders,
            EmptyHttpHeaders.INSTANCE));
    }

    @Override
    public Optional<Object> body() {
        return Optional.ofNullable(body);
    }

    @Override
    public HttpServiceResponse body(final byte[] byteArray) {
        body = byteArray;
        return this;
    }

    @Override
    public HttpServiceResponse body(final DataWritable data) {
        body = data;
        return this;
    }

    @Override
    public HttpServiceResponse body(final DataEncoding encoding, final DataWritable data) {
        dataEncoding = encoding;
        body = data;
        return null;
    }

    @Override
    public HttpServiceResponse body(final Path path) {
        body = path;
        return this;
    }

    @Override
    public HttpServiceResponse body(final String string) {
        body = string;
        return this;
    }

    @Override
    public HttpServiceResponse clearBody() {
        body = null;
        return this;
    }

    @Override
    public HttpServiceResponse clearHeaders() {
        nettyHeaders.clear();
        return this;
    }

    @Override
    public Optional<String> header(final CharSequence name) {
        return Optional.ofNullable(nettyHeaders.get(name));
    }

    @Override
    public HttpServiceResponse header(final CharSequence name, final CharSequence value) {
        nettyHeaders.set(name, value);
        return this;
    }

    @Override
    public HttpHeaders headers() {
        if (headers == null) {
            headers = new HttpHeaders(nettyHeaders);
        }
        return headers;
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
    public HttpVersion version() {
        if (version == null) {
            version = adapt(request.protocolVersion());
        }
        return version;
    }
}
