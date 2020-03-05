package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.dto.DataReadable;
import eu.arrowhead.kalix.dto.data.DataByteArray;
import eu.arrowhead.kalix.dto.data.DataStream;
import eu.arrowhead.kalix.dto.data.DataString;
import eu.arrowhead.kalix.net.http.service.HttpServiceRequestBody;
import eu.arrowhead.kalix.util.Result;
import eu.arrowhead.kalix.util.concurrent.Future;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.CompositeByteBuf;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;

import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;

public class NettyHttpServiceRequestBody implements HttpServiceRequestBody {
    private final ByteBufAllocator alloc;
    private final HttpHeaders headers;

    private BodyFuture<?> body;
    private Queue<HttpContent> pendingContent;
    private Throwable pendingThrowable;

    private boolean isAborted = false;
    private boolean isBodyRequested = false;
    private boolean isFinished = false;

    public NettyHttpServiceRequestBody(final ByteBufAllocator alloc, final HttpHeaders headers) {
        this.alloc = alloc;
        this.headers = headers;
    }

    public void abort(final Throwable throwable) {
        if (isAborted) {
            throw new IllegalStateException("Already aborted");
        }
        if (isFinished) {
            throw new IllegalStateException("Cannot abort; body finished");
        }
        isAborted = true;

        if (isBodyRequested) {
            body.abort(throwable);
        }
        else {
            pendingThrowable = throwable;
        }
    }

    public void append(final HttpContent content) {
        if (isAborted) {
            throw new IllegalStateException("Cannot append; body aborted");
        }
        if (isFinished) {
            throw new IllegalStateException("Cannot append; body finished");
        }

        if (!isBodyRequested) {
            if (pendingContent == null) {
                pendingContent = new PriorityQueue<>();
            }
            pendingContent.add(content);
            return;
        }

        if (body.isCancelled()) {
            return;
        }

        if (pendingContent != null) {
            for (final var content0 : pendingContent) {
                body.append(content0);
            }
            pendingContent = null;
        }

        // TODO: Ensure body size does not exceed some configured limit.
        body.append(content);
    }

    public void finish(final LastHttpContent lastContent) {
        if (isAborted) {
            throw new IllegalStateException("Cannot finish; body aborted");
        }
        if (isFinished) {
            throw new IllegalStateException("Already finished");
        }
        isFinished = true;

        // `headers` is the same map of headers already passed on in a
        // `HttpServiceRequest` via the `HttpServiceRequestHandler`. By adding
        // the trailing headers to the `headers` map here, they are being made
        // visible via the same `HttpServiceRequest`.
        headers.add(lastContent.trailingHeaders());

        if (isBodyRequested) {
            body.finish();
        }
    }

    @Override
    public <R extends DataReadable> Future<? extends R> bodyAs(final Class<R> class_) {
        if (isBodyRequested) {
            throw new IllegalStateException("The body of this request has " +
                "already been requested");
        }
        isBodyRequested = true;

        if (isAborted) {
            return Future.failure(pendingThrowable);
        }

        if (class_ == DataByteArray.class) {
            body = new BodyFutureDataByteArray(alloc);
        }
        else if (class_ == DataStream.class) {
            body = new BodyFutureDataStream(alloc);
        }
        else if (class_ == DataString.class) {
            body = new BodyFutureDataString(alloc, headers);
        }
        // TODO: Handle generated DTO classes.
        if (body == null) {
            throw new IllegalStateException("Unexpected class \"" + class_ +
                "\"; only generated and special DTO classes may be " +
                "requested as response bodies");
        }

        if (isFinished) {
            if (pendingContent != null) {
                for (final var content : pendingContent) {
                    body.append(content);
                }
                pendingContent = null;
            }
            body.finish();
        }

        return uncheckedCast(body);
    }

    @SuppressWarnings("unchecked")
    private <R> Future<? extends R> uncheckedCast(BodyFuture<?> bodyFuture) {
        return (Future<? extends R>) bodyFuture;
    }

    @Override
    public Future<Path> bodyTo(final Path path, final boolean append) {
        if (isBodyRequested) {
            throw new IllegalStateException("The body of this request has " +
                "already been requested");
        }
        isBodyRequested = true;

        if (isAborted) {
            return Future.failure(pendingThrowable);
        }

        final var appender = new BodyFutureDataPath(path, append);

        if (isFinished) {
            if (pendingContent != null) {
                for (final var content : pendingContent) {
                    appender.append(content);
                }
                pendingContent = null;
            }
            appender.finish();
        }
        else {
            this.body = appender;
        }

        return appender;
    }

    private static abstract class BodyFuture<V> implements Future<V> {
        private Consumer<Result<V>> consumer = null;
        private Result<V> pendingResult = null;
        private boolean isCancelled = false;

        protected void fulfill(final Result<V> result) {
            if (consumer != null) {
                consumer.accept(isCancelled
                    ? Result.failure(new CancellationException())
                    : result);
            }
            else {
                pendingResult = result;
            }
        }

        public void abort(final Throwable throwable) {
            fulfill(Result.failure(throwable));
        }

        public abstract void append(HttpContent content);


        public boolean isCancelled() {
            return isCancelled;
        }

        public abstract void finish();

        @Override
        public void onResult(final Consumer<Result<V>> consumer) {
            if (pendingResult != null) {
                consumer.accept(pendingResult);
            }
            else {
                this.consumer = consumer;
            }
        }

        @Override
        public void cancel(final boolean mayInterruptIfRunning) {
            isCancelled = true;
        }
    }

    private static abstract class BodyFutureBuffering<V> extends BodyFuture<V> {
        private final CompositeByteBuf buffer;

        private BodyFutureBuffering(final ByteBufAllocator alloc) {
            buffer = alloc.compositeBuffer();
        }

        public abstract V assembleValue(ByteBuf buffer);

        @Override
        public void append(final HttpContent content) {
            buffer.addComponent(content.content());
        }

        @Override
        public void finish() {
            fulfill(Result.success(assembleValue(buffer)));
        }
    }

    private static class BodyFutureDataByteArray extends BodyFutureBuffering<DataByteArray> {
        public BodyFutureDataByteArray(final ByteBufAllocator alloc) {
            super(alloc);
        }

        @Override
        public DataByteArray assembleValue(final ByteBuf buffer) {
            final var byteArray = new byte[buffer.readableBytes()];
            buffer.readBytes(byteArray);
            buffer.release();
            return new DataByteArray(byteArray);
        }
    }

    private static class BodyFutureDataPath extends BodyFuture<Path> {
        private final Path path;

        private FileOutputStream stream;

        public BodyFutureDataPath(final Path path, final boolean append) {
            FileOutputStream stream;
            try {
                stream = new FileOutputStream(path.toFile(), append);
            }
            catch (final Throwable throwable) {
                abort(throwable);
                stream = null;
            }
            this.path = path;
            this.stream = stream;
        }

        @Override
        public void append(final HttpContent content) {
            if (stream == null) {
                return;
            }
            final var buffer = content.content();
            try {
                buffer.readBytes(stream, buffer.readableBytes());
            }
            catch (final Throwable throwable) {
                abort(throwable);
                stream = null;
            }
        }

        @Override
        public void finish() {
            Result<Path> result;
            if (stream != null) {
                try {
                    stream.close();
                    result = Result.success(path);
                }
                catch (final Throwable throwable) {
                    result = Result.failure(throwable);
                }
                fulfill(result);
            }
            // If stream is null, we have already presented a Throwable to the
            // consumer of this Future.
        }
    }

    private static class BodyFutureDataStream extends BodyFutureBuffering<DataStream> {
        private BodyFutureDataStream(final ByteBufAllocator alloc) {
            super(alloc);
        }

        @Override
        public DataStream assembleValue(final ByteBuf buffer) {
            return new DataStream(new ByteBufInputStream(buffer, true));
        }
    }

    private static class BodyFutureDataString extends BodyFutureBuffering<DataString> {
        private final Charset charset;

        public BodyFutureDataString(final ByteBufAllocator alloc, final HttpHeaders headers) {
            super(alloc);
            charset = HttpUtil.getCharset(headers.get("content-type"), StandardCharsets.UTF_8);
        }

        @Override
        public DataString assembleValue(final ByteBuf buffer) {
            final var byteArray = new byte[buffer.readableBytes()];
            buffer.readBytes(byteArray);
            buffer.release();
            return new DataString(new String(byteArray, charset));
        }
    }
}
