package se.arkalix.internal.util.concurrent;

import se.arkalix.util.Result;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Internal
class CompletableFuture<V> implements Future<V> {
    private AtomicReference<Consumer<Boolean>> cancelTarget = new AtomicReference<>(null);
    private AtomicReference<Consumer<Result<V>>> consumer = new AtomicReference<>(null);

    public void complete(final Result<V> result) {
        final var consumer = this.consumer.get();
        if (consumer != null) {
            consumer.accept(result);
        }
    }

    public void setCancelFunction(final Consumer<Boolean> cancelFunction) {
        this.cancelTarget.set(cancelFunction);
    }

    @Override
    public void onResult(final Consumer<Result<V>> consumer) {
        this.consumer.set(consumer);
    }

    @Override
    public void cancel(final boolean mayInterruptIfRunning) {
        final var cancelTarget = this.cancelTarget.getAndSet(null);
        if (cancelTarget != null) {
            cancelTarget.accept(mayInterruptIfRunning);
        }
        consumer.set(null);
    }
}
