package eu.arrowhead.kalix.util;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Various utilities for working will collections of {@link Result Results}.
 */
public final class Results {
    private Results() {}

    /**
     * Iterates through every {@code Result} in {@code array} and merges any
     * {@link Throwable Throwables} found in faulty results. Successful results
     * are ignored.
     * <p>
     * Merging proceeds as follows: If a fault is encountered, save it. If a
     * fault has already been found when another is encountered, add the old
     * fault as a suppressed exception to the new one.
     *
     * @param array Array of {@code Results}.
     * @return Result failed if {@code array} contains one or more faults.
     */
    public static Result<?> mergeFaults(final Result<?>[] array) {
        return mergeFaults(Arrays.asList(array));
    }

    /**
     * Iterates through every {@code Result} in {@code iterable} and merges any
     * {@link Throwable Throwables} found in faulty results. Successful results
     * are ignored.
     * <p>
     * Merging proceeds as follows: If a fault is encountered, save it. If a
     * fault has already been found when another is encountered, add the old
     * fault as a suppressed exception to the new one.
     *
     * @param iterable Array of {@code Results}.
     * @return Result failed if {@code iterable} contains one or more faults.
     */
    public static Result<?> mergeFaults(final Iterable<? extends Result<?>> iterable) {
        return mergeFaults(iterable.iterator());
    }

    /**
     * Iterates through every {@code Result} in {@code iterator} and merges any
     * {@link Throwable Throwables} found in faulty results. Successful results
     * are ignored.
     * <p>
     * Merging proceeds as follows: If a fault is encountered, save it. If a
     * fault has already been found when another is encountered, add the old
     * fault as a suppressed exception to the new one.
     *
     * @param iterator Array of {@code Results}.
     * @return Result failed if {@code iterator} contains one or more faults.
     */
    public static Result<?> mergeFaults(final Iterator<? extends Result<?>> iterator) {
        Throwable lastFault = null;
        while (iterator.hasNext()) {
            final var result = iterator.next();
            if (!result.isSuccess()) {
                final var fault = result.fault();
                if (lastFault != null) {
                    fault.addSuppressed(lastFault);
                }
                lastFault = fault;
            }
        }
        return lastFault != null
            ? Result.failure(lastFault)
            : Result.done();
    }
}
