package eu.arrowhead.kalix.net.http;

import java.util.*;

/**
 * A collection of HTTP header name/value pairs.
 * <p>
 * Internally, all header names are stored in lowercase form, as it is required
 * if sending them in HTTP/2.0 messages (see RFC 7540, Section 8.1.2).
 *
 * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.2">RFC 7230, Section 3.2</a>
 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5">RFC 7231, Section 5</a>
 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7">RFC 7231, Section 7</a>
 * @see <a href="https://tools.ietf.org/html/rfc7540#section-8.1.2">RFC 7540, Section 8.1.2</a>
 * @see <a href="https://www.iana.org/assignments/message-headers/message-headers.xhtml">IANA Message Headers</a>
 */
public interface HttpHeaders extends Iterable<Map.Entry<CharSequence, CharSequence>> {
    /**
     * Acquires value of first header associated with given name, if any such
     * exists in this collection.
     *
     * @param name Name of header. Not case sensitive. Prefer lowercase.
     * @return Desired value, if available.
     */
    Optional<String> get(final CharSequence name);

    /**
     * Acquires value of first header associated with given name, if any such
     * exists in this collection, and then attempts to convert it into an
     * integer.
     *
     * @param name Name of header. Not case sensitive. Prefer lowercase.
     * @return Desired value, if available, as integer.
     * @throws NumberFormatException If value exists and is not a properly
     *                               formatted number.
     */
    default Optional<Integer> getAsInteger(final CharSequence name) {
        return get(name).map(value -> Integer.parseInt(value, 0, value.length(), 10));
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
    HttpHeaders add(final CharSequence name, final CharSequence value);

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
     * Sets header, replacing all such previously set with the same name.
     *
     * @param name  Name of header. Not case sensitive. Prefer lowercase.
     * @param value New header value.
     * @return This collection.
     */
    HttpHeaders set(final CharSequence name, final CharSequence value);

    /**
     * Removes any existing headers with the given name, and then adds headers,
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
     * Removes any existing headers with the given name, and then adds headers,
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
