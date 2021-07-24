package se.arkalix.concurrent;

import se.arkalix.util.Result;

import java.util.Objects;
import java.util.function.Consumer;

public class ProcessorForSameThread<T> implements Processor<T> {
    private static final int STATE_INITIAL = 0;
    private static final int STATE_HAS_CONSUMER = 1;
    private static final int STATE_COMPLETED = 2;

    private int state = STATE_INITIAL;
    private Consumer<T> consumer = null;
    private Consumer<? super Result<?>> completionConsumer = null;

    private final Flow<T> flow = (consumer, completionConsumer) -> {
        Objects.requireNonNull(consumer);
        Objects.requireNonNull(completionConsumer);

        if (state != STATE_INITIAL) {
            throw new IllegalStateException();
        }

        state = STATE_HAS_CONSUMER;

        this.consumer = consumer;
        this.completionConsumer = completionConsumer;
    };

    @Override
    public Flow<T> flow() {
        return flow;
    }

    @Override
    public void push(final T item) {
        if (state != STATE_HAS_CONSUMER) {
            throw new IllegalStateException();
        }

        consumer.accept(item);
    }

    @Override
    public boolean isCompleted() {
        return state == STATE_COMPLETED;
    }

    @Override
    public boolean tryComplete(final Result<?> result) {
        Objects.requireNonNull(result);

        if (state == STATE_HAS_CONSUMER) {
            state = STATE_COMPLETED;

            final var completionConsumer = this.completionConsumer;

            this.consumer = null;
            this.completionConsumer = null;

            completionConsumer.accept(result);

            return true;
        }

        return false;
    }
}
