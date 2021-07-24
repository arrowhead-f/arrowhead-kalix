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
    void onNextOnCompletion(Consumer<T> consumer, Consumer<? super Result<?>> completionConsumer);

    default void onNextOnFailure(final Consumer<T> consumer, final Consumer<Throwable> failureConsumer) {
        Objects.requireNonNull(consumer);
        Objects.requireNonNull(failureConsumer);

        onNextOnCompletion(consumer, result -> result.ifFailure(failureConsumer));
    }

    default <U> Flow<U> map(final Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);

        final var processor = new ProcessorForSameThread<U>();

        onNextOnCompletion(item0 -> {
            try {
                final var item1 = mapper.apply(item0);
                processor.push(item1);
            }
            catch (final Throwable throwable) {
                Throwables.throwSilentlyIfFatal(throwable);
                processor.fail(throwable);
            }
        }, processor::complete);

        return processor.flow();
    }

    default <U> Flow<U> flatMap(final Function<? super T, ? extends Flow<? extends U>> mapper) {
        Objects.requireNonNull(mapper);

        final var processor = new ProcessorForSameThread<U>();

        onNextOnCompletion(item -> {
            Throwable exception;

            fail:
            try {
                final var flow0 = mapper.apply(item);

                if (flow0 == null) {
                    exception = new NullPointerException();
                    break fail;
                }

                @SuppressWarnings("unchecked") final var flow1 = (Flow<U>) flow0;

                flow1.onNextOnCompletion(processor::push, processor::complete);

                return;
            }
            catch (final Throwable throwable) {
                Throwables.throwSilentlyIfFatal(throwable);
                exception = throwable;
            }

            processor.fail(exception);

        }, processor::complete);

        return processor.flow();
    }

    default Flow<T> filter(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);

        final var processor = new ProcessorForSameThread<T>();

        onNextOnCompletion(item -> {
            try {
                if (predicate.test(item)) {
                    processor.push(item);
                }
            }
            catch (final Throwable throwable) {
                Throwables.throwSilentlyIfFatal(throwable);
                processor.fail(throwable);
            }
        }, processor::complete);

        return processor.flow();
    }

    default Flow<T> fallback(final Function<Throwable, ? extends Flow<? extends T>> mapper) {
        Objects.requireNonNull(mapper);

        final var processor = new ProcessorForSameThread<T>();

        onNextOnCompletion(processor::push, result -> {
            Objects.requireNonNull(result);

            fail:
            if (result instanceof Failure<?> failure) {
                Flow<? extends T> flow0;

                try {
                    flow0 = mapper.apply(failure.exception());
                }
                catch (final Throwable throwable) {
                    Throwables.throwSilentlyIfFatal(throwable);
                    throwable.addSuppressed(failure.exception());
                    result = Failure.of(throwable);
                    break fail;
                }

                if (flow0 == null) {
                    result = Failure.of(new NullPointerException());
                    break fail;
                }

                @SuppressWarnings("unchecked") final var flow1 = (Flow<T>) flow0;

                flow1.onNextOnCompletion(processor::push, processor::complete);

                return;
            }

            processor.complete(result);
        });

        return processor.flow();
    }
}
