package se.arkalix.util.concurrent._internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.util.Result;
import se.arkalix.util.concurrent.Future;

import java.util.Objects;
import java.util.function.Consumer;

public class FutureConsumption<V> implements Future<V> {
    private static final Logger logger = LoggerFactory.getLogger(FutureConsumption.class);

    private final Future<?> parent;

    private Consumer<Result<V>> consumer;

    public FutureConsumption(final Future<?> parent) {
        this.parent = Objects.requireNonNull(parent, "parent");
    }

    public void consume(final Result<V> result) {
        if (consumer == null && logger.isTraceEnabled()) {
            logger.trace("Future result ignored: {}", result);
        }
        else {
            consumer.accept(result);
        }
    }

    public void consume(final Future<V> future) {
        if (consumer == null && logger.isTraceEnabled()) {
            logger.trace("Future ignored: {}", future);
        }
        else {
            future.onResult(consumer);
        }
    }

    @Override
    public void onResult(final Consumer<Result<V>> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void cancel(final boolean mayInterruptIfRunning) {
        parent.cancel(mayInterruptIfRunning);
    }
}
