package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.net.http.service.HttpService;
import eu.arrowhead.kalix.net.http.service.HttpServiceRequest;
import eu.arrowhead.kalix.net.http.service.HttpServiceResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import javax.net.ssl.SSLEngine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

import static eu.arrowhead.kalix.internal.net.http.NettyHttp.adapt;

public class NettyHttpServiceRequestHandler extends SimpleChannelInboundHandler<Object> {
    private final HttpServiceLookup serviceLookup;
    private final SSLEngine sslEngine;

    private NettyHttpServiceRequestBody body;

    public NettyHttpServiceRequestHandler(final HttpServiceLookup serviceLookup, final SSLEngine sslEngine) {
        this.serviceLookup = serviceLookup;
        this.sslEngine = sslEngine;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof HttpRequest) {
            if (handleRequestHead(ctx, (HttpRequest) msg)) {
                return;
            }
        }
        if (msg instanceof HttpContent) {
            handleRequestContent((HttpContent) msg);
        }
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private boolean handleRequestHead(final ChannelHandlerContext ctx, final HttpRequest request) {
        final var uriDecoder = new QueryStringDecoder(request.uri());
        final var path = uriDecoder.path();

        final HttpService service;
        {
            final var optionalService = serviceLookup.getServiceByPath(path);
            if (optionalService.isEmpty()) {
                sendEmptyResponseAndClose(ctx, request.protocolVersion(), HttpResponseStatus.NOT_FOUND);
                return true;
            }
            service = optionalService.get();
        }

        final EncodingDescriptor encoding;
        {
            final var optionalEncoding = determineEncodingFrom(service, request.headers());
            if (optionalEncoding.isEmpty()) {
                sendEmptyResponseAndClose(ctx, request.protocolVersion(), HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE);
                return true;
            }
            encoding = optionalEncoding.get();
        }

        // TODO: Enable and check size restrictions.
        if (HttpUtil.is100ContinueExpected(request)) {
            ctx.writeAndFlush(new DefaultFullHttpResponse(
                request.protocolVersion(),
                HttpResponseStatus.CONTINUE,
                Unpooled.EMPTY_BUFFER
            ));
        }

        this.body = new NettyHttpServiceRequestBody(ctx.alloc(), encoding, request.headers());

        final var version = adapt(request.protocolVersion());
        final var serviceRequest = new HttpServiceRequest.Builder()
            .encoding(encoding)
            .version(version)
            .method(adapt(request.method()))
            .headers(adapt(request.headers()))
            .path(path)
            .queryParameters(new NettyQueryParameterMap(uriDecoder))
            .requester(new NettyHttpRequester(ctx, request.headers(), sslEngine))
            .body(this.body)
            .build();
        final var serviceResponse = new HttpServiceResponse(encoding, version);

        service.handle(serviceRequest, serviceResponse).onResult(result -> {
            try {
                if (result.isSuccess()) {
                    sendResponse(ctx, request, serviceResponse);
                    return;
                }
                sendEmptyResponseAndClose(ctx, request.protocolVersion(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
                ctx.fireExceptionCaught(result.fault());
            }
            catch (final Throwable throwable) {
                ctx.fireExceptionCaught(throwable);
            }
        });

        return false;
    }

    /*
     * According to RFC 7231, Section 5.3.2, one can 'disregard the ["accept"]
     * header field by treating the response as if it is not subject to content
     * negotiation.' An Arrowhead service always has the same input and output
     * encodings. No other content-negotiation is possible. If "content-type"
     * is specified, it is used to determine encoding. If "content-type" is not
     * specified but "accept" is, it is used instead. If neither field is
     * specified, the configured default encoding is assumed to be adequate.
     */
    private Optional<EncodingDescriptor> determineEncodingFrom(final HttpService service, final HttpHeaders headers) {
        final var contentType = headers.get("content-type");
        if (contentType != null) {
            return HttpMediaTypes.findEncodingCompatibleWithContentType(service.encodings(), contentType);
        }

        final var acceptHeaders = headers.getAll("accept");
        if (acceptHeaders != null && acceptHeaders.size() > 0) {
            return HttpMediaTypes.findEncodingCompatibleWithAcceptHeaders(service.encodings(), acceptHeaders);
        }

        return Optional.of(service.defaultEncoding());
    }

    private void handleRequestContent(final HttpContent content) {
        body.append(content);
        if (content instanceof LastHttpContent) {
            body.finish((LastHttpContent) content);

        }
    }

    private void sendEmptyResponseAndClose(
        final ChannelHandlerContext ctx,
        final HttpVersion version,
        final HttpResponseStatus status)
    {
        ctx.writeAndFlush(new DefaultFullHttpResponse(version, status, Unpooled.EMPTY_BUFFER))
            .addListener(ChannelFutureListener.CLOSE);
    }

    // TODO: Bring any response exceptions back to service.

    private void sendResponse(
        final ChannelHandlerContext ctx,
        final HttpRequest request,
        final HttpServiceResponse response) throws Exception
    {
        final var optionalBody = response.body();
        if (optionalBody.isPresent()) {
            final var body = optionalBody.get();
            if (body instanceof byte[]) {
                sendByteArrayResponse(ctx, request, response, (byte[]) body);
                return;
            }
            if (body instanceof Path) {
                sendPathResponse(ctx, request, response, (Path) body);
                return;
            }
            if (body instanceof String) {
                sendStringResponse(ctx, request, response, (String) body);
                return;
            }
            // Handle DTO body.
        }
    }

    private void sendByteArrayResponse(
        final ChannelHandlerContext ctx,
        final HttpRequest request,
        final HttpServiceResponse response,
        final byte[] body)
    {
        ctx
            .writeAndFlush(new DefaultFullHttpResponse(
                request.protocolVersion(),
                response.status()
                    .map(NettyHttp::adapt)
                    .orElse(HttpResponseStatus.OK),
                Unpooled.wrappedBuffer(body),
                adapt(response.headers()),
                EmptyHttpHeaders.INSTANCE
            ))
            .addListener(ChannelFutureListener.CLOSE);
    }

    private void sendPathResponse(
        final ChannelHandlerContext ctx,
        final HttpRequest request,
        final HttpServiceResponse response,
        final Path body) throws FileNotFoundException
    {
        final var file = body.toFile();
        final var in = new FileInputStream(file);
        ctx.write(new DefaultHttpResponse(
            request.protocolVersion(),
            response.status()
                .map(NettyHttp::adapt)
                .orElse(HttpResponseStatus.OK),
            adapt(response.headers())))
            .addListener(future -> ctx.write(new DefaultFileRegion(in.getChannel(), 0, file.length()))
                .addListener(future1 -> ctx.writeAndFlush(new DefaultLastHttpContent())
                    .addListener(ChannelFutureListener.CLOSE)));
    }

    private void sendStringResponse(
        final ChannelHandlerContext ctx,
        final HttpRequest request,
        final HttpServiceResponse response,
        final String body)
    {
        final var headers = adapt(response.headers());
        final var charset = HttpUtil.getCharset(headers.get("content-type"), StandardCharsets.UTF_8);
        ctx
            .writeAndFlush(new DefaultFullHttpResponse(
                request.protocolVersion(),
                response.status()
                    .map(NettyHttp::adapt)
                    .orElse(HttpResponseStatus.OK),
                Unpooled.wrappedBuffer(body.getBytes(charset)),
                headers,
                EmptyHttpHeaders.INSTANCE
            ))
            .addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        if (body != null) {
            body.abort(cause);
        }
        ctx.close();
    }
}
