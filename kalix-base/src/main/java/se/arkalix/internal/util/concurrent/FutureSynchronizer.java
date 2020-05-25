package se.arkalix.internal.util.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.util.Result;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.function.ThrowingSupplier;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Internal
public class FutureSynchronizer<V> {
    private static final Logger logger = LoggerFactory.getLogger(FutureSynchronizer.class);

    private final Queue<Task<V>> queue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isExecuting = new AtomicBoolean(false);

    public Future<V> execute(final ThrowingSupplier<Future<V>> supplier) {
        final var task = new Task<>(supplier);
        queue.add(task);
        if (!isExecuting.getAndSet(true)) {
            return next(queue)
                .always(result -> isExecuting.set(false))
                .flatMap(ignored -> task);
        }
        return task;
    }

    private static <V> Future<?> next(final Queue<Task<V>> queue) {
        final var task0 = queue.poll();
        if (task0 == null) {
            return Future.done();
        }
        return task0.run()
            .always(ignored -> next(queue));
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
