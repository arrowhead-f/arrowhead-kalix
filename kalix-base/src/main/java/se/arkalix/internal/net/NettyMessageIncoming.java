package se.arkalix.internal.net;

import io.netty.buffer.*;
import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadable;
import se.arkalix.net.MessageIncoming;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.FutureProgress;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Internal
public abstract class NettyMessageIncoming implements MessageIncoming {
    private final ByteBufAllocator alloc;
    private final int expectedBodyLength;

    private NettyBodyIncoming<?> futureBody;

    private boolean isAborted = false;
    private boolean isRequested = false;
    private boolean isFinished = false;

    protected NettyMessageIncoming(
        final ByteBufAllocator alloc,
        final int expectedBodyLength
    ) {
        this.alloc = Objects.requireNonNull(alloc);
        this.expectedBodyLength = expectedBodyLength;
    }

    public boolean tryAbort(final Throwable throwable) {
        Objects.requireNonNull(throwable);
        if (isAborted || isFinished || !isRequested) {
            return false;
        }
        isAborted = true;

        futureBody.abort(throwable);
        return true;
    }

    public void append(final ByteBufHolder byteBufHolder) {
        if (isAborted || isFinished || !isRequested) {
            return;
        }
        if (futureBody.isCancelled()) {
            return;
        }
        // TODO: Ensure body size does not exceed some configured limit.
        futureBody.append(byteBufHolder);
    }

    public void finish() {
        if (isAborted || isFinished || !isRequested) {
            return;
        }
        isFinished = true;
        futureBody.finish();
    }

    @Override
    public <R extends DtoReadable> FutureProgress<R> bodyAs(final DtoEncoding encoding, final Class<R> class_) {
        return request(() -> new NettyBodyIncoming.As<>(alloc, expectedBodyLength, class_, encoding));
    }

    @Override
    public FutureProgress<byte[]> bodyAsByteArray() {
        return request(() -> new NettyBodyIncoming.AsByteArray(alloc, expectedBodyLength));
    }

    @Override
    public <R extends DtoReadable> FutureProgress<List<R>> bodyAsList(
        final DtoEncoding encoding,
        final Class<R> class_
    ) {
        return request(() -> new NettyBodyIncoming.AsList<>(alloc, expectedBodyLength, class_, encoding));
    }

    @Override
    public FutureProgress<? extends InputStream> bodyAsStream() {
        return request(() -> new NettyBodyIncoming.AsStream(alloc, expectedBodyLength));
    }

    @Override
    public FutureProgress<String> bodyAsString(final Charset charset) {
        return request(() -> new NettyBodyIncoming.AsString(alloc, expectedBodyLength, charset));
    }

    @Override
    public FutureProgress<Path> bodyTo(final Path path, final boolean append) {
        return request(() -> new NettyBodyIncoming.To(path, append, expectedBodyLength));
    }

    private <V> FutureProgress<V> request(final Supplier<NettyBodyIncoming<V>> supplier) {
        if (isAborted) {
            throw new IllegalStateException("Body receival has already been " +
                "aborted");
        }
        if (isFinished) {
            throw new IllegalStateException("Body receival has already been " +
                "completed and then discarded; bodies such as this must be " +
                "requested right after it is known they will become available");
        }
        if (isRequested) {
            throw new IllegalStateException("Body recevial has already been " +
                "requested; it may not be requested more than once");
        }
        isRequested = true;

        final var futureBody = supplier.get();
        this.futureBody = futureBody;
        return futureBody;
    }

}