package eu.arrowhead.kalix.internal.util.concurrent;

import eu.arrowhead.kalix.util.annotation.Internal;
import eu.arrowhead.kalix.util.concurrent.Future;
import io.netty.channel.EventLoopGroup;

import java.time.Duration;
import java.util.concurrent.ThreadFactory;

/**
 * Reference-counted {@link NettyScheduler} instance.
 * <p>
 * This {@code NettyScheduler} can have its {@link #shutdown(Duration)} method
 * called without actually being shut down, as long as it holds a sufficiently
 * large internal reference count. This is useful, in particular, to ensure a
 * scheduler is not shut down until all users of it have signaled that they
 * have no more interest in using it.
 */
@Internal
public class NettySchedulerReferenceCounted extends NettyScheduler {
    private static NettySchedulerReferenceCounted defaultScheduler = null;

    private long referenceCount = 1;

    /**
     * Creates new {@code Scheduler} from given {@code EventLoopGroup}.
     *
     * @param eventLoopGroup Thread pool to wrap.
     */
    public NettySchedulerReferenceCounted(final EventLoopGroup eventLoopGroup) {
        super(eventLoopGroup);
    }

    /**
     * Creates new {@code Scheduler} that uses {@code threadFactory} for thread
     * creation and fills its thread pool with {@code nThreads} number of
     * threads.
     *
     * @param threadFactory Factory to use for thread creation, or {@code null}
     *                      if the default one is to be used.
     * @param nThreads      The number of desired thread pool threads, or 0 for
     *                      the available number of CPU cores times two.
     */
    public NettySchedulerReferenceCounted(final ThreadFactory threadFactory, final int nThreads) {
        super(threadFactory, nThreads);
    }

    /**
     * @return Default {@code Scheduler} with a thread pool containing twice as
     * many threads as available system CPU cores.
     * <p>
     * Note that the same scheduler will always be returned by this static
     * method, no matter how many times it is called. Its reference count will,
     * however, increase every time.
     */
    public static synchronized NettyScheduler getDefault() {
        if (defaultScheduler == null) {
            defaultScheduler = new NettySchedulerReferenceCounted(null, 0);
        }
        else {
            defaultScheduler.retain();
        }
        return defaultScheduler;
    }

    /**
     * Increases reference count by one, making it necessary to call
     * {@link #shutdown(Duration)} one more time before this scheduler actually
     * shuts down.
     */
    public synchronized void retain() {
        if (referenceCount == Long.MAX_VALUE) {
            throw new IllegalStateException("Reference counter overflow");
        }
        referenceCount += 1;
    }

    @Override
    public synchronized Future<?> shutdown(final Duration timeout) {
        if (--referenceCount == 0) {
            return super.shutdown(timeout);
        }
        return Future.success(null);
    }
}
