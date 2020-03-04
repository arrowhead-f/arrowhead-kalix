package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.dto.DataReadable;
import eu.arrowhead.kalix.util.concurrent.Future;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import javax.net.ssl.SSLEngine;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;

public class NettyHttpServiceRequestHandler extends SimpleChannelInboundHandler<Object> {
    private final HttpServiceRequestHandler handler;
    private final SSLEngine sslEngine;

    private HttpRequest request;

    public NettyHttpServiceRequestHandler(final HttpServiceRequestHandler handler, final SSLEngine sslEngine) {
        this.handler = handler;
        this.sslEngine = sslEngine;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            handleRequest(ctx, (HttpRequest) msg);
        }
        if (msg instanceof HttpContent) {
            handleContent(ctx, (HttpContent) msg);
        }
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
        super.channelReadComplete(ctx);
    }

    private void handleRequest(final ChannelHandlerContext ctx, final HttpRequest request) {
        this.request = request;

        final var decoder = new QueryStringDecoder(request.uri());

        handler.handle(new NettyHttpServiceRequest.Builder()
            .version(request.protocolVersion())
            .method(request.method())
            .headers(request.headers())
            .path(decoder.path())
            .queryParameters(new NettyQueryParameterMap(decoder))
            .bodyHandler(new NettyHttpBodyHandler() {
                @Override
                public <R extends DataReadable> Future<R> bodyAs(final Class<R> class_) {
                    return Future.success(null); // TODO
                }

                @Override
                public Future<byte[]> bodyAsBytes() {
                    return Future.success(new byte[0]); // TODO
                }

                @Override
                public InputStream bodyAsStream() {
                    return new ByteArrayInputStream(new byte[0]); // TODO
                }

                @Override
                public Future<String> bodyAsString() {
                    return Future.success(""); // TODO
                }

                @Override
                public Future<?> bodyToPath(final Path path) {
                    return Future.done(); // TODO
                }
            })
            .requesterSupplier(() -> null)
            .build())
            .onResult(result -> {
                // TODO: Send response! If body hasn't been received yet, discard it!
            });

        // TODO: Make sure configured size restrictions are honored by request headers.
        if (HttpUtil.is100ContinueExpected(request)) {
            ctx.write(new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.CONTINUE,
                Unpooled.EMPTY_BUFFER
            ));
        }


    }

    private void handleContent(final ChannelHandlerContext ctx, final HttpContent content) {

    }
}
