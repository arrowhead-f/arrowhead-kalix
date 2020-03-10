package eu.arrowhead.kalix.internal.net.http.client;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.dto.DataWritable;
import eu.arrowhead.kalix.dto.DataWriter;
import eu.arrowhead.kalix.dto.WriteException;
import eu.arrowhead.kalix.internal.dto.binary.ByteBufWriter;
import eu.arrowhead.kalix.internal.net.http.NettyHttpAdapters;
import eu.arrowhead.kalix.net.http.HttpPeer;
import eu.arrowhead.kalix.net.http.HttpVersion;
import eu.arrowhead.kalix.net.http.client.HttpClient;
import eu.arrowhead.kalix.net.http.client.HttpClientRequest;
import eu.arrowhead.kalix.net.http.client.HttpClientResponse;
import eu.arrowhead.kalix.util.Result;
import eu.arrowhead.kalix.util.annotation.Internal;
import eu.arrowhead.kalix.util.concurrent.Future;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;

import static eu.arrowhead.kalix.internal.util.concurrent.NettyFutures.adapt;

@Internal
public class NettyHttpClient implements HttpClient, HttpPeer {
    private final EncodingDescriptor encoding;
    private final Queue<FutureResponse> pendingResponseQueue = new LinkedList<>();

    private Channel channel = null;
    private X509Certificate[] certificateChain = null;

    public NettyHttpClient(final EncodingDescriptor encoding) {
        this.encoding = encoding;
    }

    @Override
    public X509Certificate[] certificateChain() {
        if (certificateChain == null) {
            throw new IllegalStateException("Not in secure mode");
        }
        return certificateChain;
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
    public Future<HttpClientResponse> send(final HttpClientRequest request) {
        try {
            writeRequestToChannel(request);
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
        final var pendingResponse = new FutureResponse();
        pendingResponseQueue.add(pendingResponse);
        return pendingResponse;
    }

    private void writeRequestToChannel(final HttpClientRequest request) throws WriteException, IOException {
        final var body = request.body().orElse(null);
        final var headers = request.headers().unwrap();
        final var method = request.method().orElseThrow(() -> new IllegalArgumentException("Expected method"));
        final var uri = request.uri().orElseThrow(() -> new IllegalArgumentException("Expected uri"));
        final var version = request.version().orElse(HttpVersion.HTTP_11);

        final ByteBuf content;
        if (body == null) {
            content = Unpooled.EMPTY_BUFFER;
        }
        else if (body instanceof byte[]) {
            content = Unpooled.wrappedBuffer((byte[]) body);
        }
        else if (body instanceof DataWritable) {
            final var dataEncoding = encoding.asDataEncoding().orElseThrow(() -> new UnsupportedOperationException("" +
                "There is no DTO support for the \"" + encoding +
                "\" encoding; request body cannot be encoded"));

            content = channel.alloc().buffer();
            DataWriter.write((DataWritable) body, dataEncoding, new ByteBufWriter(content));
            if (headers.get(HttpHeaderNames.CONTENT_TYPE) == null) {
                headers.set(HttpHeaderNames.CONTENT_TYPE, dataEncoding.asMediaType());
            }
        }
        else if (body instanceof Path) {
            final var path = (Path) body;
            final var file = new RandomAccessFile(path.toFile(), "r");
            final var length = file.length();
            final var response = new DefaultHttpRequest(
                NettyHttpAdapters.adapt(version),
                NettyHttpAdapters.adapt(method),
                uri,
                headers);

            HttpUtil.setContentLength(response, length);
            channel.write(new DefaultFileRegion(file.getChannel(), 0, length));
            channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            return;
        }
        else if (body instanceof String) {
            final var charset = HttpUtil.getCharset(headers.get("content-type"), StandardCharsets.UTF_8);
            content = Unpooled.wrappedBuffer(((String) body).getBytes(charset));
        }
        else {
            throw new IllegalStateException("Invalid response body supplied \"" + body + "\"");
        }

        headers
            .set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes())
            .set(HttpHeaderNames.HOST, remoteSocketAddress().getHostString())
            .add(HttpHeaderNames.ACCEPT, "application/" + encoding.name().toLowerCase());

        channel.writeAndFlush(new DefaultFullHttpRequest(
            NettyHttpAdapters.adapt(version),
            NettyHttpAdapters.adapt(method),
            uri,
            content,
            headers,
            EmptyHttpHeaders.INSTANCE));
    }

    @Override
    public Future<?> close() {
        return adapt(channel.close());
    }

    /**
     * @param certificateChain The certificate chain, if any, that represents
     *                         the remote host communicated with via this
     *                         client.
     */
    public void setCertificateChain(final X509Certificate[] certificateChain) {
        this.certificateChain = certificateChain;
    }

    /**
     * @param channel The channel through which requests are to be sent.
     */
    public void setChannel(final Channel channel) {
        this.channel = channel;
    }

    public boolean onResponseResult(final Result<HttpClientResponse> result) {
        final var pendingResponse = pendingResponseQueue.poll();
        if (pendingResponse == null) {
            throw new IllegalStateException("No pending response available", result.isSuccess()
                ? null
                : result.fault());
        }
        return pendingResponse.setResult(result);
    }

    private static class FutureResponse implements Future<HttpClientResponse> {
        private Consumer<Result<HttpClientResponse>> consumer = null;
        private boolean isDone = false;
        private Result<HttpClientResponse> pendingResult = null;

        @Override
        public void onResult(final Consumer<Result<HttpClientResponse>> consumer) {
            if (isDone) {
                return;
            }
            if (pendingResult != null) {
                consumer.accept(pendingResult);
                isDone = true;
            }
            else {
                this.consumer = consumer;
            }
        }

        /*
         * Cancelling simply causes the response to be ignored. If not wanting
         * the response to be received at all the client must be closed.
         */
        @Override
        public void cancel(final boolean mayInterruptIfRunning) {
            if (isDone) {
                return;
            }
            setResult(Result.failure(new CancellationException()));
        }

        public boolean setResult(final Result<HttpClientResponse> result) {
            if (isDone) {
                return false;
            }
            if (consumer != null) {
                consumer.accept(result);
                isDone = true;
            }
            else {
                pendingResult = result;
            }
            return true;
        }
    }
}
