package se.arkalix.net._internal;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.util.Result;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

@Internal
public class NettyBodyReceiverFileWriter implements NettyBodyReceiver, Future<Long> {
    private static final Logger logger = LoggerFactory.getLogger(NettyBodyReceiverFileWriter.class);

    private boolean isCancelled = false;
    private Consumer<Result<Long>> consumer;
    private long numberOfBytesWritten;
    private Result<Long> result;
    private FileOutputStream stream;

    public NettyBodyReceiverFileWriter(final Path path, final boolean append) {
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(Objects.requireNonNull(path, "path").toFile(), append);
        }
        catch (final Throwable throwable) {
            abort(throwable);
        }
        finally {
            this.stream = stream;
        }
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void abort(final Throwable cause) {
        result = Result.failure(cause);
        if (consumer != null) {
            final var consumer0 = consumer;
            consumer = null;
            final var result0 = result;
            result = null;
            try {
                consumer0.accept(result0);
            }
            catch (final Throwable throwable) {
                if (logger.isErrorEnabled()) {
                    throwable.addSuppressed(cause);
                    logger.error("Future result consumer unexpectedly threw " +
                        "exception after being notified about a fault", throwable);
                }
            }
        }
        closeStream();
    }

    private void closeStream() {
        try {
            stream.close();
        }
        catch (final IOException exception) {
            logger.debug("Failed to close file output stream", exception);
        }
        finally {
            stream = null;
        }
    }

    @Override
    public void write(final ByteBuf byteBuf) {
        if (stream == null) {
            return;
        }
        try {
            final var length = byteBuf.readableBytes();
            byteBuf.readBytes(stream, length);
            numberOfBytesWritten += length;
        }
        catch (final Throwable throwable) {
            abort(throwable);
        }
    }

    @Override
    public void close() {
        if (stream == null) {
            return;
        }
        Result<Long> result = null;
        try {
            stream.close();
            result = Result.success(numberOfBytesWritten);
            if (consumer != null) {
                final var consumer0 = consumer;
                consumer = null;
                consumer0.accept(result);
                result = null;
            }
        }
        catch (final Throwable throwable) {
            abort(throwable);
        }
        finally {
            this.result = result;
        }
    }

    @Override
    public void onResult(final Consumer<Result<Long>> consumer) {
        if (result != null) {
            final var result0 = result;
            result = null;
            try {
                consumer.accept(result0);
            }
            catch (final Throwable throwable) {
                abort(throwable);
            }
        }
        else {
            this.consumer = consumer;
        }
    }

    @Override
    public void cancel(final boolean mayInterruptIfRunning) {
        isCancelled = true;
        closeStream();
    }
}
