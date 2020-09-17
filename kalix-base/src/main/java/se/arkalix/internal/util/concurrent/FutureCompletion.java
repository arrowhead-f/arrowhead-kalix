package se.arkalix.internal.util.concurrent;

import se.arkalix.util.Result;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;

import java.util.function.Consumer;

@Internal
public class FutureCompletion<V> implements Future<V> {
    private Consumer<Boolean> cancelFunction = null;
    private Consumer<Result<V>> consumer = null;
    private Result<V> result = null;

    public synchronized void complete(final Result<V> result) {
        if (consumer != null) {
            consumer.accept(result);
            consumer = null;
        }
        else {
            this.result = result;
        }
    }

    public synchronized void setCancelFunction(final Consumer<Boolean> cancelFunction) {
        this.cancelFunction = cancelFunction;
    }

    @Override
    public synchronized void onResult(final Consumer<Result<V>> consumer) {
        if (result != null) {
            consumer.accept(result);
            result = null;
        }
        else {
            this.consumer = consumer;
        }
    }

    @Override
    public synchronized void cancel(final boolean mayInterruptIfRunning) {
        if (cancelFunction != null) {
            cancelFunction.accept(mayInterruptIfRunning);
            cancelFunction = null;
        }
        consumer = null;
        result = null;
    }
}
