package se.arkalix.io._internal;

import se.arkalix.util.annotation.Internal;
import se.arkalix.util.annotation.ThreadSafe;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Internal
public record NioEventLoopTask(Runnable runnable, long baselineEpochMs) implements Comparable<NioEventLoopTask>, Runnable {
    public NioEventLoopTask {
        Objects.requireNonNull(runnable);
    }

    @ThreadSafe
    public static NioEventLoopTask of(final Runnable runnable) {
        return new NioEventLoopTask(runnable, 0L);
    }

    @ThreadSafe
    public static NioEventLoopTask of(final Runnable runnable, final Instant baseline) {
        if (baseline == null) {
            throw new NullPointerException("baseline");
        }
        return new NioEventLoopTask(runnable, baseline.toEpochMilli());
    }

    @ThreadSafe
    public static NioEventLoopTask of(final Runnable runnable, final Duration baseline) {
        if (baseline == null) {
            throw new NullPointerException("baseline");
        }
        return new NioEventLoopTask(runnable, Instant.now().plus(baseline).toEpochMilli());
    }

    @Override
    public int compareTo(final NioEventLoopTask task) {
        return baselineEpochMs > task.baselineEpochMs ? 1 : -1;
    }

    @Override
    public void run() {
        runnable.run();
    }
}
