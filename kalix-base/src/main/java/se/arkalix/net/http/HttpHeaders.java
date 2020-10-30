package se.arkalix.net.http;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * A collection of HTTP header name/value pairs, where names are
 * case-insensitive.
 * <p>
 * When using this class, it is recommended to always use the lower-case form
 * of header names. Lower-case is required when using HTTP/2.0 (see RFC 7540,
 * Section 8.1.2), and sticking to lower-case means that less conversion
 * overhead is introduced when such messages are sent.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.2">RFC 7230, Section 3.2</a>
 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5">RFC 7231, Section 5</a>
 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7">RFC 7231, Section 7</a>
 * @see <a href="https://tools.ietf.org/html/rfc7540#section-8.1.2">RFC 7540, Section 8.1.2</a>
 * @see <a href="https://www.iana.org/assignments/message-headers/message-headers.xhtml">IANA Message Headers</a>
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface HttpHeaders {
    /**
     * Acquires value of first header associated with given name, if any such
     * exists in this collection.
     *
     * @param name Name of header. Not case sensitive. Prefer lowercase.
     * @return Desired value, if available.
     */
    default Optional<String> get(final CharSequence name) {
        final var values = getAll(name);
        if (values.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(values.get(0));
    }

    /**
     * Acquires value of first header associated with given name, if any such
     * exists in this collection, and then attempts to transform it using
     * given mapper function.
     *
     * @param name   Name of header. Not case sensitive. Prefer lowercase.
     * @param mapper Function to apply to header.
     * @return Desired value, if available, as returned by {@code mapper}.
     * @throws HttpHeaderInvalid If value exists but mapper threw any {@link
     *                           Exception}.
     */
    default <T> Optional<T> getAs(final CharSequence name, final Function<String, T> mapper) {
        String value = null;
        try {
            value = get(name).orElse(null);
            if (value == null || value.isBlank()) {
                return Optional.empty();
            }
            return Optional.ofNullable(mapper.apply(value));
        }
        catch (final Exception exception) {
            throw new HttpHeaderInvalid(name.toString(), value, exception);
        }
    }

    /**
     * Acquires value of first header associated with given name, if any such
     * exists in this collection, and then attempts to convert it into an
     * integer.
     *
     * @param name Name of header. Not case sensitive. Prefer lowercase.
     * @return Desired value, if available, as integer.
     * @throws HttpHeaderInvalid If value exists but is not a properly
     *                           formatted number.
     */
    default Optional<Integer> getAsInteger(final CharSequence name) {
        return getAs(name, Integer::parseInt);
    }

    /**
     * Acquires the values of all headers with the provided name.
     *
     * @param name Name of header value to get and split, as described above.
     *             Note that header names are not case sensitive.
     * @return List of values, which may be empty.
     */
    List<String> getAll(final CharSequence name);

    /**
     * Adds header to this collection without replacing any existing header
     * with the same name.
     *
     * @param name  Name of header. Not case sensitive. Prefer lowercase.
     * @param value New header value.
     * @return This collection.
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.2.2">RFC 7230, Section 3.2.2</a>
     */
    default HttpHeaders add(final CharSequence name, final CharSequence value) {
        return add(name, Collections.singletonList(value.toString()));
    }

    /**
     * Adds headers, all with the given name, to this collection without
     * replacing any existing pair with that name.
     * <p>
     * The method may be though of as performing the equivalent of the
     * following code:
     * <pre>
     *     for (final var value : values) {
     *         headers.add(name, value);
     *     }
     * </pre>
     *
     * @param name   Name of header. Not case sensitive. Prefer lowercase.
     * @param values New values to associate with header name.
     * @return This collection.
     * @see #add(CharSequence, CharSequence)
     */
    HttpHeaders add(final CharSequence name, final Iterable<String> values);

    /**
     * Adds headers, all with the given name, to this collection without
     * replacing any existing pair with that name.
     * <p>
     * The method may be though of as performing the equivalent of the
     * following code:
     * <pre>
     *     for (final var value : values) {
     *         headers.add(name, value);
     *     }
     * </pre>
     *
     * @param name   Name of header. Not case sensitive. Prefer lowercase.
     * @param values New values to associate with header name.
     * @return This collection.
     * @see #add(CharSequence, CharSequence)
     */
    default HttpHeaders add(final CharSequence name, final String... values) {
        return add(name, Arrays.asList(values));
    }

    /**
     * Checks whether this collection contains any header matching the given
     * name.
     *
     * @param name Name of header. Not case sensitive. Prefer lowercase.
     * @return {@code true} only if this collection contains at least one
     * header matching given {@code name}.
     */
    boolean contains(final CharSequence name);

    /**
     * Sets header, replacing all such previously set with the same name.
     *
     * @param name  Name of header. Not case sensitive. Prefer lowercase.
     * @param value New header value.
     * @return This collection.
     */
    default HttpHeaders set(final CharSequence name, final CharSequence value) {
        return set(name, Collections.singletonList(value.toString()));
    }

    /**
     * Removes any existing headers with the given name and then adds headers,
     * all with the given name, to this collection.
     * <p>
     * The method may be though of as performing the equivalent of the
     * following code:
     * <pre>
     *     headers.remove(name);
     *     for (final var value : values) {
     *         headers.add(name, value);
     *     }
     * </pre>
     *
     * @param name   Name of header. Not case sensitive. Prefer lowercase.
     * @param values New values to associate with header name.
     * @return This collection.
     * @see #add(CharSequence, CharSequence)
     */
    HttpHeaders set(final CharSequence name, final Iterable<String> values);

    /**
     * Removes any existing headers with the given name and then adds headers,
     * all with the given name, to this collection.
     * <p>
     * The method may be though of as performing the equivalent of the
     * following code:
     * <pre>
     *     headers.remove(name);
     *     for (final var value : values) {
     *         headers.add(name, value);
     *     }
     * </pre>
     *
     * @param name   Name of header. Not case sensitive. Prefer lowercase.
     * @param values New values to associate with header name.
     * @return This collection.
     * @see #add(CharSequence, CharSequence)
     */
    default HttpHeaders set(final CharSequence name, final String... values) {
        return set(name, Arrays.asList(values));
    }

    /**
     * Sets header only if no such exists with the same name.
     *
     * @param name  Name of header. Not case sensitive. Prefer lowercase.
     * @param value New header value.
     * @return This collection.
     */
    default HttpHeaders setIfEmpty(final CharSequence name, final CharSequence value) {
        if (!contains(name)) {
            set(name, value);
        }
        return this;
    }

    /**
     * Sets headers, all with the given name only if no such exists with the
     * same name.
     * <p>
     * The method may be though of as performing the equivalent of the
     * following code:
     * <pre>
     *     if (headers.get(name).isEmpty()) {
     *         for (final var value : values) {
     *             headers.add(name, value);
     *         }
     *     }
     * </pre>
     *
     * @param name   Name of header. Not case sensitive. Prefer lowercase.
     * @param values New values to associate with header name.
     * @return This collection.
     * @see #add(CharSequence, CharSequence)
     */
    default HttpHeaders setIfEmpty(final CharSequence name, final Iterable<String> values) {
        if (!contains(name)) {
            set(name, values);
        }
        return this;
    }

    /**
     * Sets headers, all with the given name only if no such exists with the
     * same name.
     * <p>
     * The method may be though of as performing the equivalent of the
     * following code:
     * <pre>
     *     if (headers.get(name).isEmpty()) {
     *         for (final var value : values) {
     *             headers.add(name, value);
     *         }
     *     }
     * </pre>
     *
     * @param name   Name of header. Not case sensitive. Prefer lowercase.
     * @param values New values to associate with header name.
     * @return This collection.
     * @see #add(CharSequence, CharSequence)
     */
    default HttpHeaders setIfEmpty(final CharSequence name, final String... values) {
        if (!contains(name)) {
            set(name, Arrays.asList(values));
        }
        return this;
    }

    /**
     * Removes all headers with given {@code name} from this collection.
     *
     * @param name Name of pair to remove.
     * @return This collection.
     */
    HttpHeaders remove(final CharSequence name);

    /**
     * Removes all headers from this collection.
     *
     * @return This collection.
     */
    HttpHeaders clear();
}
