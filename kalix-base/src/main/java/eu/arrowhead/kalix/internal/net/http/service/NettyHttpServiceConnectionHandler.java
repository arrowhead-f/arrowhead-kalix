package eu.arrowhead.kalix.internal.net.http.service;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.internal.net.http.HttpMediaTypes;
import eu.arrowhead.kalix.internal.net.http.NettyHttpBodyReceiver;
import eu.arrowhead.kalix.internal.net.http.NettyHttpPeer;
import eu.arrowhead.kalix.net.http.service.HttpService;
import eu.arrowhead.kalix.util.annotation.Internal;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import javax.net.ssl.SSLEngine;

import java.net.InetSocketAddress;
import java.util.Optional;

@Internal
public class NettyHttpServiceConnectionHandler extends SimpleChannelInboundHandler<Object> {
    private final HttpServiceLookup serviceLookup;
    private final SSLEngine sslEngine;

    private NettyHttpBodyReceiver body;

    public NettyHttpServiceConnectionHandler(final HttpServiceLookup serviceLookup, final SSLEngine sslEngine) {
        this.serviceLookup = serviceLookup;
        this.sslEngine = sslEngine;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof HttpRequest) {
            readRequest(ctx, (HttpRequest) msg);
        }
        if (msg instanceof HttpContent) {
            readContent((HttpContent) msg);
        }
    }

    private void readRequest(final ChannelHandlerContext ctx, final HttpRequest request) {
        // TODO: Enable and check size restrictions.

        final var keepAlive = HttpUtil.isKeepAlive(request);

        final var queryStringDecoder = new QueryStringDecoder(request.uri());
        final var path = queryStringDecoder.path();

        final HttpService service;
        {
            final var optionalService = serviceLookup.getServiceByPath(path);
            if (optionalService.isEmpty()) {
                sendErrorAndCleanup(ctx, request, HttpResponseStatus.NOT_FOUND, keepAlive);
                return;
            }
            service = optionalService.get();
        }

        final EncodingDescriptor encoding;
        {
            final var optionalEncoding = determineEncodingFrom(service, request.headers());
            if (optionalEncoding.isEmpty()) {
                sendErrorAndCleanup(ctx, request, HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE, keepAlive);
                return;
            }
            encoding = optionalEncoding.get();
        }

        if (HttpUtil.is100ContinueExpected(request)) {
            ctx.writeAndFlush(new DefaultFullHttpResponse(
                request.protocolVersion(),
                HttpResponseStatus.CONTINUE,
                Unpooled.EMPTY_BUFFER
            ));
        }

        final var serviceRequestBody = new NettyHttpBodyReceiver(ctx.alloc(), encoding, request.headers());
        final var serviceRequest = new NettyHttpServiceRequest.Builder()
            .body(serviceRequestBody)
            .queryStringDecoder(queryStringDecoder)
            .request(request)
            .requester(new NettyHttpPeer((InetSocketAddress) ctx.channel().remoteAddress(), sslEngine))
            .build();

        final var serviceResponseHeaders = new DefaultHttpHeaders();
        final var serviceResponse = new NettyHttpServiceResponse(request, serviceResponseHeaders, encoding);

        this.body = serviceRequestBody;

        service.handle(serviceRequest, serviceResponse).onResult(result -> {
            try {
                if (result.isSuccess()) {
                    HttpUtil.setKeepAlive(serviceResponseHeaders, request.protocolVersion(), keepAlive);
                    final var channelFuture = serviceResponse.write(ctx.channel());
                    if (!keepAlive) {
                        channelFuture.addListener(ChannelFutureListener.CLOSE);
                    }
                    return;
                }
                sendErrorAndCleanup(ctx, request, HttpResponseStatus.INTERNAL_SERVER_ERROR, keepAlive);
                ctx.fireExceptionCaught(result.fault());
            }
            catch (final Throwable throwable) {
                ctx.fireExceptionCaught(throwable);
            }
        });
    }

    /**
     * According to RFC 7231, Section 5.3.2, one can 'disregard the ["accept"]
     * header field by treating the response as if it is not subject to content
     * negotiation.' An Arrowhead service always has the same input and output
     * encodings. If "content-type" is specified, it is used to determine
     * encoding. If "content-type" is not specified but "accept" is, it is used
     * instead. If neither field is specified, the configured default encoding
     * is assumed to be adequate. No other content-negotiation is possible.
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
    private void readContent(final HttpContent content) {
        if (body == null) {
            return;
        }
        body.append(content);
        if (content instanceof LastHttpContent) {
            body.finish((LastHttpContent) content);
        }
    }

    private void sendErrorAndCleanup(
        final ChannelHandlerContext ctx,
        final HttpRequest request,
        final HttpResponseStatus status,
        final boolean keepAlive)
    {
        final HttpHeaders headers;
        final var version = request.protocolVersion();
        if (keepAlive) {
            headers = new DefaultHttpHeaders(false);
            HttpUtil.setKeepAlive(headers, version, true);
        }
        else {
            headers = EmptyHttpHeaders.INSTANCE;
        }
        final var future = ctx.writeAndFlush(new DefaultFullHttpResponse(
            version, status, Unpooled.EMPTY_BUFFER, headers, EmptyHttpHeaders.INSTANCE));

        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) {
        ctx.flush();
    }

    // TODO: Bring any response exceptions back to service.

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        if (body != null) {
            body.abort(cause);
        }
        else {
            ctx.fireExceptionCaught(cause);
        }
    }
}
