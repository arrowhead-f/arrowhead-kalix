package se.arkalix.concurrent;

import se.arkalix.util.Result;

import java.util.Objects;
import java.util.function.Consumer;

public class UnsynchronizedPromise<T> implements Promise<T> {
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
            case STATE_INITIAL -> {
                state = STATE_HAS_CONSUMER;
                this.consumer = consumer;
            }
            case STATE_HAS_RESULT -> {
                state = STATE_COMPLETED;

                final var result = this.result;
                this.result = null;

                consumer.accept(result);
            }
            default -> throw new IllegalStateException();
        }
    };

    @Override
    public Future<T> future() {
        return future;
    }

    @Override
    public boolean isCompleted() {
        return state == STATE_COMPLETED;
    }

    @Override
    public boolean tryComplete(final Result<T> result) {
        Objects.requireNonNull(result);

        return switch (state) {
            case STATE_INITIAL -> {
                state = STATE_HAS_RESULT;
                this.result = result;

                yield true;
            }
            case STATE_HAS_CONSUMER -> {
                state = STATE_COMPLETED;

                final var consumer = this.consumer;
                this.consumer = null;

                consumer.accept(result);

                yield true;
            }
            default -> false;
        };
    }
}
