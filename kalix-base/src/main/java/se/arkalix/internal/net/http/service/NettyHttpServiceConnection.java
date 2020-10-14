package se.arkalix.internal.net.http.service;

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
import se.arkalix.description.SystemIdentityDescription;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.dto.DtoReadException;
import se.arkalix.dto.DtoWriteException;
import se.arkalix.internal.net.NettyBodyOutgoing;
import se.arkalix.internal.net.NettySimpleChannelInboundHandler;
import se.arkalix.internal.net.http.HttpMediaTypes;
import se.arkalix.internal.net.http.NettyHttpConverters;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpServiceConnection;
import se.arkalix.net.http.service.HttpServiceRequestException;
import se.arkalix.query.ServiceNotFoundException;
import se.arkalix.security.access.AccessTokenException;
import se.arkalix.security.identity.SystemIdentity;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static se.arkalix.internal.net.http.NettyHttpConverters.convert;
import static se.arkalix.internal.util.concurrent.NettyFutures.adapt;

@Internal
public class NettyHttpServiceConnection
    extends NettySimpleChannelInboundHandler<Object>
    implements HttpServiceConnection
{
    private static final Logger logger = LoggerFactory.getLogger(NettyHttpServiceConnection.class);

    private final ArSystem system;
    private final HttpServiceLookup serviceLookup;
    private final SslHandler sslHandler;

    private SystemIdentityDescription consumer = null;
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
        this.system = Objects.requireNonNull(system, "Expected system");
        this.serviceLookup = Objects.requireNonNull(serviceLookup, "Expected serviceLookup");
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
                        consumer = SystemIdentityDescription.tryFrom(
                            sslHandler.engine().getSession().getPeerCertificates(),
                            (InetSocketAddress) ctx.channel().remoteAddress())
                            .orElse(null);
                        return;
                    }
                    else {
                        cause = future.cause();
                    }
                }
                catch (final Throwable throwable) {
                    cause = throwable;
                }
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to complete TLS handshake with " + ctx.channel().remoteAddress(), cause);
                }
                ctx.close();
            });
        }
        else {
            final var remoteSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            consumer = SystemIdentityDescription.from("<" + remoteSocketAddress + ">", remoteSocketAddress);
        }
        super.channelActive(ctx);
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

    private void readRequest(final ChannelHandlerContext ctx, final HttpRequest nettyRequest) {
        this.nettyRequest = nettyRequest;

        // TODO: Enable and check size restrictions.

        isClosing = !HttpUtil.isKeepAlive(nettyRequest);

        final var queryStringDecoder = new QueryStringDecoder(nettyRequest.uri());

        // Resolve HTTP service.
        {
            final var path = queryStringDecoder.path();
            service = serviceLookup.getServiceByPath(path).orElse(null);
            if (service == null) {
                sendEmptyResponseAndCleanup(ctx, NOT_FOUND);
                return;
            }
        }

        // Resolve default response encoding.
        final EncodingDescriptor defaultEncoding;
        {
            final var headers = nettyRequest.headers();
            final var encodings = service.encodings();

            final var acceptHeaders = headers.getAll("accept");
            if (acceptHeaders != null && acceptHeaders.size() > 0) {
                defaultEncoding = HttpMediaTypes.findEncodingCompatibleWithAcceptHeaders(encodings, acceptHeaders)
                    .orElse(service.defaultEncoding());
            }
            else {
                final var contentType = headers.get("content-type");
                if (contentType != null) {
                    defaultEncoding = HttpMediaTypes.findEncodingCompatibleWithContentType(encodings, contentType)
                        .orElse(service.defaultEncoding());
                }
                else {
                    sendEmptyResponseAndCleanup(ctx, UNSUPPORTED_MEDIA_TYPE);
                    return;
                }
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

        final var kalixResponse = new DefaultHttpServiceResponse();

        service
            .handle(this.kalixRequest, kalixResponse)
            .ifSuccess(ignored -> sendKalixResponseAndCleanup(ctx, kalixResponse, defaultEncoding))
            .onFailure(fault -> {
                if (fault instanceof HttpServiceRequestException) {
                    sendEmptyResponseAndCleanup(ctx, convert(((HttpServiceRequestException) fault).status()));
                }
                else if (fault instanceof DtoReadException) {
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
        kalixRequest.append(content);
        if (content instanceof LastHttpContent) {
            kalixRequest.headers().unwrap().add(((LastHttpContent) content).trailingHeaders());
            kalixRequest.finish();
            kalixRequest = null;
        }
    }

    private void sendKalixResponseAndCleanup(
        final ChannelHandlerContext ctx,
        final DefaultHttpServiceResponse response,
        final EncodingDescriptor defaultEncoding
    ) throws DtoWriteException, IOException {
        final var status = response.status()
            .orElseThrow(() -> new IllegalStateException("No HTTP status specified in service response"));

        final var nettyStatus = convert(status);
        final var nettyVersion = response.version()
            .map(NettyHttpConverters::convert)
            .orElse(nettyRequest.protocolVersion());
        final var nettyHeaders = response.headers().unwrap();

        HttpUtil.setKeepAlive(nettyHeaders, nettyVersion, !isClosing);

        if (!nettyHeaders.contains(CONTENT_TYPE)) {
            final var encoding = response.encoding().orElse(null);
            if (encoding == null) {
                nettyHeaders.set(CONTENT_TYPE, TEXT_PLAIN + ";charset=" + response.charset()
                    .orElse(StandardCharsets.UTF_8)
                    .name()
                    .toLowerCase());
            }
            else {
                nettyHeaders.set(CONTENT_TYPE, HttpMediaTypes.toMediaType(encoding));
            }
        }

        final var body = NettyBodyOutgoing.from(response, ctx.alloc(), defaultEncoding);

        if (!nettyHeaders.contains(CONTENT_LENGTH)) {
            nettyHeaders.set(CONTENT_LENGTH, Long.toString(body.length()));
        }

        final var channel = ctx.channel();
        channel.write(new DefaultHttpResponse(nettyVersion, nettyStatus, nettyHeaders));
        body.writeTo(channel);
        final var future = channel.write(LastHttpContent.EMPTY_LAST_CONTENT);

        if (isClosing) {
            future.addListener(ChannelFutureListener.CLOSE);
        }

        cleanup();
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, Throwable cause) {
        try {
            if (kalixRequest != null) {
                kalixRequest.tryAbort(cause);
            }
        }
        catch (final Throwable throwable) {
            throwable.addSuppressed(cause);
            cause = throwable;
        }
        finally {
            sendInternalServerErrorLogAndCleanup(ctx, cause);
        }
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (!(evt instanceof IdleStateEvent)) {
            return;
        }
        isClosing = true;
        try {
            final var idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                if (kalixRequest != null) {
                    final var exception = new HttpServiceRequestException(HttpStatus.REQUEST_TIMEOUT);
                    if (kalixRequest.tryAbort(exception)) {
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

        if (!isClosing) {
            HttpUtil.setKeepAlive(headers, version, true);
        }

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
