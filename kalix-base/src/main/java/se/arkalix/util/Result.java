package se.arkalix.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class Result<T> {
    Result() {}

    public abstract boolean isSuccess();

    public abstract boolean isFailure();

    public abstract void ifSuccess(Consumer<? super T> consumer);

    public abstract void ifSuccessOrElse(Consumer<? super T> consumer, Consumer<Throwable> failureConsumer);

    public abstract void ifFailure(Consumer<Throwable> consumer);

    public abstract Result<T> or(Supplier<? extends Result<? extends T>> supplier);

    public abstract T orElse(T other);

    public abstract T orElseGet(Supplier<? extends T> supplier);

    public abstract T orElseThrow();

    public abstract T orElseThrow(Function<Throwable, Throwable> mapper);

    public abstract <R> Result<R> map(Function<? super T, ? extends R> mapper);

    public abstract <R> Result<R> flatMap(Function<? super T, ? extends Result<? extends R>> mapper);

    public abstract Result<T> filter(Predicate<? super T> predicate);

    public abstract Result<T> filter(Predicate<? super T> predicate, Supplier<Throwable> failureSupplier);

    public abstract Result<T> recover(Function<Throwable, ? extends T> mapper);

    public abstract Result<T> flatRecover(Function<Throwable, ? extends Result<? extends T>> mapper);
}
