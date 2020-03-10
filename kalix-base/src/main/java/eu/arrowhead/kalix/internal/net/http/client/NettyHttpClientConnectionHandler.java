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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Internal
public class NettyHttpClientConnectionHandler extends SimpleChannelInboundHandler<HttpObject> {
    private final NettyHttpClientConnection connection;

    private NettyHttpBodyReceiver body = null;

    public NettyHttpClientConnectionHandler(
        final NettyHttpClientConnection connection)
    {
        this.connection = Objects.requireNonNull(connection, "Expected connection");
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
        final var encodings = connection.encodings();
        final var encoding = HttpMediaTypes.findEncodingCompatibleWithContentType(encodings, contentType)
            .orElseThrow(() -> new HttpClientResponseException("" +
                "The content-type \"" + contentType + "\" is not compatible " +
                "with eny encoding declared for the HTTP client owning this " +
                "connection " + Stream.of(encodings)
                .map(EncodingDescriptor::toString)
                .collect(Collectors.joining(", ", "(", ")."))));

        final var serviceResponseBody = new NettyHttpBodyReceiver(ctx.alloc(), encoding, response.headers());
        final var serviceResponse = new NettyHttpClientResponse(serviceResponseBody, encoding, response);

        this.body = serviceResponseBody;

        connection.onResponseResult(Result.success(serviceResponse));
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
        if (connection.onResponseResult(Result.failure(cause))) {
            return;
        }
        ctx.fireExceptionCaught(cause);
    }
}
