package eu.arrowhead.kalix.internal.net.http.client;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.internal.net.http.HttpMediaTypes;
import eu.arrowhead.kalix.internal.net.http.NettyHttpBodyReceiver;
import eu.arrowhead.kalix.internal.net.http.NettyHttpPeer;
import eu.arrowhead.kalix.internal.net.http.service.NettyHttpServiceResponse;
import eu.arrowhead.kalix.util.annotation.Internal;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import javax.net.ssl.SSLEngine;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Optional;

@Internal
public class NettyHttpClientConnectionHandler extends SimpleChannelInboundHandler<HttpObject> {
    private final HttpResponseReceiver responseReceiver;
    private final SSLEngine sslEngine;

    private NettyHttpBodyReceiver body = null;

    public NettyHttpClientConnectionHandler(final HttpResponseReceiver responseReceiver, final SSLEngine sslEngine) {
        this.responseReceiver = Objects.requireNonNull(responseReceiver, "Expected responseReceiver");
        this.sslEngine = sslEngine;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final HttpObject msg) throws Exception {
        if (msg instanceof HttpResponse) {
            if (handleResponseHead(ctx, (HttpResponse) msg)) {
                return;
            }
        }
        if (msg instanceof HttpContent) {
            handleResponseContent((HttpContent) msg);
        }
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private boolean handleResponseHead(final ChannelHandlerContext ctx, final HttpResponse response) {
        // TODO: Enable and check size restrictions.
/*
        final EncodingDescriptor encoding;
        {
            final var optionalEncoding = determineEncodingFrom(response.headers());
            if (optionalEncoding.isEmpty()) {
                sendErrorAndClose(ctx, response, HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE);
                return true;
            }
            encoding = optionalEncoding.get();
        }

        final var serviceResponseBody = new NettyHttpBodyReceiver(ctx.alloc(), encoding, response.headers());
        final var serviceResponse = new NettyHttpClientResponse.Builder()
            .body(serviceResponseBody)
            .encoding(encoding)
            .response(response)
            .responder(new NettyHttpPeer((InetSocketAddress) ctx.channel().remoteAddress(), sslEngine))
            .build();

        this.body = serviceResponseBody;

        responseReceiver.receive(serviceResponse);
*/
        return false;
    }

    /*
     * According to RFC 7231, Section 5.3.2, one can 'disregard the ["accept"]
     * header field by treating the response as if it is not subject to content
     * negotiation.' An Arrowhead service always has the same input and output
     * encodings. If "content-type" is specified, it is used to determine
     * encoding. If "content-type" is not specified but "accept" is, it is used
     * instead. If neither field is specified, the configured default encoding
     * is assumed to be adequate. No other content-negotiation is possible.
     */
/*    private Optional<EncodingDescriptor> determineEncodingFrom(final HttpHeaders headers) {
        final var contentType = headers.get("content-type");
        if (contentType != null) {
            return HttpMediaTypes.findEncodingCompatibleWithContentType(service.encodings(), contentType);
        }

        final var acceptHeaders = headers.getAll("accept");
        if (acceptHeaders != null && acceptHeaders.size() > 0) {
            return HttpMediaTypes.findEncodingCompatibleWithAcceptHeaders(service.encodings(), acceptHeaders);
        }

        return Optional.of(service.defaultEncoding());
    }*/

    private void handleResponseContent(final HttpContent content) {
        body.append(content);
        if (content instanceof LastHttpContent) {
            body.finish((LastHttpContent) content);
        }
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
