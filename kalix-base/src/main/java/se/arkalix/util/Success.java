package se.arkalix.util;

import se.arkalix.util._internal.Throwables;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Success<T> extends Result<T> {
    private static final Success<Void> EMPTY = new Success<>(null);

    private final T value;

    private Success(final T value) {
        this.value = value;
    }

    public static Success<Void> empty() {
        return EMPTY;
    }

    public static <T> Success<T> of(final T value) {
        return new Success<>(value);
    }

    public T value() {
        return value;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public boolean isFailure() {
        return false;
    }

    @Override
    public void ifSuccess(final Consumer<? super T> consumer) {
        Objects.requireNonNull(consumer)
            .accept(value);
    }

    @Override
    public void ifSuccessOrElse(final Consumer<? super T> consumer, final Consumer<Throwable> failureConsumer) {
        Objects.requireNonNull(consumer);
        Objects.requireNonNull(failureConsumer);

        consumer.accept(value);
    }

    @Override
    public void ifFailure(final Consumer<Throwable> consumer) {
        Objects.requireNonNull(consumer);
    }

    @Override
    public Result<T> or(final Supplier<? extends Result<? extends T>> supplier) {
        Objects.requireNonNull(supplier);

        return this;
    }

    @Override
    public T orElse(final T other) {
        return value;
    }

    @Override
    public T orElseGet(final Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier);

        return value;
    }

    @Override
    public T orElseThrow() {
        return value;
    }

    @Override
    public T orElseThrow(final Function<Throwable, Throwable> mapper) {
        return value;
    }

    @Override
    public <R> Result<R> map(final Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper);

        try {
            return new Success<>(mapper.apply(value));
        }
        catch (final Throwable throwable) {
            Throwables.throwSilentlyIfFatal(throwable);
            return Failure.of(throwable);
        }
    }

    @Override
    public <R> Result<R> flatMap(final Function<? super T, ? extends Result<? extends R>> mapper) {
        Objects.requireNonNull(mapper);

        final Result<? extends R> result0;
        try {
            result0 = mapper.apply(value);
        }
        catch (final Throwable throwable) {
            Throwables.throwSilentlyIfFatal(throwable);
            return Failure.of(throwable);
        }

        Objects.requireNonNull(result0);

        @SuppressWarnings("unchecked") final var result1 = (Result<R>) result0;

        return result1;
    }

    @Override
    public Result<T> filter(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);

        final boolean isMatch;
        try {
            isMatch = predicate.test(value);
        }
        catch (final Throwable throwable) {
            Throwables.throwSilentlyIfFatal(throwable);
            return Failure.of(throwable);
        }

        return isMatch ? this : Failure.of(new NoSuchElementException());
    }

    @Override
    public Result<T> filter(final Predicate<? super T> predicate, final Supplier<Throwable> failureSupplier) {
        Objects.requireNonNull(predicate);

        final boolean isMatch;
        try {
            isMatch = predicate.test(value);
        }
        catch (final Throwable throwable) {
            Throwables.throwSilentlyIfFatal(throwable);
            return Failure.of(throwable);
        }

        return isMatch ? this : Failure.of(failureSupplier.get());
    }

    @Override
    public Result<T> recover(final Function<Throwable, ? extends T> mapper) {
        return this;
    }

    @Override
    public Result<T> flatRecover(final Function<Throwable, ? extends Result<? extends T>> mapper) {
        return this;
    }
}
