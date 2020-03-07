package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.net.http.service.HttpServiceRequest;
import eu.arrowhead.kalix.net.http.service.HttpServiceResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import javax.net.ssl.SSLEngine;

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
        if (msg instanceof LastHttpContent) {
            handleRequestEnd((LastHttpContent) msg);
        }
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private boolean handleRequestHead(final ChannelHandlerContext ctx, final HttpRequest request) {
        final var uriDecoder = new QueryStringDecoder(request.uri());
        final var path = uriDecoder.path();

        // Lookup the service that will handle the request.
        final var optionalService = serviceLookup.getServiceByPath(path);
        if (optionalService.isEmpty()) {
            sendEmptyResponseAndClose(ctx, request.protocolVersion(), HttpResponseStatus.NOT_FOUND);
            return true;
        }
        final var service = optionalService.get();

        // Verify that the stated "content-length" does not exceed any limits.
        {
            // TODO: Enable and check size restrictions.
            if (HttpUtil.is100ContinueExpected(request)) {
                ctx.writeAndFlush(new DefaultFullHttpResponse(
                    request.protocolVersion(),
                    HttpResponseStatus.CONTINUE,
                    Unpooled.EMPTY_BUFFER
                ));
            }
        }

        // Determine what HTTP encoding to use both for encoding and decoding.
        final EncodingDescriptor encoding;
        {
            final var contentType = request.headers().get("content-type");
            if (contentType == null) {
                encoding = service.defaultEncoding();
            }
            else {
                final var encoding0 = HttpMediaTypes.findEncodingCompatibleWith(service.encodings(), contentType);
                if (encoding0.isEmpty()) {
                    sendEmptyResponseAndClose(ctx, request.protocolVersion(),
                        HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE);
                    return true;
                }
                encoding = encoding0.get();
            }
            // According to RFC 7231, Section 5.3.2, one can "disregard the
            // ["accept"] header field by treating the response as if it is
            // not subject to content negotiation." An Arrowhead service always
            // has the same input and output encodings. No content-negotiation
            // is possible. The "content-type", if present, is used to decide
            // the types of both the incoming and the outgoing messages.
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

        service.handle(serviceRequest, serviceResponse)
            .onResult(result -> {
                if (result.isSuccess()) {
                    sendResponse(ctx, serviceResponse);
                }
                else {
                    sendEmptyResponseAndClose(ctx, request.protocolVersion(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
                    ctx.fireExceptionCaught(result.fault());
                }
            });

        return false;
    }

    private void handleRequestContent(final HttpContent content) {
        body.append(content);
    }

    private void handleRequestEnd(final LastHttpContent lastContent) {
        body.finish(lastContent);
    }

    private void sendEmptyResponseAndClose(
        final ChannelHandlerContext ctx,
        final HttpVersion version,
        final HttpResponseStatus status)
    {
        ctx.writeAndFlush(new DefaultFullHttpResponse(version, status, Unpooled.EMPTY_BUFFER))
            .addListener(ChannelFutureListener.CLOSE);
    }

    private void sendResponse(final ChannelHandlerContext ctx, final HttpServiceResponse response) {
        // TODO: Implement!
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        if (body != null) {
            body.abort(cause);
        }
        ctx.close();
    }
}
