package se.arkalix.util.concurrent;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The result of an operation that can either succeed or fail.
 * <p>
 * A {@code Result} may either be a <i>success</i>, in which case a
 * <i>value</i> is available, or a <i>failure</i>, which makes an
 * <i>fault</i> available. The {@link #isSuccess()} method is used to
 * determine which of the two situations is the case. The {@link #value()}
 * and {@link #fault()} methods are used to collect the value or fault,
 * respectively.
 * <p>
 * This type is especially appropriate to use when throwing exceptions do not
 * cause them to end up at the initiator of the execution in question. This is
 * often the case when dealing with thread pools, as the threads in such pools
 * can do nothing to meaningfully handle exceptions thrown while they are
 * executing.
 *
 * @param <V> Type of value provided by {@code Result} if successful.
 */
@SuppressWarnings("unused")
public class Result<V> {
    private static final Result<?> DONE = new Result<>(true, null, null);

    private final boolean isSuccess;
    private final V value;
    private final Throwable fault;

    private Result(final boolean isSuccess, final V value, final Throwable fault) {
        this.isSuccess = isSuccess;
        this.value = value;
        this.fault = fault;
    }

    /**
     * Creates new successful {@code Result}.
     *
     * @param value Value.
     * @param <V>   Type of value.
     * @return New {@code Result}.
     */
    public static <V> Result<V> success(final V value) {
        return new Result<>(true, value, null);
    }

    /**
     * Creates new failure {@code Result}.
     *
     * @param throwable Reason for failure.
     * @param <V>       Type of value that would have been provided by the
     *                  created {@code Result}, if it were successful.
     * @return New {@code Result}.
     */
    public static <V> Result<V> failure(final Throwable throwable) {
        return new Result<>(false, null, Objects.requireNonNull(throwable));
    }

    /**
     * Creates new successful {@code Result} with {@code null} value.
     *
     * @param <V> Arbitrary type.
     * @return New {@code Result}.
     */
    @SuppressWarnings("unchecked")
    public static <V> Result<V> done() {
        return (Result<V>) DONE;
    }

    /**
     * @return {@code true} if this {@code Result} contains a value.
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * @return {@code true} if this {@code Result} contains a fault.
     */
    public boolean isFailure() {
        return !isSuccess;
    }

    /**
     * @return A {@code Throwable} if this {@code Result} is a failure.
     * {@code null} otherwise.
     */
    public Throwable fault() {
        return fault;
    }

    /**
     * @return Some value if this {@code Result} is a success. {@code null}
     * otherwise.
     */
    public V value() {
        return value;
    }

    /**
     * Either returns {@code Result} value or throws its fault, depending
     * on whether it is successful or not.
     *
     * @return Result value, if this {@code Result} is successful.
     */
    public V valueOrThrow() {
        if (isSuccess()) {
            return value();
        }
        throwFault();
        return null;
    }

    @SuppressWarnings("unchecked")
    private <E extends Throwable> void throwFault() throws E {
        throw (E) fault();
    }

    /**
     * Calls given {@code consumer} only if this result is successful.
     *
     * @param consumer Consumer function to call, if successful.
     */
    public void ifSuccess(final Consumer<? super V> consumer) {
        if (isSuccess()) {
            consumer.accept(value());
        }
    }

    /**
     * Calls given {@code consumer} only if this result is a failure.
     *
     * @param consumer Consumer function to call, if not successful.
     */
    public void ifFailure(final Consumer<Throwable> consumer) {
        if (isFailure()) {
            consumer.accept(fault());
        }
    }

    /**
     * If this result is successful, applies given {@code mapper} to its value.
     * Otherwise, a new {@code Result} with the fault contained in this one is
     * returned.
     *
     * @param <U>    Type of return value of {@code mapper}.
     * @param mapper Function to apply to result value, if this result is
     *               successful.
     * @return New result containing either output of mapping or an fault
     * passed on from this result.
     */
    public <U> Result<U> map(final Function<? super V, U> mapper) {
        if (isSuccess()) {
            return success(mapper.apply(value()));
        }
        return failure(fault());
    }

    /**
     * If this result is successful, applies given {@code mapper} to its value,
     * and then returns the {@code Result} returned by the {@code mapper}. If
     * this result is not successful, a new {@code Result} with the fault it
     * contains is returned.
     *
     * @param <U>    Type of value of {@code Result} returned by
     *               {@code mapper}.
     * @param mapper Function to apply to result value, if this result is
     *               successful.
     * @return New result consisting either of result of {@code mapper} or an
     * fault passed on from this result.
     */
    public <U> Result<U> flatMap(final Function<? super V, ? extends Result<U>> mapper) {
        if (isSuccess()) {
            return mapper.apply(value());
        }
        return failure(fault());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final Result<?> result = (Result<?>) o;
        return isSuccess == result.isSuccess && (isSuccess
            ? Objects.equals(value, result.value)
            : Objects.equals(fault, result.fault));
    }

    @Override
    public int hashCode() {
        return Objects.hash(isSuccess, isSuccess ? value : fault);
    }

    @Override
    public String toString() {
        return "Result{" + (isSuccess
            ? "value=" + value
            : "fault=" + fault) +
            '}';
    }
}
