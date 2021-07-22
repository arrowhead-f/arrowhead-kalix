package se.arkalix.concurrent;

import se.arkalix.util.Failure;
import se.arkalix.util.Result;
import se.arkalix.util.Success;

import java.util.Objects;
import java.util.function.Consumer;

public class Promise<T> {
    private static final int STATE_INITIAL = 0;
    private static final int STATE_HAS_CONSUMER = 1;
    private static final int STATE_HAS_RESULT = 2;
    private static final int STATE_COMPLETED = 3;
    
    private int state = STATE_INITIAL;
    private Consumer<? super Result<T>> consumer = null;
    private Result<T> result = null;

    private final Future<T> future = consumer -> {
        Objects.requireNonNull(consumer);

        switch (state) {
        case STATE_INITIAL:
            state = STATE_HAS_CONSUMER;
            this.consumer = consumer;
            break;

        case STATE_HAS_RESULT:
            state = STATE_COMPLETED;
            try {
                consumer.accept(result);
            }
            finally {
                this.consumer = null;
                result = null;
            }
            break;

        default:
            throw new IllegalStateException();
        }
    };

    public Future<T> future() {
        return future;
    }

    public boolean isCompleted() {
        return state == STATE_COMPLETED;
    }

    public void complete(final Result<T> result) {
        if (!tryComplete(result)) {
            throw new IllegalStateException();
        }
    }

    public void completeWith(final Future<T> future) {
        Objects.requireNonNull(future)
            .onCompletion(this::complete);
    }

    public boolean tryComplete(final Result<T> result) {
        Objects.requireNonNull(result);

        switch (state) {
        case STATE_INITIAL:
            state = STATE_HAS_RESULT;
            this.result = result;
            return true;

        case STATE_HAS_CONSUMER:
            state = STATE_COMPLETED;
            try {
                consumer.accept(result);
            }
            finally {
                consumer = null;
                this.result = null;
            }
            return true;

        default:
            return false;
        }
    }

    public void fulfill(final T value) {
        complete(Success.of(value));
    }

    public boolean tryFulfill(final T value) {
        return tryComplete(Success.of(value));
    }

    public void fail(final Throwable exception) {
        complete(Failure.of(exception));
    }

    public boolean tryFail(final Throwable exception) {
        return tryComplete(Failure.of(exception));
    }
}
