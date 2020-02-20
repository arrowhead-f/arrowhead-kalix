package eu.arrowhead.kalix.concurrent;

/**
 * The result of a {@link Future}.
 * <p>
 * A {@code Result} may either be a <i>success</i>, in which case a
 * <i>value</i> is available, or a <i>failure</i>, which makes an
 * <i>error</i> available. The {@link #isSuccess()} method is used to
 * determine which of the two situations is the case. The {@link #value()}
 * and {@link #error()} methods are used to collect the value or error,
 * respectively.
 *
 * @param <V> Type of value provided by {@code Result} if successful.
 */
public class Result<V> {
    private final boolean isSuccess;
    private final V value;
    private final Throwable error;

    private Result(final boolean isSuccess, final V value, final Throwable error) {
        this.isSuccess = isSuccess;
        this.value = value;
        this.error = error;
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
     * @param error Reason for failure.
     * @param <V>   Type of value that would have been provided by the
     *              created {@code Result}, if it were successful.
     * @return New {@code Result}.
     */
    public static <V> Result<V> failure(final Throwable error) {
        return new Result<>(false, null, error);
    }

    /**
     * @return {@code true} if this {@code Result} contains a value.
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * @return Some exception if this {@code Result} is a failure.
     * {@code null} otherwise.
     */
    public Throwable error() {
        return error;
    }

    /**
     * @return Some value if this {@code Result} is a success. {@code null}
     * otherwise.
     */
    public V value() {
        return value;
    }

    /**
     * Either returns {@code Result} value or throws its error, depending
     * on whether it is successful or not.
     * <p>
     * In the case of being a failure, the error is thrown as-is if it is a
     * subclass of {@link RuntimeException}. If not, it is wrapped in a
     * {@code RuntimeException} before being thrown.
     *
     * @return Result value, if the {@code Result} is successful.
     * @throws RuntimeException If the {@code Result} is a failure.
     */
    public V valueOrThrow() {
        if (isSuccess()) {
            return value();
        }
        if (error instanceof RuntimeException) {
            throw (RuntimeException) error;
        }
        throw new RuntimeException(error());
    }
}
