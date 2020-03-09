package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.dto.DataWritable;
import eu.arrowhead.kalix.dto.DataWriter;
import eu.arrowhead.kalix.dto.WriteException;
import eu.arrowhead.kalix.internal.dto.binary.ByteBufWriter;
import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpStatus;
import eu.arrowhead.kalix.net.http.HttpVersion;
import eu.arrowhead.kalix.net.http.service.HttpServiceResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

import static eu.arrowhead.kalix.internal.net.http.NettyHttp.adapt;

public class NettyHttpServiceResponse implements HttpServiceResponse {
    private final EncodingDescriptor encoding;
    private final HttpRequest request;
    private final io.netty.handler.codec.http.HttpHeaders nettyHeaders;

    private Object body = null;
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

    public ChannelFuture writeTo(final ChannelHandlerContext ctx)
        throws WriteException, IOException
    {
        final ByteBuf content;
        if (body == null) {
            content = Unpooled.EMPTY_BUFFER;
        }
        else if (body instanceof byte[]) {
            content = Unpooled.wrappedBuffer((byte[]) body);
        }
        else if (body instanceof DataWritable) {
            content = ctx.alloc().buffer();
            DataWriter.write((DataWritable) body,
                encoding.asDataEncoding().orElseThrow(() -> new UnsupportedOperationException("" +
                    "There is no DTO support for the \"" + encoding +
                    "\" encoding; response body cannot be encoded")),
                new ByteBufWriter(content));
        }
        else if (body instanceof Path) {
            return writeFileBodyTo((Path) body, ctx);
        }
        else if (body instanceof String) {
            final var charset = HttpUtil.getCharset(nettyHeaders.get("content-type"), StandardCharsets.UTF_8);
            content = Unpooled.wrappedBuffer(((String) body).getBytes(charset));
        }
        else {
            throw new IllegalStateException("Invalid response body supplied \"" + body + "\"");
        }

        return ctx.writeAndFlush(new DefaultFullHttpResponse(
            request.protocolVersion(),
            adapt(status),
            content,
            nettyHeaders,
            EmptyHttpHeaders.INSTANCE));
    }

    private ChannelFuture writeFileBodyTo(final Path path, final ChannelHandlerContext ctx) throws IOException {
        final var file = new RandomAccessFile(path.toFile(), "r");
        final var length = file.length();
        final var response = new DefaultHttpResponse(request.protocolVersion(), adapt(status), nettyHeaders);
        HttpUtil.setContentLength(response, length);

        ctx.write(new DefaultFileRegion(file.getChannel(), 0, length));
        return ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    }

    @Override
    public Optional<Object> body() {
        return Optional.ofNullable(body);
    }

    @Override
    public HttpServiceResponse body(final byte[] byteArray) {
        this.body = byteArray;
        return this;
    }

    @Override
    public HttpServiceResponse body(final DataWritable body) {
        this.body = body;
        return this;
    }

    @Override
    public HttpServiceResponse body(final Path path) {
        this.body = path;
        return this;
    }

    @Override
    public HttpServiceResponse body(final String string) {
        this.body = string;
        return this;
    }

    @Override
    public HttpServiceResponse clearBody() {
        body = null;
        return this;
    }

    @Override
    public HttpServiceResponse clearHeaders() {
        return null;
    }

    @Override
    public EncodingDescriptor encoding() {
        return encoding;
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
            headers = new NettyHttpHeaders(nettyHeaders);
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
