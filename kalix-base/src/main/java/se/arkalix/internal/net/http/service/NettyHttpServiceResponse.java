package se.arkalix.internal.net.http.service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoWritable;
import se.arkalix.dto.DtoWriteException;
import se.arkalix.internal.dto.binary.ByteBufWriter;
import se.arkalix.internal.net.http.HttpMediaTypes;
import se.arkalix.net.http.HttpHeaders;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.HttpVersion;
import se.arkalix.net.http.service.HttpServiceResponse;
import se.arkalix.util.annotation.Internal;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static se.arkalix.internal.net.http.NettyHttpConverters.convert;

@Internal
public class NettyHttpServiceResponse implements HttpServiceResponse {
    private final EncodingDescriptor encoding;
    private final HttpRequest request;
    private final io.netty.handler.codec.http.HttpHeaders nettyHeaders;

    private Object body = null;
    private DtoEncoding dtoEncoding = null;
    private HttpHeaders headers = null;
    private HttpStatus status = null;
    private HttpVersion version = null;

    public NettyHttpServiceResponse(
        final HttpRequest request,
        final io.netty.handler.codec.http.HttpHeaders headers,
        final EncodingDescriptor encoding)
    {
        this.encoding = Objects.requireNonNull(encoding, "Expected encoding");
        this.nettyHeaders = Objects.requireNonNull(headers, "Expected headers");
        this.request = Objects.requireNonNull(request, "Expected request");
    }

    @SuppressWarnings("unchecked")
    public ChannelFuture write(final Channel channel)
        throws DtoWriteException, IOException
    {
        if (status == null) {
            throw new IllegalStateException("No HTTP status specified");
        }

        final var nettyStatus = convert(status);
        final var nettyVersion = request.protocolVersion();

        final ByteBuf content;
        if (body == null) {
            content = Unpooled.EMPTY_BUFFER;
        }
        else if (body instanceof byte[]) {
            content = Unpooled.wrappedBuffer((byte[]) body);
        }
        else if (body instanceof DtoWritable || body instanceof List) {
            if (dtoEncoding == null) {
                dtoEncoding = encoding.asDtoEncoding().orElseThrow(() -> new IllegalStateException("" +
                    "There is no DTO support for the \"" + encoding +
                    "\" encoding; response body cannot be encoded"));
            }
            content = channel.alloc().buffer();
            final var buffer = new ByteBufWriter(content);
            final var writer = dtoEncoding.writer();
            if (body instanceof DtoWritable) {
                writer.writeOne((DtoWritable) body, buffer);
            }
            else {
                writer.writeMany((List<DtoWritable>) body, buffer);
            }
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
    public HttpServiceResponse body(final DtoWritable data) {
        body = data;
        return this;
    }

    @Override
    public HttpServiceResponse body(final List<DtoWritable> data) {
        body = data;
        return this;
    }

    @Override
    public HttpServiceResponse body(final DtoEncoding encoding, final DtoWritable data) {
        dtoEncoding = encoding;
        body = data;
        return this;
    }

    @Override
    public <L extends List<? extends DtoWritable>> HttpServiceResponse body(final DtoEncoding encoding, L data) {
        dtoEncoding = encoding;
        body = data;
        return this;
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
            version = convert(request.protocolVersion());
        }
        return version;
    }
}
