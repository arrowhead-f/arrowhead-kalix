package eu.arrowhead.kalix.internal.net.http.client;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.internal.net.http.HttpMediaTypes;
import eu.arrowhead.kalix.internal.net.http.NettyHttpBodyReceiver;
import eu.arrowhead.kalix.internal.net.http.NettyHttpPeer;
import eu.arrowhead.kalix.internal.net.http.service.NettyHttpServiceResponse;
import eu.arrowhead.kalix.net.http.client.HttpClientResponseException;
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
    protected void channelRead0(final ChannelHandlerContext ctx, final HttpObject msg) {
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

        final EncodingDescriptor encoding;
        {
            final var encodings = responseReceiver.encodings();
            final var contentType = response.headers().get("content-type");
            if (contentType == null) {
                encoding = encodings[0];
            }
            else {
                final var encoding0 = HttpMediaTypes.findEncodingCompatibleWithContentType(encodings, contentType);
                if (encoding0.isEmpty()) {
                    responseReceiver.fail(new HttpClientResponseException(
                        "No supported media type in response body; " +
                            "content-type \"" + contentType + "\" does not " +
                            "match any encoding supported by this client"));
                    return true;
                }
                encoding = encoding0.get();
            }
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

        return false;
    }

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
