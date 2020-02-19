package eu.arrowhead.kalix.util.function;

import java.util.Objects;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> {
    R apply(T t) throws E;

    default <V> ThrowingFunction<V, R, E>compose(ThrowingFunction<? super V, ? extends T, ? extends E> before) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }

    default <V> ThrowingFunction<T, V, E> andThen(ThrowingFunction<? super R, ? extends V, ? extends E> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

    static <T, E extends Throwable> ThrowingFunction<T, T, E> identity() {
        return t -> t;
    }
}
