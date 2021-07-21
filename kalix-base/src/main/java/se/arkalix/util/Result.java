package se.arkalix.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Result<T> {
    boolean isSuccess();

    boolean isFailure();

    void ifSuccess(Consumer<? super T> consumer);

    void ifSuccessOrElse(Consumer<? super T> consumer, Consumer<Throwable> failureAction);

    void ifFailure(Consumer<Throwable> consumer);

    T get();

    Result<T> or(Supplier<? extends Result<? extends T>> supplier);

    T orElse(T other);

    T orElseGet(Supplier<? extends T> supplier);

    <R> Result<R> map(Function<? super T, ? extends R> mapper);

    <R> Result<R> flatMap(Function<? super T, ? extends Result<? extends R>> mapper);

    Result<T> filter(Predicate<? super T> predicate);

    Result<T> recover(Function<Throwable, ? extends T> mapper);

    Result<T> recoverWith(Function<Throwable, ? extends Result<? extends T>> mapper);

    Optional<T> toOptional();
}
