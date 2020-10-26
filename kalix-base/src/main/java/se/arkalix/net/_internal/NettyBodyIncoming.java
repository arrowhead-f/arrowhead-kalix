package se.arkalix.net._internal;

import io.netty.buffer.*;
import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadException;
import se.arkalix.dto.DtoReadable;
import se.arkalix.dto.binary._internal.ByteBufReader;
import se.arkalix.util.Result;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.concurrent.FutureProgress;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;

public abstract class NettyBodyIncoming<V> implements FutureProgress<V> {
    private final int expectedLength;

    private int currentLength = 0;

    private Consumer<Result<V>> consumer = null;
    private Result<V> pendingResult = null;
    private Listener listener = null;

    private boolean isCancelled = false;
    private boolean isCompleted = false;

    protected NettyBodyIncoming(final int expectedLength) {
        if (expectedLength < 0) {
            throw new IllegalArgumentException("`size` < 0");
        }
        this.expectedLength = expectedLength;
    }

    public void abort(final Throwable throwable) {
        complete(Result.failure(throwable));
    }

    public void append(final ByteBufHolder content) {
        final var buffer = content.content();
        if (listener != null) {
            currentLength += buffer.readableBytes();
            try {
                listener.onProgress(currentLength, Math.max(currentLength, expectedLength));
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

    private static abstract class Buffered<V> extends NettyBodyIncoming<V> {
        private final CompositeByteBuf buffer;

        private Buffered(final ByteBufAllocator alloc, final int expectedLength) {
            super(expectedLength);
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

    static class As<V extends DtoReadable> extends Buffered<V> {
        private final Class<V> class_;
        private final DtoEncoding encoding;

        public As(
            final ByteBufAllocator alloc,
            final int expectedLength,
            final Class<V> class_,
            final DtoEncoding encoding
        ) {
            super(alloc, expectedLength);
            this.class_ = class_;
            this.encoding = encoding;
        }

        @Override
        public V assembleValue(final ByteBuf buffer) {
            try {
                return encoding.reader().readOne(class_, new ByteBufReader(buffer));
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

    static class AsByteArray extends Buffered<byte[]> {
        public AsByteArray(final ByteBufAllocator alloc, final int expectedLength) {
            super(alloc, expectedLength);
        }

        @Override
        public byte[] assembleValue(final ByteBuf buffer) {
            final var byteArray = new byte[buffer.readableBytes()];
            buffer.readBytes(byteArray);
            buffer.release();
            return byteArray;
        }
    }

    static class AsList<V extends DtoReadable> extends Buffered<List<V>> {
        private final Class<V> class_;
        private final DtoEncoding encoding;

        public AsList(
            final ByteBufAllocator alloc,
            final int expectedLength,
            final Class<V> class_,
            final DtoEncoding encoding
        ) {
            super(alloc, expectedLength);
            this.class_ = class_;
            this.encoding = encoding;
        }

        @Override
        public List<V> assembleValue(final ByteBuf buffer) {
            try {
                return encoding.reader().readMany(class_, new ByteBufReader(buffer));
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

    static class AsStream extends Buffered<InputStream> {
        public AsStream(final ByteBufAllocator alloc, final int expectedLength) {
            super(alloc, expectedLength);
        }

        @Override
        public InputStream assembleValue(final ByteBuf buffer) {
            return new ByteBufInputStream(buffer);
        }
    }

    static class AsString extends Buffered<String> {
        private final Charset charset;

        public AsString(final ByteBufAllocator alloc, final int expectedLength, final Charset charset) {
            super(alloc, expectedLength);
            this.charset = charset != null ? charset : StandardCharsets.UTF_8;
        }

        @Override
        public String assembleValue(final ByteBuf buffer) {
            final var byteArray = new byte[buffer.readableBytes()];
            buffer.readBytes(byteArray);
            buffer.release();
            return new String(byteArray, charset);
        }
    }

    static class To extends NettyBodyIncoming<Path> {
        private final Path path;
        private final FileOutputStream stream;

        public To(final Path path, final boolean append, final int expectedLength) {
            super(expectedLength);
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
}
