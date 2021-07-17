package se.arkalix.util.concurrent;

import java.util.function.Consumer;

public class Promise<V> {
    private static final int STATE_INITIAL = 0;
    private static final int STATE_HAS_CONSUMER = 1;
    private static final int STATE_HAS_RESULT = 2;
    private static final int STATE_CANCELLED = 3;
    private static final int STATE_COMPLETED = 4;
    
    private int state = STATE_INITIAL;
    private Consumer<Result<V>> consumer = null;
    private Result<V> result = null;

    private final Future<V> future = new Future<>() {
        @Override
        public void await(final Consumer<Result<V>> consumer) {
            if (consumer == null) {
                throw new NullPointerException("consumer");
            }
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
            return true;
        }
    };

    public Future<V> future() {
        return future;
    }
    
    public void fulfill() {
        complete(Result.done());
    }

    public void fulfill(final V value) {
        complete(Result.success(value));
    }

    public void forfeit(final Throwable fault) {
        complete(Result.failure(fault));
    }

    public void complete(final Result<V> result) {
        if (result == null) {
            throw new NullPointerException("result");
        }
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
