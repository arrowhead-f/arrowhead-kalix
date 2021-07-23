package se.arkalix.util;

import se.arkalix.util._internal.Throwables;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public record Failure<T>(Throwable exception) implements Result<T> {
    public Failure {
        Objects.requireNonNull(exception);
        Throwables.throwSilentlyIfFatal(exception);
    }

    public static <T> Failure<T> of(final Throwable exception) {
        return new Failure<>(exception);
    }

    public <U> Failure<U> retype() {
        @SuppressWarnings("unchecked") final var self = (Failure<U>) this;
        return self;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public boolean isFailure() {
        return true;
    }

    @Override
    public void ifSuccess(final Consumer<? super T> consumer) {
        Objects.requireNonNull(consumer);
    }

    @Override
    public void ifSuccessOrElse(final Consumer<? super T> consumer, final Consumer<Throwable> failureConsumer) {
        Objects.requireNonNull(consumer);
        Objects.requireNonNull(failureConsumer)
            .accept(exception);
    }

    @Override
    public void ifFailure(final Consumer<Throwable> consumer) {
        Objects.requireNonNull(consumer)
            .accept(exception);
    }

    @Override
    public Result<T> or(final Supplier<? extends Result<? extends T>> supplier) {
        Objects.requireNonNull(supplier);

        final Result<? extends T> result0;
        try {
            result0 = supplier.get();
        }
        catch (final Throwable throwable0) {
            throwable0.addSuppressed(exception);
            return Failure.of(throwable0);
        }

        Objects.requireNonNull(result0);

        @SuppressWarnings("unchecked")
        final var result1 = (Result<T>) result0;

        return result1;
    }

    @Override
    public T orElse(final T other) {
        return other;
    }

    @Override
    public T orElseGet(final Supplier<? extends T> supplier) {
        return Objects.requireNonNull(supplier)
            .get();
    }

    @Override
    public T orElseThrow() {
        Throwables.throwSilently(exception);
        return null;
    }

    @Override
    public T orElseThrow(final Function<Throwable, Throwable> mapper) {
        Throwables.throwSilently(mapper.apply(exception));
        return null;
    }

    @Override
    public <R> Result<R> map(final Function<? super T, ? extends R> mapper) {
        @SuppressWarnings("unchecked") final var result = (Result<R>) this;

        return result;
    }

    @Override
    public <R> Result<R> flatMap(final Function<? super T, ? extends Result<? extends R>> mapper) {
        @SuppressWarnings("unchecked") final var result = (Result<R>) this;

        return result;
    }

    @Override
    public Result<T> filter(final Predicate<? super T> predicate) {
        return this;
    }

    @Override
    public Result<T> filter(final Predicate<? super T> predicate, final Supplier<Throwable> failureSupplier) {
        return this;
    }

    @Override
    public Result<T> recover(final Function<Throwable, ? extends T> mapper) {
        Objects.requireNonNull(mapper);
        try {
            return Success.of(mapper.apply(exception));
        }
        catch (final Throwable throwable0) {
            throwable0.addSuppressed(exception);
            Throwables.throwSilentlyIfFatal(throwable0);
            return Failure.of(throwable0);
        }
    }

    @Override
    public Result<T> flatRecover(final Function<Throwable, ? extends Result<? extends T>> mapper) {
        Objects.requireNonNull(mapper);

        final Result<? extends T> result0;
        try {
            result0 = mapper.apply(exception);
        }
        catch (final Throwable throwable0) {
            throwable0.addSuppressed(exception);
            Throwables.throwSilentlyIfFatal(throwable0);
            return Failure.of(throwable0);
        }

        Objects.requireNonNull(result0);

        @SuppressWarnings("unchecked") final var result1 = (Result<T>) result0;

        return result1;
    }
}
