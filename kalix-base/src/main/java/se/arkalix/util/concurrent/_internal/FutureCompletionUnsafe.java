package se.arkalix.util.concurrent._internal;

import se.arkalix.util.Result;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;

import java.util.function.Consumer;

@Internal
public class FutureCompletionUnsafe<V> implements Future<V> {
    private Consumer<Boolean> cancelCallback = null;
    private Consumer<Result<V>> consumer = null;
    private Result<V> result = null;

    private boolean isCancelled = false;
    private boolean isCompleted = false;

    public boolean isCancelled() {
        return isCancelled;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void complete(final Result<V> result) {
        if (isCompleted) {
            throw new IllegalStateException("Already completed");
        }
        if (consumer != null) {
            consumer.accept(result);
            consumer = null;
        }
        else if (!isCancelled) {
            this.result = result;
        }
        isCompleted = true;
    }

    public void setCancelCallback(final Consumer<Boolean> cancelCallback) {
        if (!isCancelled) {
            this.cancelCallback = cancelCallback;
        }
    }

    @Override
    public void onResult(final Consumer<Result<V>> consumer) {
        if (result != null) {
            consumer.accept(result);
            result = null;
            isCompleted = true;
        }
        else if (!isCancelled) {
            this.consumer = consumer;
        }
    }

    @Override
    public void cancel(final boolean mayInterruptIfRunning) {
        if (cancelCallback != null) {
            cancelCallback.accept(mayInterruptIfRunning);
            cancelCallback = null;
        }
        consumer = null;
        result = null;
        isCancelled = true;
    }
}