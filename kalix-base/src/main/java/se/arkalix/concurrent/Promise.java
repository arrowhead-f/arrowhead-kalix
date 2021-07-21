package se.arkalix.concurrent;

import se.arkalix.util.Failure;
import se.arkalix.util.Result;
import se.arkalix.util.Success;
import se.arkalix.util.concurrent.FutureAlreadyHasConsumer;
import se.arkalix.util.concurrent.FutureAlreadyHasResult;

import java.util.Objects;
import java.util.function.Consumer;

public class Promise<T> {
    private static final int STATE_INITIAL = 0;
    private static final int STATE_HAS_CONSUMER = 1;
    private static final int STATE_HAS_RESULT = 2;
    private static final int STATE_CANCELLED = 3;
    private static final int STATE_COMPLETED = 4;
    
    private int state = STATE_INITIAL;
    private Consumer<? super Result<? super T>> consumer = null;
    private Result<? super T> result = null;

    private final Future<T> future = new Future<>() {
        @Override
        public void onResult(final Consumer<? super Result<? super T>> consumer) {
            Objects.requireNonNull(consumer);

            switch (state) {
            case STATE_INITIAL:
                state = STATE_HAS_CONSUMER;
                Promise.this.consumer = consumer;
                break;

            case STATE_HAS_CONSUMER:
            case STATE_COMPLETED:
                throw new FutureAlreadyHasConsumer();

            case STATE_HAS_RESULT:
                state = STATE_COMPLETED;
                try {
                    consumer.accept(result);
                }
                finally {
                    Promise.this.consumer = null;
                    result = null;
                }
                break;

            case STATE_CANCELLED:
                break;

            default:
                throw new IllegalStateException("Bad promise state: " + state);
            }
        }

        @Override
        public boolean cancel() {
            switch (state) {
            case STATE_INITIAL:
                break;

            case STATE_HAS_CONSUMER:
                consumer = null;
                break;

            case STATE_HAS_RESULT:
                result = null;
                break;

            case STATE_CANCELLED:
            case STATE_COMPLETED:
                return false;

            default:
                throw new IllegalStateException("Bad promise state: " + state);
            }
            state = STATE_CANCELLED;
            onCancelled();
            return true;
        }
    };

    public Future<T> future() {
        return future;
    }

    public void fulfill(final T value) {
        complete(Success.of(value));
    }

    public void forfeit(final Throwable fault) {
        complete(Failure.of(fault));
    }

    public void complete(final Result<? super T> result) {
        Objects.requireNonNull(result);

        switch (state) {
        case STATE_INITIAL:
            state = STATE_HAS_RESULT;
            this.result = result;
            break;

        case STATE_HAS_CONSUMER:
            state = STATE_COMPLETED;
            try {
                consumer.accept(result);
            }
            finally {
                consumer = null;
                this.result = null;
            }
            break;

        case STATE_HAS_RESULT:
        case STATE_COMPLETED:
            throw new FutureAlreadyHasResult();

        case STATE_CANCELLED:
            break;

        default:
            throw new IllegalStateException("Bad promise state: " + state);
        }
    }

    protected void onCancelled() {
        // Does nothing.
    }

    public boolean isCancelled() {
        return state == STATE_CANCELLED;
    }

    public boolean isCompleted() {
        return state == STATE_COMPLETED;
    }

    public boolean isCancelledOrCompleted() {
        return state > 2;
    }
    
    public boolean isFailure() {
        return isCompleted() && result.isFailure();
    }
    
    public boolean isSuccess() {
        return isCompleted() && result.isSuccess();
    }
}
