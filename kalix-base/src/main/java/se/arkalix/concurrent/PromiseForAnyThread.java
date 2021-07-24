package se.arkalix.concurrent;

import se.arkalix.util.Result;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class PromiseForAnyThread<T> implements Promise<T> {
    private static final int STATE_INITIAL = 0;
    private static final int STATE_HAS_CONSUMER = 1;
    private static final int STATE_HAS_RESULT = 2;
    private static final int STATE_COMPLETED = 3;

    private final AtomicReference<State<T>> state = new AtomicReference<>(new State<>(STATE_INITIAL, null, null, false));

    private final Future<T> future = consumer -> {
        Objects.requireNonNull(consumer);

        final var newState = state.updateAndGet(state -> switch (state.code) {
            case STATE_INITIAL -> new State<>(STATE_HAS_CONSUMER, consumer, null, false);
            case STATE_HAS_RESULT -> new State<>(STATE_COMPLETED, null, state.result, false);
            default -> new State<>(state.code, state.consumer, state.result, true);
        });
        
        if (newState.isIllegal) {
            throw new IllegalStateException();
        }

        if (newState.code == STATE_COMPLETED) {
            consumer.accept(newState.result);
        }
    };

    @Override
    public Future<T> future() {
        return future;
    }

    @Override
    public boolean isCompleted() {
        return state.get().code == STATE_COMPLETED;
    }

    @Override
    public boolean tryComplete(final Result<T> result) {
        Objects.requireNonNull(result);

        final var newState = state.updateAndGet(state -> switch (state.code) {
            case STATE_INITIAL -> new State<>(STATE_HAS_RESULT, null, result, false);
            case STATE_HAS_CONSUMER -> new State<>(STATE_COMPLETED, state.consumer, null, false);
            default -> new State<>(state.code, state.consumer, state.result, true);
        });

        if (newState.isIllegal) {
            return false;
        }

        if (newState.code == STATE_COMPLETED) {
            newState.consumer.accept(result);
        }
        
        return true;
    }

    private record State<T>(int code, Consumer<? super Result<T>> consumer, Result<T> result, boolean isIllegal) {}
}
