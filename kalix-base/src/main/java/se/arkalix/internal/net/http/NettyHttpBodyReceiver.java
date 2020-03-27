package se.arkalix.internal.net.http;

import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadable;
import se.arkalix.dto.DtoReader;
import se.arkalix.dto.DtoReadException;
import se.arkalix.internal.dto.binary.ByteBufReader;
import se.arkalix.net.http.HttpBodyReceiver;
import se.arkalix.util.Result;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.concurrent.FutureProgress;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.CompositeByteBuf;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Internal
public class NettyHttpBodyReceiver implements HttpBodyReceiver {
    private final ByteBufAllocator alloc;
    private final EncodingDescriptor encoding;
    private final HttpHeaders headers;

    private FutureBody<?> body;

    private boolean isAborted = false;
    private boolean isBodyRequested = false;
    private boolean isFinished = false;

    public NettyHttpBodyReceiver(final ByteBufAllocator alloc, final HttpHeaders headers) {
        this(alloc, headers, null);
    }

    public NettyHttpBodyReceiver(
        final ByteBufAllocator alloc,
        final HttpHeaders headers,
        final EncodingDescriptor encoding)
    {
        this.alloc = Objects.requireNonNull(alloc, "Expected alloc");
        this.headers = Objects.requireNonNull(headers, "Expected headers");
        this.encoding = encoding;
    }

    public boolean tryAbort(final Throwable throwable) {
        Objects.requireNonNull(throwable, "Expected throwable");
        if (isAborted || isFinished || !isBodyRequested) {
            return false;
        }
        isAborted = true;

        body.abort(throwable);
        return true;
    }

    public void append(final HttpContent content) {
        if (isAborted || isFinished || !isBodyRequested) {
            return;
        }
        if (body.isCancelled()) {
            return;
        }
        // TODO: Ensure body size does not exceed some configured limit.
        body.append(content);
    }

    public void finish() {
        finish(null);
    }

    public void finish(final LastHttpContent lastContent) {
        if (isAborted || isFinished || !isBodyRequested) {
            return;
        }
        isFinished = true;

        if (lastContent != null) {
            headers.add(lastContent.trailingHeaders());
        }
        body.finish();
    }

    public <R extends DtoReadable> FutureProgress<R> bodyAs(final Class<R> class_) {
        if (encoding == null) {
            throw new IllegalStateException("No default encoding has been set");
        }
        final var dataEncoding = encoding.asDtoEncoding()
            .orElseThrow(() -> new UnsupportedOperationException("" +
                "There is no DTO support for the \"" + encoding +
                "\" encoding; request body cannot be decoded"));

        return handleBodyRequest(() -> new FutureBodyAs<>(alloc, headers, class_, dataEncoding));
    }

    @Override
    public <R extends DtoReadable> FutureProgress<R> bodyAs(final DtoEncoding encoding, final Class<R> class_) {
        return handleBodyRequest(() -> new FutureBodyAs<>(alloc, headers, class_, encoding));
    }

    @Override
    public FutureProgress<byte[]> bodyAsByteArray() {
        return handleBodyRequest(() -> new FutureBodyAsByteArray(alloc, headers));
    }

    @Override
    public FutureProgress<? extends InputStream> bodyAsStream() {
        return handleBodyRequest(() -> new FutureBodyAsStream(alloc, headers));
    }

    @Override
    public FutureProgress<String> bodyAsString() {
        return handleBodyRequest(() -> new FutureBodyAsString(alloc, headers));
    }

    @Override
    public FutureProgress<Path> bodyTo(final Path path, final boolean append) {
        return handleBodyRequest(() -> new FutureBodyToPath(path, append, headers));
    }

    private <V> FutureProgress<V> handleBodyRequest(final Supplier<FutureBody<V>> futureBodySupplier) {
        if (isAborted) {
            throw new IllegalStateException("Incoming HTTP body has already " +
                "been aborted");
        }
        if (isFinished) {
            throw new IllegalStateException("Incoming HTTP body has already " +
                "been received and discarded since it wasn't immediately " +
                "listened for");
        }
        if (isBodyRequested) {
            throw new IllegalStateException("Incoming HTTP body has already " +
                "been requested; the handler or other context that requests " +
                "an HTTP body must also make sure to handle it");
        }
        isBodyRequested = true;

        final var body = futureBodySupplier.get();
        this.body = body;
        return body;
    }

    private static abstract class FutureBody<V> implements FutureProgress<V> {
        private final int expectedContentLength;

        private Consumer<Result<V>> consumer = null;
        private Result<V> pendingResult = null;
        private Listener listener = null;
        private boolean isCancelled = false;
        private boolean isCompleted = false;
        private int currentProgress = 0;

        protected FutureBody(final HttpHeaders headers) {
            this.expectedContentLength = headers.getInt("content-length", 0);
        }

        public void abort(final Throwable throwable) {
            complete(Result.failure(throwable));
        }

        public void append(final HttpContent content) {
            final var buffer = content.content();
            if (listener != null) {
                currentProgress += buffer.readableBytes();
                try {
                    listener.onProgress(currentProgress, Math.max(currentProgress, expectedContentLength));
                }
                catch (final Throwable throwable) {
                    complete(Result.failure(throwable));
                }
            }
            append(buffer);
        }

        protected abstract void append(ByteBuf buffer);

        protected void complete(final Result<V> result) {
            if (isCompleted) {
                return;
            }
            isCompleted = true;

            if (consumer != null) {
                consumer.accept(isCancelled
                    ? Result.failure(new CancellationException())
                    : result);
            }
            else {
                pendingResult = result;
            }
        }

        public abstract void finish();

        public boolean isCancelled() {
            return isCancelled;
        }

        @Override
        public Future<V> addProgressListener(final Listener listener) {
            this.listener = listener;
            return this;
        }

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

    private static abstract class FutureBodyBuffered<V> extends FutureBody<V> {
        private final CompositeByteBuf buffer;

        private FutureBodyBuffered(final ByteBufAllocator alloc, final HttpHeaders headers) {
            super(headers);
            buffer = alloc.compositeBuffer();
        }

        public abstract V assembleValue(ByteBuf buffer);

        @Override
        public void append(final ByteBuf buffer) {
            buffer.retain();
            this.buffer.addComponent(true, buffer);
        }

        @Override
        public void finish() {
            complete(Result.success(assembleValue(buffer)));
        }
    }

    private static class FutureBodyAs<V extends DtoReadable> extends FutureBodyBuffered<V> {
        private final Class<V> class_;
        private final DtoEncoding encoding;

        private FutureBodyAs(
            final ByteBufAllocator alloc,
            final HttpHeaders headers,
            final Class<V> class_,
            final DtoEncoding encoding)
        {
            super(alloc, headers);
            this.class_ = class_;
            this.encoding = encoding;
        }

        @Override
        public V assembleValue(final ByteBuf buffer) {
            try {
                return DtoReader.read(class_, encoding, new ByteBufReader(buffer));
            }
            catch (final DtoReadException exception) {
                abort(exception);
                return null;
            }
            finally {
                buffer.release();
            }
        }
    }

    private static class FutureBodyAsByteArray extends FutureBodyBuffered<byte[]> {
        public FutureBodyAsByteArray(final ByteBufAllocator alloc, final HttpHeaders headers) {
            super(alloc, headers);
        }

        @Override
        public byte[] assembleValue(final ByteBuf buffer) {
            final var byteArray = new byte[buffer.readableBytes()];
            buffer.readBytes(byteArray);
            buffer.release();
            return byteArray;
        }
    }

    private static class FutureBodyToPath extends FutureBody<Path> {
        private final Path path;

        private FileOutputStream stream;

        public FutureBodyToPath(final Path path, final boolean append, final HttpHeaders headers) {
            super(headers);
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
        public void append(final ByteBuf buffer) {
            if (stream == null) {
                return;
            }
            try {
                final var length = buffer.readableBytes();
                buffer.readBytes(stream, length);
            }
            catch (final Throwable throwable) {
                abort(throwable);
            }
        }

        @Override
        public void finish() {
            if (stream == null) {
                return;
            }
            Result<Path> result;
            try {
                stream.close();
                result = Result.success(path);
            }
            catch (final Throwable throwable) {
                result = Result.failure(throwable);
            }
            complete(result);
        }
    }

    private static class FutureBodyAsStream extends FutureBodyBuffered<InputStream> {
        private FutureBodyAsStream(final ByteBufAllocator alloc, final HttpHeaders headers) {
            super(alloc, headers);
        }

        @Override
        public InputStream assembleValue(final ByteBuf buffer) {
            return new ByteBufInputStream(buffer);
        }
    }

    private static class FutureBodyAsString extends FutureBodyBuffered<String> {
        private final Charset charset;

        public FutureBodyAsString(final ByteBufAllocator alloc, final HttpHeaders headers) {
            super(alloc, headers);
            charset = HttpUtil.getCharset(headers.get("content-type"), StandardCharsets.UTF_8);
        }

        @Override
        public String assembleValue(final ByteBuf buffer) {
            final var byteArray = new byte[buffer.readableBytes()];
            buffer.readBytes(byteArray);
            buffer.release();
            return new String(byteArray, charset);
        }
    }
}
