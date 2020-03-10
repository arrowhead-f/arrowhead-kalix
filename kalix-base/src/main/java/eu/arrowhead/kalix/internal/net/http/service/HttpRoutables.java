package eu.arrowhead.kalix.internal.net.http.service;

import eu.arrowhead.kalix.net.http.service.*;
import eu.arrowhead.kalix.util.annotation.Internal;

/**
 * Various utilities related to {@link HttpRoutable}s.
 */
@Internal
public class HttpRoutables {
    private HttpRoutables() {}

    /**
     * Compares given routables <i>a</i> and <i>b</i> such that -1 is returned
     * if <i>a</i> is more specialized than <i>b</i>, 1 is returned if <i>a</i>
     * is less specialized than <i>b</i> and 0 is returned if their patterns
     * and methods are equal.
     * <p>
     * A routable is more specialized than another such only if the subset of
     * method/path combinations it can match is smaller than  that of another
     * routable.
     *
     * @param a First routable to compare.
     * @param b Second routable to compare.
     * @return A negative integer, zero, or a positive integer depending on
     * whether <i>a</i> is more specialized, equal to, or less specialized than
     * <i>b</i>, respectively.
     */
    public static int compare(final HttpRoutable a, final HttpRoutable b) {
        // Routables with patterns come before those without patterns.
        final var ap = a.pattern();
        final var bp = b.pattern();
        if (ap.isPresent()) {
            if (bp.isEmpty()) {
                return -1;
            }
            final var c = ap.get().compareTo(bp.get());
            if (c != 0) {
                return c;
            }
        }
        else if (bp.isPresent()) {
            return 1;
        }

        // Routables with methods come before those without methods.
        final var am = a.method();
        final var bm = b.method();
        if (am.isPresent()) {
            if (bm.isEmpty()) {
                return -1;
            }
            return am.get().compareTo(bm.get());
        }
        else if (bm.isPresent()) {
            return 1;
        }

        return 0;
    }

    /**
     * Compares given catchers <i>a</i> and <i>b</i> such that -1 is returned
     * if <i>a</i> is more specialized than <i>b</i>, 1 is returned if <i>a</i>
     * is less specialized than <i>b</i> and 0 is returned if their patterns,
     * methods and exception classes are equal.
     * <p>
     * A catcher is more specialized than another such only if the subset of
     * method/path/exception combinations it can match is smaller than that of
     * another catcher.
     *
     * @param a First catcher to compare.
     * @param b Second catcher to compare.
     * @return A negative integer, zero, or a positive integer depending on
     * whether <i>a</i> is more specialized, equal to, or less specialized than
     * <i>b</i>, respectively.
     */
    public static int compareCatchers(final HttpCatcher<?> a, final HttpCatcher<?> b) {
        final var cr = compare(a, b);
        if (cr != 0) {
            return cr;
        }
        // Catchers with more specialized exception classes come before those
        // with less specialized exception classes.
        final var ae = a.exceptionClass();
        final var be = b.exceptionClass();
        if (ae == be) {
            return a.ordinal() - b.ordinal();
        }
        if (ae.isAssignableFrom(be)) {
            return 1;
        }
        return -1;
    }

    /**
     * Compares given validators <i>a</i> and <i>b</i> such that -1 is returned
     * if <i>a</i> is more specialized than <i>b</i>, 1 is returned if <i>a</i>
     * is less specialized than <i>b</i> and 0 is returned if their patterns,
     * methods and exception classes are equal.
     * <p>
     * A validator is more specialized than another such only if the subset of
     * method/path/exception combinations it can match is smaller than that of
     * another validator.
     *
     * @param a First validator to compare.
     * @param b Second validator to compare.
     * @return A negative integer, zero, or a positive integer depending on
     * whether <i>a</i> is more specialized, equal to, or less specialized than
     * <i>b</i>, respectively.
     */
    public static int compareValidators(final HttpValidator a, final HttpValidator b) {
        final var cr = compare(a, b);
        if (cr != 0) {
            return cr;
        }
        return a.ordinal() - b.ordinal();
    }

    /**
     * Determines whether there are method/path pairs that would match both
     * provided routables.
     *
     * @param a First routable to test.
     * @param b Second routable to test.
     * @return {@code true} only if an intersection of matching method/path
     * pairs exists between the given filter and route.
     */
    public static boolean intersect(final HttpRoutable a, final HttpRoutable b) {
        final var am = a.method();
        if (am.isPresent()) {
            final var bm = b.method();
            if (bm.isPresent() && bm.get() != am.get()) {
                return false;
            }
        }
        final var ap = a.pattern();
        if (ap.isPresent()) {
            final var bp = b.pattern();
            return bp.isEmpty() || ap.get().intersectsWith(bp.get());
        }
        return true;
    }
}
