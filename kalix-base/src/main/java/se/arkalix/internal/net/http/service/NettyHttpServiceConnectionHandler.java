package se.arkalix.internal.net.http.service;

import io.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.ArSystem;
import se.arkalix.description.ConsumerDescription;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.dto.DtoReadException;
import se.arkalix.internal.net.http.HttpMediaTypes;
import se.arkalix.internal.net.http.NettyHttpBodyReceiver;
import se.arkalix.internal.net.NettySimpleChannelInboundHandler;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpServiceRequestException;
import se.arkalix.security.access.AccessTokenException;
import se.arkalix.util.annotation.Internal;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import javax.net.ssl.SSLPeerUnverifiedException;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static se.arkalix.util.concurrent.Future.done;

@Internal
public class NettyHttpServiceConnectionHandler extends NettySimpleChannelInboundHandler<Object> {
    private static final Logger logger = LoggerFactory.getLogger(NettyHttpServiceConnectionHandler.class);

    private final ArSystem system;
    private final HttpServiceLookup serviceLookup;
    private final SslHandler sslHandler;

    private HttpRequest request = null;
    private boolean keepAlive = false;
    private HttpServiceInternal service = null;
    private ConsumerDescription consumer = null;
    private NettyHttpBodyReceiver body = null;

    public NettyHttpServiceConnectionHandler(
        final ArSystem system,
        final HttpServiceLookup serviceLookup,
        final SslHandler sslHandler)
    {
        this.system = Objects.requireNonNull(system, "Expected system");
        this.serviceLookup = Objects.requireNonNull(serviceLookup, "Expected serviceLookup");
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
        keepAlive = HttpUtil.isKeepAlive(request);

        final var queryStringDecoder = new QueryStringDecoder(request.uri());
        final var path = queryStringDecoder.path();

        service = resolveService(ctx, path);
        if (service == null) {
            return;
        }
        if (!authorize(ctx, request)) {
            return;
        }
        final var encoding = resolveEncoding(ctx);
        if (encoding == null) {
            return;
        }

        if (HttpUtil.is100ContinueExpected(request)) {
            ctx.writeAndFlush(new DefaultFullHttpResponse(request.protocolVersion(), CONTINUE, Unpooled.EMPTY_BUFFER));
        }

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

        service
            .handle(serviceRequest, serviceResponse)
            .map(ignored -> {
                HttpUtil.setKeepAlive(serviceResponseHeaders, request.protocolVersion(), keepAlive);
                final var channelFuture = serviceResponse.write(ctx.channel());
                if (!keepAlive) {
                    channelFuture.addListener(ChannelFutureListener.CLOSE);
                }
                return done();
            })
            .onFailure(fault -> {
                if (fault instanceof HttpServiceRequestException || fault instanceof DtoReadException) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Incoming request could not be processed", fault);
                    }
                    sendEmptyResponseAndCleanup(ctx, BAD_REQUEST, false);
                }
                else {
                    logAndSendInternalServerError(ctx, "handling", fault);
                }
            });
    }

    private HttpServiceInternal resolveService(final ChannelHandlerContext ctx, final String path) {
        final var optionalService = serviceLookup.getServiceByPath(path);
        if (optionalService.isEmpty()) {
            sendEmptyResponseAndCleanup(ctx, NOT_FOUND);
            return null;
        }
        return optionalService.get();
    }

    private boolean authorize(final ChannelHandlerContext ctx, final HttpRequest request) {
        var token = request.headers().get("authorization");
        if (token != null && token.regionMatches(true, 0, "bearer ", 0, 7)) {
            token = token.substring(7).stripLeading();
        }
        try {
            if (consumer == null && sslHandler != null) {
                final var optionalConsumer = ConsumerDescription.tryFrom(
                    sslHandler.engine().getSession().getPeerCertificates(),
                    (InetSocketAddress) ctx.channel().remoteAddress());

                if (optionalConsumer.isEmpty()) {
                    sendEmptyResponseAndCleanup(ctx, UNAUTHORIZED);
                    return false;
                }
                consumer = optionalConsumer.get();
            }

            if (!service.accessPolicy().isAuthorized(consumer, system, service.description(), token)) {
                sendEmptyResponseAndCleanup(ctx, UNAUTHORIZED);
                return false;
            }

            return true;
        }
        catch (final AccessTokenException | SSLPeerUnverifiedException exception) {
            sendEmptyResponseAndCleanup(ctx, UNAUTHORIZED);
            return false;
        }
        catch (final Exception exception) {
            logAndSendInternalServerError(ctx, "authorizing", exception);
            return false;
        }
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
    private EncodingDescriptor resolveEncoding(final ChannelHandlerContext ctx) {
        final var headers = request.headers();
        final var encodings = service.encodings();

        var encoding = Optional.<EncodingDescriptor>empty();

        final var contentType = headers.get("content-type");
        if (contentType != null) {
            encoding = HttpMediaTypes.findEncodingCompatibleWithContentType(encodings, contentType);
        }
        else {
            final var acceptHeaders = headers.getAll("accept");
            if (acceptHeaders != null && acceptHeaders.size() > 0) {
                encoding = HttpMediaTypes.findEncodingCompatibleWithAcceptHeaders(encodings, acceptHeaders);
            }
            else {
                return service.defaultEncoding();
            }
        }
        if (encoding.isPresent()) {
            return encoding.get();
        }
        sendEmptyResponseAndCleanup(ctx, UNSUPPORTED_MEDIA_TYPE);
        return null;
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
    public void exceptionCaught(final ChannelHandlerContext ctx, Throwable cause) {
        try {
            if (body != null && body.tryAbort(cause)) {
                body = null;
            }
        }
        catch (final Throwable throwable) {
            throwable.addSuppressed(cause);
            cause = throwable;
        }
        finally {
            logAndSendInternalServerError(ctx, "handling", cause);
        }
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (evt instanceof IdleStateEvent) {
            final var idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                if (body != null) {
                    final var exception = new HttpServiceRequestException(HttpStatus.REQUEST_TIMEOUT);
                    if (body.tryAbort(exception)) {
                        body = null;
                        sendEmptyResponseAndCleanup(ctx, REQUEST_TIMEOUT, false);
                    }
                }
            }
            ctx.close();
        }
    }

    private void logAndSendInternalServerError(
        final ChannelHandlerContext ctx,
        final String activity,
        final Throwable throwable)
    {
        if (logger.isErrorEnabled()) {
            final var builder = new StringBuilder();
            builder
                .append("An unexpected exception was caught while ")
                .append(activity);

            if (request != null) {
                builder
                    .append(" the request ")
                    .append(request.method())
                    .append(' ')
                    .append(request.uri());
            }
            else {
                builder.append(" a request");
            }

            if (service != null) {
                builder
                    .append(" routed to the \"")
                    .append(service.name())
                    .append("\" service");
            }
            else {
                builder.append(" before it could be routed to a service");
            }

            builder.append("; the request was received from ");

            if (consumer != null) {
                builder
                    .append("the system \"")
                    .append(consumer.name())
                    .append("\" at ");
            }
            builder.append(ctx.channel().remoteAddress());

            logger.error(builder.toString(), throwable);
        }
        sendEmptyResponseAndCleanup(ctx, INTERNAL_SERVER_ERROR, false);
    }

    private void sendEmptyResponseAndCleanup(final ChannelHandlerContext ctx, final HttpResponseStatus status) {
        sendEmptyResponseAndCleanup(ctx, status, keepAlive);
    }

    private void sendEmptyResponseAndCleanup(
        final ChannelHandlerContext ctx,
        final HttpResponseStatus status,
        final boolean keepAlive)
    {
        final HttpHeaders headers = new DefaultHttpHeaders(false)
            .add("content-length", "0");

        final var version = request != null
            ? request.protocolVersion()
            : HttpVersion.HTTP_1_1;

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
