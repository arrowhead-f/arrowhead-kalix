package se.arkalix.util;

import se.arkalix.util._internal.Throwables;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Failure<T> implements Result<T> {
    private final Throwable fault;

    private Failure(final Throwable fault) {
        Objects.requireNonNull(fault);
        Throwables.throwSilentlyIfFatal(fault);

        this.fault = fault;
    }

    public static <T> Failure<T> of(final Throwable fault) {
        return new Failure<>(fault);
    }

    @SuppressWarnings("unchecked")
    private static <X extends Throwable> void throwSilently(Throwable throwable) throws X {
        throw (X) throwable;
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
    public void ifSuccessOrElse(final Consumer<? super T> consumer, final Consumer<Throwable> failureAction) {
        Objects.requireNonNull(consumer);
        Objects.requireNonNull(failureAction)
            .accept(fault);
    }

    @Override
    public void ifFailure(final Consumer<Throwable> consumer) {
        Objects.requireNonNull(consumer)
            .accept(fault);
    }

    @Override
    public T get() {
        throwSilently(fault);
        return null;
    }

    @Override
    public Result<T> or(final Supplier<? extends Result<? extends T>> supplier) {
        Objects.requireNonNull(supplier);

        try {
            @SuppressWarnings("unchecked") final var result = (Result<T>) supplier.get();

            return Objects.requireNonNull(result);
        }
        catch (final Throwable throwable0) {
            throwable0.addSuppressed(fault);
            return new Failure<>(throwable0);
        }
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
    public Result<T> recover(final Function<Throwable, ? extends T> mapper) {
        try {
            final var value = Objects.requireNonNull(mapper)
                .apply(fault);
            return Success.of(value);
        }
        catch (final Throwable throwable0) {
            throwable0.addSuppressed(fault);
            return new Failure<>(throwable0);
        }
    }

    @Override
    public Result<T> recoverWith(final Function<Throwable, ? extends Result<? extends T>> mapper) {
        try {
            final var result = Objects.requireNonNull(mapper)
                .apply(fault);

            @SuppressWarnings("unchecked") final var result0 = (Result<T>) result;

            return result0;
        }
        catch (final Throwable throwable0) {
            throwable0.addSuppressed(fault);
            return new Failure<>(throwable0);
        }
    }

    @Override
    public Optional<T> toOptional() {
        return Optional.empty();
    }
}
