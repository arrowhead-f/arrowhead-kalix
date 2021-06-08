package se.arkalix.net.http.service._internal;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.ArSystem;
import se.arkalix.SystemRecordWithIdentity;
import se.arkalix.codec.CodecType;
import se.arkalix.codec.DecoderException;
import se.arkalix.codec.MediaType;
import se.arkalix.net._internal.NettyBodyOutgoing;
import se.arkalix.net._internal.NettySimpleChannelInboundHandler;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http._internal.HttpMediaTypes;
import se.arkalix.net.http._internal.NettyHttpConverters;
import se.arkalix.net.http._internal.NettyHttpHeaders;
import se.arkalix.net.http.service.HttpServiceConnection;
import se.arkalix.net.http.service.HttpServiceRequestException;
import se.arkalix.query.ServiceNotFoundException;
import se.arkalix.security.access.AccessTokenException;
import se.arkalix.security.identity.SystemIdentity;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Objects;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static se.arkalix.net.http._internal.NettyHttpConverters.convert;
import static se.arkalix.util.concurrent._internal.NettyFutures.adapt;

@Internal
public class NettyHttpServiceConnection
    extends NettySimpleChannelInboundHandler<HttpObject>
    implements HttpServiceConnection
{
    private static final Logger logger = LoggerFactory.getLogger(NettyHttpServiceConnection.class);

    private final ArSystem system;
    private final HttpServiceLookup serviceLookup;
    private final SslHandler sslHandler;

    private SystemRecordWithIdentity consumer = null;
    private Channel channel = null;

    private HttpRequest nettyRequest = null;
    private NettyHttpServiceRequest kalixRequest = null;
    private HttpServerService service = null;

    private boolean isClosing = false;

    public NettyHttpServiceConnection(
        final ArSystem system,
        final HttpServiceLookup serviceLookup,
        final SslHandler sslHandler
    ) {
        this.system = Objects.requireNonNull(system, "system");
        this.serviceLookup = Objects.requireNonNull(serviceLookup, "serviceLookup");
        this.sslHandler = sslHandler;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        channel = ctx.channel();
        if (sslHandler != null) {
            sslHandler.handshakeFuture().addListener(future -> {
                Throwable cause;
                try {
                    if (future.isSuccess()) {
                        consumer = SystemRecordWithIdentity.tryFrom(
                            sslHandler.engine().getSession().getPeerCertificates(),
                            (InetSocketAddress) channel.remoteAddress())
                            .orElse(null);
                        if (logger.isDebugEnabled()) {
                            logger.debug("TLS handshake completed with " + channel.remoteAddress());
                        }
                        return;
                    }
                    else {
                        cause = future.cause();
                    }
                }
                catch (final Throwable throwable) {
                    cause = throwable;
                }
                if (cause instanceof ClosedChannelException) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Connection closed by " + channel.remoteAddress() +
                            " before TLS handshake could take place", cause);
                    }
                }
                else if (logger.isDebugEnabled()) {
                    logger.debug("Failed to complete TLS handshake with " +
                        channel.remoteAddress(), cause);
                }
                ctx.close();
            });
        }
        else {
            final var remoteSocketAddress = (InetSocketAddress) channel.remoteAddress();
            consumer = SystemRecordWithIdentity.from("<" + remoteSocketAddress + ">", remoteSocketAddress, null);
        }
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final HttpObject msg) {
        if (msg instanceof HttpRequest) {
            readRequest(ctx, (HttpRequest) msg);
        }
        if (msg instanceof HttpContent) {
            readContent((HttpContent) msg);
        }
    }

    private void readRequest(final ChannelHandlerContext ctx, final HttpRequest nettyRequest) {
        this.nettyRequest = nettyRequest;

        // TODO: Enable and check size restrictions.

        isClosing = !HttpUtil.isKeepAlive(nettyRequest);

        final var queryStringDecoder = new QueryStringDecoder(nettyRequest.uri());

        // Resolve HTTP service.
        {
            final var path = queryStringDecoder.path();
            if (logger.isTraceEnabled()) {
                logger.trace("Attempting to resolve service by path " + path);
            }
            service = serviceLookup.getServiceByPath(path).orElse(null);
            if (service == null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("No service associated with path " + path);
                }
                sendEmptyResponseAndCleanup(ctx, NOT_FOUND);
                return;
            }
        }

        // Ensure consumer is authenticated and authorized.
        {
            boolean isAuthorized;
            var token = nettyRequest.headers().get("authorization");
            if (token != null && token.regionMatches(true, 0, "Bearer ", 0, 7)) {
                token = token.substring(7).stripLeading();
            }
            try {
                isAuthorized = service.accessPolicy()
                    .isAuthorized(consumer, system, service.description(), token);
            }
            catch (final AccessTokenException exception) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Bad access token \"" + token + "\"", exception);
                }
                isAuthorized = false;
            }
            if (!isAuthorized) {
                sendEmptyResponseAndCleanup(ctx, FORBIDDEN);
                return;
            }
        }

        // Resolve default response codec.
        final CodecType defaultCodecType;
        {
            final var headers = nettyRequest.headers();
            final var codecs = service.codecs();

            final var acceptHeaders = headers.getAll("accept");
            if (acceptHeaders != null && acceptHeaders.size() > 0) {
                defaultCodecType = HttpMediaTypes.findCodecTypeCompatibleWithAcceptHeaders(codecs, acceptHeaders)
                    .orElse(service.defaultCodecType());
            }
            else {
                final var contentType = headers.get("content-type");
                if (contentType != null) {
                    defaultCodecType = HttpMediaTypes.findCodecTypeCompatibleWithContentType(codecs, contentType)
                        .orElse(service.defaultCodecType());
                }
                else {
                    defaultCodecType = service.defaultCodecType();
                }
            }
        }

        if (HttpUtil.is100ContinueExpected(nettyRequest)) {
            ctx.writeAndFlush(new DefaultFullHttpResponse(nettyRequest.protocolVersion(), CONTINUE, Unpooled.EMPTY_BUFFER));
        }

        this.kalixRequest = new NettyHttpServiceRequest.Builder()
            .alloc(ctx.alloc())
            .connection(this)
            .queryStringDecoder(queryStringDecoder)
            .request(nettyRequest)
            .consumer(consumer)
            .build();

        final var kalixResponse = new DefaultHttpServiceResponse(MediaType.getOrCreate(defaultCodecType));

        service
            .handle(this.kalixRequest, kalixResponse)
            .ifSuccess(ignored -> sendKalixResponseAndCleanup(kalixResponse, defaultCodecType))
            .onFailure(fault -> {
                if (fault instanceof HttpServiceRequestException) {
                    final var exception = (HttpServiceRequestException) fault;
                    if (exception.status() == HttpStatus.INTERNAL_SERVER_ERROR && logger.isDebugEnabled()) {
                        logger.debug("Caught explicit INTERNAL SERVER ERROR exception", exception);
                    }
                    sendEmptyResponseAndCleanup(ctx, convert(exception.status()));
                }
                else if (fault instanceof DecoderException) {
                    sendEmptyResponseAndCleanup(ctx, BAD_REQUEST);
                }
                else if (fault instanceof ServiceNotFoundException) {
                    sendEmptyResponseAndCleanup(ctx, NOT_FOUND);
                }
                else {
                    sendInternalServerErrorLogAndCleanup(ctx, fault);
                }
            });
    }

    private void readContent(final HttpContent content) {
        if (kalixRequest == null) {
            return;
        }
        final var body = kalixRequest.body();
        if (body.isCancelled()) {
            return;
        }
        body.write(content.content());
        if (content instanceof LastHttpContent) {
            kalixRequest.unwrap().headers().add(((LastHttpContent) content).trailingHeaders());
            kalixRequest = null;
            body.close();
        }
    }

    private void sendKalixResponseAndCleanup(
        final DefaultHttpServiceResponse response,
        final CodecType defaultCodecType
    ) throws IOException {
        final var status = response.status()
            .orElseThrow(() -> new IllegalStateException("No HTTP status specified in service response"));

        final var nettyStatus = convert(status);
        final var nettyVersion = response.version()
            .map(NettyHttpConverters::convert)
            .orElse(nettyRequest.protocolVersion());
        final var nettyHeaders = ((NettyHttpHeaders) response.headers()).unwrap();

        final NettyBodyOutgoing nettyBody;
        {
            final var responseBody = response.body().orElse(null);
            if (responseBody != null) {
                nettyBody = NettyBodyOutgoing.from(responseBody, channel.alloc());

                if (!nettyHeaders.contains(CONTENT_TYPE)) {
                    nettyHeaders.set(CONTENT_TYPE, MediaType.getOrCreate(nettyBody.codecType()
                        .orElse(defaultCodecType)));
                }
            }
            else {
                nettyBody = null;
            }
        }

        if (!nettyHeaders.contains(CONTENT_LENGTH)) {
            nettyHeaders.set(CONTENT_LENGTH, nettyBody != null
                ? Long.toString(nettyBody.length())
                : "0");
        }

        HttpUtil.setKeepAlive(nettyHeaders, nettyVersion, !isClosing);

        channel.write(new DefaultHttpResponse(nettyVersion, nettyStatus, nettyHeaders));
        if (nettyBody != null) {
            channel.write(nettyBody.content());
        }
        final var future = channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

        if (isClosing) {
            future.addListener(ChannelFutureListener.CLOSE);
        }

        cleanup();
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof SSLHandshakeException) {
            if (logger.isDebugEnabled()) {
                logger.debug("SSL handshake failed", cause);
            }
            return;
        }
        try {
            if (kalixRequest != null) {
                final var body = kalixRequest.body();
                if (!body.isDone()) {
                    kalixRequest = null;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Relayed exception causing 500 response to be sent to client", cause);
                    }
                    sendEmptyResponseAndCleanup(ctx, INTERNAL_SERVER_ERROR);
                    return;
                }
            }
        }
        catch (final Throwable throwable) {
            throwable.addSuppressed(cause);
            cause = throwable;
        }
        sendInternalServerErrorLogAndCleanup(ctx, cause);
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (!(evt instanceof IdleStateEvent)) {
            ctx.fireUserEventTriggered(evt);
            return;
        }
        isClosing = true;
        try {
            final var idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                if (kalixRequest != null) {
                    final var body = kalixRequest.body();
                    if (!body.isDone()) {
                        kalixRequest = null;
                        body.abort(new HttpServiceRequestException(HttpStatus.REQUEST_TIMEOUT));
                        sendEmptyResponseAndCleanup(ctx, REQUEST_TIMEOUT);
                    }
                }
            }
        }
        finally {
            ctx.close();
        }
    }

    private void sendInternalServerErrorLogAndCleanup(
        final ChannelHandlerContext ctx,
        final Throwable throwable
    ) {
        if (logger.isErrorEnabled()) {
            final var builder = new StringBuilder();
            builder
                .append("An unexpected exception was caught while handling");

            if (nettyRequest != null) {
                builder
                    .append(" the request ")
                    .append(nettyRequest.method())
                    .append(' ')
                    .append(nettyRequest.uri());
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
        isClosing = true;
        sendEmptyResponseAndCleanup(ctx, INTERNAL_SERVER_ERROR);
    }


    private void sendEmptyResponseAndCleanup(
        final ChannelHandlerContext ctx,
        final HttpResponseStatus status
    ) {
        final HttpHeaders headers = new DefaultHttpHeaders(false)
            .add("content-length", "0");

        final var version = nettyRequest != null
            ? nettyRequest.protocolVersion()
            : HttpVersion.HTTP_1_1;

        HttpUtil.setKeepAlive(headers, version, !isClosing);

        final var future = ctx.writeAndFlush(new DefaultFullHttpResponse(
            version, status, Unpooled.EMPTY_BUFFER, headers, EmptyHttpHeaders.INSTANCE));

        if (isClosing) {
            future.addListener(ChannelFutureListener.CLOSE);
        }

        cleanup();
    }

    private void cleanup() {
        nettyRequest = null;
        kalixRequest = null;
        service = null;
        isClosing = false;
    }

    @Override
    public SystemIdentity remoteIdentity() {
        return consumer.identity();
    }

    @Override
    public ArSystem localSystem() {
        return system;
    }

    @Override
    public InetSocketAddress remoteSocketAddress() {
        return (InetSocketAddress) channel.remoteAddress();
    }

    @Override
    public InetSocketAddress localSocketAddress() {
        return (InetSocketAddress) channel.localAddress();
    }

    @Override
    public boolean isLive() {
        return channel.isActive();
    }

    @Override
    public boolean isSecure() {
        return sslHandler != null;
    }

    @Override
    public Future<?> close() {
        return adapt(channel.close());
    }
}
