package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.dto.DataReadable;
import eu.arrowhead.kalix.dto.data.DataByteArray;
import eu.arrowhead.kalix.dto.data.DataString;
import eu.arrowhead.kalix.net.http.HttpStatus;
import eu.arrowhead.kalix.net.http.service.HttpServiceRequestBody;
import eu.arrowhead.kalix.net.http.service.HttpServiceRequestException;
import eu.arrowhead.kalix.util.Result;
import eu.arrowhead.kalix.util.concurrent.Future;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;

public class NettyHttpServiceRequestBody implements HttpServiceRequestBody {
    private final ByteBufAllocator alloc;
    private final HttpHeaders headers;

    private Appender appender;
    private Queue<HttpContent> contentQueue;

    public NettyHttpServiceRequestBody(final ByteBufAllocator alloc, final HttpHeaders headers) {
        this.alloc = alloc;
        this.headers = headers;
    }

    public void append(final HttpContent content) {
        if (appender == null) {
            if (contentQueue == null) {
                contentQueue = new PriorityQueue<>();
            }
            contentQueue.add(content);
            return;
        }

        if (appender.isCancelled()) {
            return;
        }

        if (contentQueue != null) {
            for (final var queuedContent : contentQueue) {
                appender.append(queuedContent);
            }
            contentQueue = null;
        }

        // TODO: Ensure body size does not exceed some configured limit.
        appender.append(content);
    }

    public void finish(final LastHttpContent lastContent) {
        headers.add(lastContent.trailingHeaders());
        appender.finish();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends DataReadable> Future<? extends R> bodyAs(final Class<R> class_) {
        if (appender != null) {
            throw new IllegalStateException("bodyAs() has already been called for this request");
        }

        if (class_ == DataByteArray.class) {
            return (Future<? extends R>) (appender = new AppenderFutureDataByteArray(alloc, headers));
        }

        if (class_ == DataString.class) {
            return (Future<? extends R>) (appender = new AppenderFutureDataString(alloc, headers));
        }

        throw new IllegalStateException("Unexpected class \"" + class_ +
            "\"; only generated and special DTO classes are supported");
    }

    private interface Appender {
        void append(final HttpContent content);

        void finish();

        boolean isCancelled();
    }

    private static abstract class AppenderFuture<V> implements Appender, Future<V> {
        private Consumer<Result<V>> consumer = null;
        private boolean isCancelled = false;

        protected void fulfill(final Result<V> result) {
            if (consumer != null) {
                consumer.accept(isCancelled
                    ? Result.failure(new CancellationException())
                    : result);
                consumer = null;
            }
        }

        @Override
        public boolean isCancelled() {
            return isCancelled;
        }

        @Override
        public void onResult(final Consumer<Result<V>> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void cancel(final boolean mayInterruptIfRunning) {
            isCancelled = true;
        }
    }

    private static abstract class AppenderFutureAggregating<V> extends AppenderFuture<V> {
        private final ByteBuf buffer;

        protected AppenderFutureAggregating(final ByteBufAllocator alloc, final HttpHeaders headers) {
            var contentLength = headers.getInt("content-length");
            if (contentLength != null) {
                if (contentLength < 0) {
                    throw new HttpServiceRequestException(HttpStatus.BAD_REQUEST,
                        "Bad content-length header");
                }
                buffer = alloc.buffer(contentLength);
            }
            else {
                buffer = alloc.buffer();
            }
        }

        public abstract V assembleValue(byte[] byteArray);

        @Override
        public void append(final HttpContent content) {
            buffer.writeBytes(content.content());
        }

        @Override
        public void finish() {
            final var byteArray = new byte[buffer.readableBytes()];
            buffer.readBytes(byteArray);
            buffer.release();
            fulfill(Result.success(assembleValue(byteArray)));
        }
    }

    private static class AppenderFutureDataByteArray extends AppenderFutureAggregating<DataByteArray> {
        public AppenderFutureDataByteArray(final ByteBufAllocator alloc, final HttpHeaders headers) {
            super(alloc, headers);
        }

        @Override
        public DataByteArray assembleValue(final byte[] byteArray) {
            return new DataByteArray(byteArray);
        }
    }

    private static class AppenderFutureDataString extends AppenderFutureAggregating<DataString> {
        private final Charset charset;

        public AppenderFutureDataString(final ByteBufAllocator alloc, final HttpHeaders headers) {
            super(alloc, headers);
            charset = HttpUtil.getCharset(headers.get("content-type"), StandardCharsets.UTF_8);
        }

        @Override
        public DataString assembleValue(final byte[] byteArray) {
            return new DataString(new String(byteArray, charset));
        }
    }
}
