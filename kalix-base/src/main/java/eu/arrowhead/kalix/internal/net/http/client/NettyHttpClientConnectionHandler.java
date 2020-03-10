package eu.arrowhead.kalix.internal.net.http.client;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.internal.net.http.HttpMediaTypes;
import eu.arrowhead.kalix.internal.net.http.NettyHttpBodyReceiver;
import eu.arrowhead.kalix.net.http.client.HttpClientResponseException;
import eu.arrowhead.kalix.util.Result;
import eu.arrowhead.kalix.util.annotation.Internal;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.util.Objects;

@Internal
public class NettyHttpClientConnectionHandler extends SimpleChannelInboundHandler<HttpObject> {
    private final EncodingDescriptor encoding;
    private final NettyHttpClient client;

    private NettyHttpBodyReceiver body = null;

    public NettyHttpClientConnectionHandler(
        final EncodingDescriptor encoding,
        final NettyHttpClient client)
    {
        this.encoding = encoding;
        this.client = Objects.requireNonNull(client, "Expected client");
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final HttpObject msg) {
        if (msg instanceof HttpResponse) {
            handleResponseHead(ctx, (HttpResponse) msg);
        }
        if (msg instanceof HttpContent) {
            handleResponseContent((HttpContent) msg);
        }
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void handleResponseHead(final ChannelHandlerContext ctx, final HttpResponse response) {
        // TODO: Enable and check size restrictions.

        final var contentType = response.headers().get("content-type");
        if (contentType != null) {
            if (!HttpMediaTypes.isEncodingCompatibleWithContentType(encoding, contentType)) {
                client.onResponseResult(Result.failure(new HttpClientResponseException(
                    "No supported media type in response body from " +
                        client.remoteSocketAddress() + "; content-type \"" +
                        contentType + "\" does not match the encoding " +
                        "supported by this client (" + encoding + ")")));
                return;
            }
        }

        final var serviceResponseBody = new NettyHttpBodyReceiver(ctx.alloc(), encoding, response.headers());
        final var serviceResponse = new NettyHttpClientResponse(serviceResponseBody, encoding, response);

        this.body = serviceResponseBody;

        client.onResponseResult(Result.success(serviceResponse));
    }

    private void handleResponseContent(final HttpContent content) {
        if (body == null) {
            return;
        }
        body.append(content);
        if (content instanceof LastHttpContent) {
            body.finish((LastHttpContent) content);
        }
    }

    // TODO: Bring any response exceptions back to client.

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        if (body != null) {
            body.abort(cause);
            return;
        }
        if (client.onResponseResult(Result.failure(cause))) {
            return;
        }
        ctx.fireExceptionCaught(cause);
    }
}
