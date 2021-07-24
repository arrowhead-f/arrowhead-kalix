package se.arkalix.concurrent;

import se.arkalix.util.Failure;
import se.arkalix.util.Result;
import se.arkalix.util._internal.Throwables;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@FunctionalInterface
public interface Flow<T> {
    void onNextOrCompletion(Consumer<T> consumer, Consumer<Result<?>> completionConsumer);

    default void onNextOrFailure(final Consumer<T> consumer, final Consumer<Throwable> failureConsumer) {
        onNextOrCompletion(consumer, result -> result.ifFailure(failureConsumer));
    }

    default <U> Flow<U> map(final Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);

        return (consumer, completionConsumer) -> onNextOrCompletion(value0 -> {
            try {
                final var value1 = mapper.apply(value0);
                consumer.accept(value1);
            }
            catch (final Throwable throwable) {
                Throwables.throwSilentlyIfFatal(throwable);
                completionConsumer.accept(Failure.of(throwable));
            }
        }, completionConsumer);
    }

    default <U> Flow<U> flatMap(final Function<? super T, ? extends Flow<? extends U>> mapper) {
        Objects.requireNonNull(mapper);

        return (consumer, completionConsumer) -> onNextOrCompletion(value -> {
            Throwable exception;

            fail:
            try {
                final var flow0 = mapper.apply(value);

                if (flow0 == null) {
                    exception = new NullPointerException();
                    break fail;
                }

                @SuppressWarnings("unchecked") final var flow1 = (Flow<U>) flow0;

                flow1.onNextOrCompletion(consumer, completionConsumer);
                return;
            }
            catch (final Throwable throwable) {
                Throwables.throwSilentlyIfFatal(throwable);
                exception = throwable;
            }

            completionConsumer.accept(Failure.of(exception));

        }, completionConsumer);
    }

    default Flow<T> filter(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);

        return (consumer, completionConsumer) -> onNextOrCompletion(value -> {
            try {
                if (predicate.test(value)) {
                    consumer.accept(value);
                }
            }
            catch (final Throwable throwable) {
                Throwables.throwSilentlyIfFatal(throwable);
                completionConsumer.accept(Failure.of(throwable));
            }
        }, completionConsumer);
    }
}
