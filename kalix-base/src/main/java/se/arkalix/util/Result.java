package se.arkalix.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Result<T> {
    // TODO: (Java 17) sealed interface Result<T> permits Failure, Success

    boolean isSuccess();

    boolean isFailure();

    void ifSuccess(Consumer<? super T> consumer);

    void ifSuccessOrElse(Consumer<? super T> consumer, Consumer<Throwable> failureConsumer);

    void ifFailure(Consumer<Throwable> consumer);

    Result<T> or(Supplier<? extends Result<? extends T>> supplier);

    T orElse(T other);

    T orElseGet(Supplier<? extends T> supplier);

    T orElseThrow();

    T orElseThrow(Function<Throwable, Throwable> mapper);

    <R> Result<R> map(Function<? super T, ? extends R> mapper);

    <R> Result<R> flatMap(Function<? super T, ? extends Result<? extends R>> mapper);

    Result<T> filter(Predicate<? super T> predicate);

    Result<T> filter(Predicate<? super T> predicate, Supplier<Throwable> failureSupplier);

    Result<T> recover(Function<Throwable, ? extends T> mapper);

    Result<T> flatRecover(Function<Throwable, ? extends Result<? extends T>> mapper);
}
