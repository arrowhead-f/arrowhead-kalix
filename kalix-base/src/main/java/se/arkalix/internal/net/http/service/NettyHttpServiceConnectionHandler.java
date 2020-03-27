package se.arkalix.internal.net.http.service;

import io.netty.handler.ssl.SslHandler;
import se.arkalix.description.SystemDescription;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.dto.DtoReadException;
import se.arkalix.internal.net.http.HttpMediaTypes;
import se.arkalix.internal.net.http.NettyHttpBodyReceiver;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpServiceRequestException;
import se.arkalix.security.access.AccessTokenException;
import se.arkalix.util.annotation.Internal;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import javax.net.ssl.SSLPeerUnverifiedException;
import java.net.InetSocketAddress;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static se.arkalix.util.concurrent.Future.done;

@Internal
public class NettyHttpServiceConnectionHandler extends SimpleChannelInboundHandler<Object> {
    private final HttpServiceLookup serviceLookup;
    private final SslHandler sslHandler;

    private SystemDescription consumer = null;
    private HttpRequest request = null;
    private NettyHttpBodyReceiver body = null;

    public NettyHttpServiceConnectionHandler(final HttpServiceLookup serviceLookup, final SslHandler sslHandler) {
        this.serviceLookup = serviceLookup;
        this.sslHandler = sslHandler;
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

        this.request = request;
        final var keepAlive = HttpUtil.isKeepAlive(request);
        final var queryStringDecoder = new QueryStringDecoder(request.uri());
        final var path = queryStringDecoder.path();

        // Resolve service.
        final HttpServiceInternal service;
        {
            final var optionalService = serviceLookup.getServiceByPath(path);
            if (optionalService.isEmpty()) {
                sendErrorAndCleanup(ctx, NOT_FOUND, keepAlive);
                return;
            }
            service = optionalService.get();
        }

        // Authorize.
        {
            var token = request.headers().get("authorization");
            if (token != null && token.regionMatches(true, 0, "bearer ", 0, 7)) {
                token = token.substring(7).stripLeading();
            }
            try {
                if (consumer == null && sslHandler != null) {
                    final var optionalConsumer = SystemDescription.tryFrom(
                        sslHandler.engine().getSession().getPeerCertificates(),
                        (InetSocketAddress) ctx.channel().remoteAddress());

                    if (optionalConsumer.isEmpty()) {
                        sendErrorAndCleanup(ctx, UNAUTHORIZED, false);
                        return;
                    }
                    consumer = optionalConsumer.get();
                }

                if (!service.accessPolicy().isAuthorized(consumer, service.description(), token)) {
                    sendErrorAndCleanup(ctx, UNAUTHORIZED, false);
                    return;
                }
            }
            catch (final AccessTokenException | SSLPeerUnverifiedException exception) {
                sendErrorAndCleanup(ctx, UNAUTHORIZED, false);
                return;
            }
            catch (final Exception exception) {
                ctx.fireExceptionCaught(exception);
                sendErrorAndCleanup(ctx, INTERNAL_SERVER_ERROR, false);
                return;
            }
        }

        // Resolve encoding.
        final EncodingDescriptor encoding;
        {
            final var optionalEncoding = determineEncodingFrom(service, request.headers());
            if (optionalEncoding.isEmpty()) {
                sendErrorAndCleanup(ctx, UNSUPPORTED_MEDIA_TYPE, keepAlive);
                return;
            }
            encoding = optionalEncoding.get();
        }

        // Manage `expect: continue` header, if present.
        if (HttpUtil.is100ContinueExpected(request)) {
            ctx.writeAndFlush(new DefaultFullHttpResponse(request.protocolVersion(), CONTINUE, Unpooled.EMPTY_BUFFER));
        }

        // Prepare for receiving HTTP request body and Assemble Kalix request.
        final var serviceRequestBody = new NettyHttpBodyReceiver(ctx.alloc(), request.headers(), encoding);
        final var serviceRequest = new NettyHttpServiceRequest.Builder()
            .body(serviceRequestBody)
            .queryStringDecoder(queryStringDecoder)
            .request(request)
            .consumer(consumer)
            .build();
        final var serviceResponseHeaders = new DefaultHttpHeaders();
        final var serviceResponse = new NettyHttpServiceResponse(request, serviceResponseHeaders, encoding);
        this.body = serviceRequestBody;

        // Tell service to handle request and then respond to the connected client.
        service.handle(serviceRequest, serviceResponse)
            .map(ignored -> {
                HttpUtil.setKeepAlive(serviceResponseHeaders, request.protocolVersion(), keepAlive);
                final var channelFuture = serviceResponse.write(ctx.channel());
                if (!keepAlive) {
                    channelFuture.addListener(ChannelFutureListener.CLOSE);
                }
                return done();
            })
            .onFailure(fault -> {
                final var status = (fault instanceof HttpServiceRequestException || fault instanceof DtoReadException)
                    ? BAD_REQUEST
                    : INTERNAL_SERVER_ERROR;
                sendErrorAndCleanup(ctx, status, keepAlive);
                if (status == INTERNAL_SERVER_ERROR) {
                    ctx.fireExceptionCaught(fault); // TODO: Log properly.
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
    private Optional<EncodingDescriptor> determineEncodingFrom(
        final HttpServiceInternal service,
        final HttpHeaders headers)
    {
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
            body = null;
        }
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) {
        ctx.flush();
    }

    // TODO: Bring any response exceptions back to service.
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, Throwable cause) {
        try {
            if (body != null) {
                body.abort(cause);
                body = null;
            }
        }
        catch (final Throwable throwable) {
            throwable.addSuppressed(cause);
            cause = throwable;
        }
        finally {
            ctx.fireExceptionCaught(cause); // TODO: Log properly.
            sendErrorAndCleanup(ctx, INTERNAL_SERVER_ERROR, false);
        }
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (evt instanceof IdleStateEvent) {
            final var idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                if (body != null) {
                    body.abort(new HttpServiceRequestException(HttpStatus.REQUEST_TIMEOUT));
                    body = null;
                    sendErrorAndCleanup(ctx, REQUEST_TIMEOUT, false);
                }
            }
            ctx.close();
        }
    }

    private void sendErrorAndCleanup(
        final ChannelHandlerContext ctx,
        final HttpResponseStatus status,
        final boolean keepAlive)
    {
        final HttpHeaders headers;
        final var version = request != null
            ? request.protocolVersion()
            : HttpVersion.HTTP_1_1;

        headers = new DefaultHttpHeaders(false)
            .add("content-length", "0");

        if (keepAlive) {
            HttpUtil.setKeepAlive(headers, version, true);
        }

        final var future = ctx.writeAndFlush(new DefaultFullHttpResponse(
            version, status, Unpooled.EMPTY_BUFFER, headers, EmptyHttpHeaders.INSTANCE));

        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
