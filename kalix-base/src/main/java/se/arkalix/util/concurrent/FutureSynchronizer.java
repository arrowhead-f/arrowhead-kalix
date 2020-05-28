package se.arkalix.util.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.util.Result;
import se.arkalix.util.function.ThrowingSupplier;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Forces submitted {@link Future futures} to execute one after the other.
 * <p>
 * Synchronization using instances of this class is only relevant when multiple
 * threads may cause asynchronous actions to be triggered in parallel that must
 * be executed in order.
 */
public class FutureSynchronizer {
    private static final Logger logger = LoggerFactory.getLogger(FutureSynchronizer.class);

    private final Queue<Task<?>> queue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isExecuting = new AtomicBoolean(false);

    /**
     * Submits new task for synchronous evaluation.
     *
     * @param supplier Function supplying {@link Future} to be executed
     *                 synchronously.
     * @param <V>      Value type of supplied {@link Future}.
     * @return New {@link Future} that completes with result of {@link Future}
     * supplied via given {@code supplier} function.
     */
    public <V> Future<V> submit(final ThrowingSupplier<Future<V>> supplier) {
        final var task = new Task<>(supplier);
        queue.add(task);
        if (!isExecuting.getAndSet(true)) {
            return execute()
                .always(result -> isExecuting.set(false))
                .flatMap(ignored -> task);
        }
        return task;
    }

    private Future<?> execute() {
        final var task0 = queue.poll();
        if (task0 == null) {
            return Future.done();
        }
        return task0.run()
            .always(ignored -> execute());
    }

    private static class Task<V> implements Future<V> {
        private final ThrowingSupplier<Future<V>> supplier;

        private final AtomicReference<Consumer<Result<V>>> consumer = new AtomicReference<>(null);
        private final AtomicReference<Future<V>> cancellableFuture = new AtomicReference<>(null);
        private final AtomicReference<Result<V>> result = new AtomicReference<>(null);

        private Task(final ThrowingSupplier<Future<V>> supplier) {
            this.supplier = Objects.requireNonNull(supplier, "Expected supplier");
        }

        public Future<?> run() {
            try {
                final var future = supplier.get()
                    .always(result -> {
                        final var consumer = this.consumer.getAndSet(null);
                        if (consumer != null) {
                            try {
                                consumer.accept(result);
                            }
                            catch (final Throwable throwable0) {
                                if (result.isFailure()) {
                                    throwable0.addSuppressed(result.fault());
                                }
                                try {
                                    consumer.accept(Result.failure(throwable0));
                                }
                                catch (final Throwable throwable1) {
                                    throwable1.addSuppressed(throwable0);
                                    logger.error("Failed to execute future synchronization task", throwable1);
                                }
                            }
                        }
                        else {
                            this.result.set(result);
                        }
                    })
                    .mapCatch(Throwable.class, ignored -> null);
                cancellableFuture.set(future);
                return future;
            }
            catch (final Throwable throwable) {
                final var consumer = this.consumer.getAndSet(null);
                if (consumer != null) {
                    consumer.accept(Result.failure(throwable));
                }
                return Future.done();
            }
        }

        @Override
        public void onResult(final Consumer<Result<V>> consumer) {
            Objects.requireNonNull(consumer, "Expected consumer");

            final var result = this.result.getAndSet(null);
            if (result != null) {
                consumer.accept(result);
            }
            else {
                this.consumer.set(consumer);
            }
        }

        @Override
        public void cancel(final boolean mayInterruptIfRunning) {
            final var future = cancellableFuture.getAndSet(null);
            if (future != null) {
                future.cancel(mayInterruptIfRunning);
            }
            result.set(null);
            consumer.set(null);
        }
    }
}
