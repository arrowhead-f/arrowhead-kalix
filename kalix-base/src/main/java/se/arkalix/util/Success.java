package se.arkalix.util;

import se.arkalix.util._internal.Throwables;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Success<T> implements Result<T> {
    private static final Success<Void> EMPTY = new Success<>() {
        @Override
        public Optional<Void> toOptional() {
            return Optional.empty();
        }
    };

    private final T value;

    private Success() {
        value = null;
    }

    public static Success<Void> empty() {
        return EMPTY;
    }

    private Success(final T value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public static <T> Success<T> of(final T value) {
        return new Success<>(value);
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
    public void ifSuccessOrElse(final Consumer<? super T> consumer, final Consumer<Throwable> failureAction) {
        Objects.requireNonNull(consumer);
        Objects.requireNonNull(failureAction);

        try {
            consumer.accept(value);
        }
        catch (final Throwable throwable) {
            Throwables.throwSilentlyIfFatal(throwable);
            failureAction.accept(throwable);
        }
    }

    @Override
    public void ifFailure(final Consumer<Throwable> consumer) {
        Objects.requireNonNull(consumer);
    }

    @Override
    public T get() {
        return value;
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
    public <R> Result<R> map(final Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper);

        try {
            return new Success<>(mapper.apply(value));
        }
        catch (final Throwable throwable) {
            return Failure.of(throwable);
        }
    }

    @Override
    public <R> Result<R> flatMap(final Function<? super T, ? extends Result<? extends R>> mapper) {
        final var result = Objects.requireNonNull(mapper)
            .apply(value);

        @SuppressWarnings("unchecked") final var result0 = (Result<R>) result;

        return result0;
    }

    @Override
    public Result<T> filter(final Predicate<? super T> predicate) {
        final var isMatch = Objects.requireNonNull(predicate)
            .test(value);

        return isMatch
            ? this
            : Failure.of(new NoSuchElementException());
    }

    @Override
    public Result<T> recover(final Function<Throwable, ? extends T> mapper) {
        return this;
    }

    @Override
    public Result<T> recoverWith(final Function<Throwable, ? extends Result<? extends T>> mapper) {
        return this;
    }

    @Override
    public Optional<T> toOptional() {
        assert value != null;

        return Optional.of(value);
    }
}
