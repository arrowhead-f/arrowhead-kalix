package se.arkalix.internal.util.concurrent;

import se.arkalix.util.Result;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Internal
public class FutureCompletion<V> implements Future<V> {
    private AtomicReference<Consumer<Boolean>> cancelFunction = new AtomicReference<>(null);
    private AtomicReference<Consumer<Result<V>>> consumer = new AtomicReference<>(null);

    public void complete(final Result<V> result) {
        final var consumer = this.consumer.getAndSet(null);
        if (consumer != null) {
            consumer.accept(result);
        }
    }

    public void setCancelFunction(final Consumer<Boolean> cancelFunction) {
        this.cancelFunction.set(cancelFunction);
    }

    @Override
    public void onResult(final Consumer<Result<V>> consumer) {
        this.consumer.set(consumer);
    }

    @Override
    public void cancel(final boolean mayInterruptIfRunning) {
        final var cancelFunction = this.cancelFunction.getAndSet(null);
        if (cancelFunction != null) {
            cancelFunction.accept(mayInterruptIfRunning);
        }
        consumer.set(null); // Best-effort attempt to prevent completion.
    }
}
