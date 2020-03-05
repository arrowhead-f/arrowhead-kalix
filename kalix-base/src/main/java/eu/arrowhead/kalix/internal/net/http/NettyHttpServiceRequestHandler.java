package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.net.http.service.HttpRequester;
import eu.arrowhead.kalix.net.http.service.HttpServiceRequest;
import eu.arrowhead.kalix.net.http.service.HttpServiceResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLPeerUnverifiedException;

import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;

import static eu.arrowhead.kalix.internal.net.http.NettyHttp.adapt;

public class NettyHttpServiceRequestHandler extends SimpleChannelInboundHandler<Object> {
    private final HttpServiceRequestHandler handler;
    private final SSLEngine sslEngine;

    private NettyHttpServiceRequestBody body;

    public NettyHttpServiceRequestHandler(
        final HttpServiceRequestHandler handler,
        final SSLEngine sslEngine)
    {
        this.handler = handler;
        this.sslEngine = sslEngine;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof HttpRequest) {
            handleRequest(ctx, (HttpRequest) msg);
        }
        if (msg instanceof HttpContent) {
            handleContent((HttpContent) msg);
        }
        if (msg instanceof LastHttpContent) {
            handleLastContent((LastHttpContent) msg);
        }
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void handleRequest(final ChannelHandlerContext ctx, final HttpRequest request) {
        // Verify that media type in request is supported.
        /*{
            final var mediaType = HttpUtil.getMimeType(request);
            if (mediaType != null) {
                var isSupported = false;
                for (final var encoding : encodings) {
                    if (encoding.usedByMediaType(mediaType)) {
                        isSupported = true;
                        break;
                    }
                }
                if (!isSupported) {
                    ctx.writeAndFlush(new DefaultFullHttpResponse(
                        request.protocolVersion(),
                        HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE,
                        Unpooled.EMPTY_BUFFER
                    )).addListener(ChannelFutureListener.CLOSE);
                    return;
                }
            }
        }*/ // TODO: Implement this properly. We need to know which service is being invoked!

        // Verify that the stated "content-length" does not exceed any limit.
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

        this.body = new NettyHttpServiceRequestBody(ctx.alloc(), request.headers());
        final var decoder = new QueryStringDecoder(request.uri());
        final var request0 = new HttpServiceRequest.Builder()
            .version(adapt(request.protocolVersion()))
            .method(adapt(request.method()))
            .headers(adapt(request.headers()))
            .path(decoder.path())
            .queryParameters(new NettyQueryParameterMap(decoder))
            .body(this.body)
            .requesterSupplier(() -> {
                try {
                    return new HttpRequester(
                        sslEngine != null
                            ? (X509Certificate) sslEngine.getSession().getPeerCertificates()[0]
                            : null,
                        (InetSocketAddress) ctx.channel().remoteAddress(),
                        request.headers().get("authorization")
                    );
                }
                catch (final SSLPeerUnverifiedException exception) {
                    throw new RuntimeException(exception);
                }
            })
            .build();

        handler.handle(request0).onResult(result -> {
            if (result.isSuccess()) {
                handleResponse(result.value());
            }
            else {
                ctx.writeAndFlush(new DefaultFullHttpResponse(
                    request.protocolVersion(),
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    Unpooled.EMPTY_BUFFER
                )).addListener(ChannelFutureListener.CLOSE);
                ctx.fireExceptionCaught(result.fault());
            }
        });
    }

    private void handleContent(final HttpContent content) {
        body.append(content);
    }

    private void handleLastContent(final LastHttpContent lastContent) {
        body.finish(lastContent);
    }

    private void handleResponse(final HttpServiceResponse response) {
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
