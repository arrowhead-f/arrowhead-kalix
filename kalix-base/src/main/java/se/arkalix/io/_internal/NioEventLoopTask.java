package se.arkalix.io._internal;

import se.arkalix.util.annotation.Internal;
import se.arkalix.util.annotation.ThreadSafe;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Internal
public class NioEventLoopTask implements Comparable<NioEventLoopTask>, Runnable {
    private final Runnable runnable;
    private final long baselineEpochMs;

    public NioEventLoopTask(final Runnable runnable, final long baselineEpochMs) {
        this.runnable = Objects.requireNonNull(runnable, "runnable");
        this.baselineEpochMs = baselineEpochMs;
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

    @ThreadSafe
    public long baselineEpochMs() {
        return baselineEpochMs;
    }
}
