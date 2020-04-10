package se.arkalix.net.http;

import se.arkalix.util.annotation.Internal;
import io.netty.handler.codec.http.DefaultHttpHeaders;

import java.util.*;

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
public class HttpHeaders {
    private final io.netty.handler.codec.http.HttpHeaders headers;

    /**
     * Creates new empty collection of {@code HttpHeaders}.
     */
    public HttpHeaders() {
        headers = new DefaultHttpHeaders();
    }

    /**
     * <i>Internal API</i>. Might change in breaking ways between patch
     * versions of the Kalix library. Use is not advised.
     */
    @Internal
    public HttpHeaders(final io.netty.handler.codec.http.HttpHeaders headers) {
        this.headers = headers;
    }

    /**
     * Acquires value of first header associated with given name, if any such
     * exists in this collection.
     *
     * @param name Name of header. Not case sensitive. Prefer lowercase.
     * @return Desired value, if available.
     */
    public Optional<String> get(final CharSequence name) {
        return Optional.ofNullable(headers.get(name));
    }

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
    public Optional<Integer> getAsInteger(final CharSequence name) {
        return Optional.ofNullable(headers.getInt(name));
    }

    /**
     * Acquires the values of all headers with the provided name.
     *
     * @param name Name of header value to get and split, as described above.
     *             Note that header names are not case sensitive.
     * @return List of values, which may be empty.
     */
    public List<String> getAll(final CharSequence name) {
        return headers.getAll(name);
    }

    /**
     * Adds header to this collection without replacing any existing header
     * with the same name.
     *
     * @param name  Name of header. Not case sensitive. Prefer lowercase.
     * @param value New header value.
     * @return This collection.
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.2.2">RFC 7230, Section 3.2.2</a>
     */
    public HttpHeaders add(final CharSequence name, final CharSequence value) {
        headers.add(name, value);
        return this;
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
    public HttpHeaders add(final CharSequence name, final Iterable<String> values) {
        headers.add(name, values);
        return this;
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
    public HttpHeaders add(final CharSequence name, final String... values) {
        return add(name, Arrays.asList(values));
    }

    /**
     * Sets header, replacing all such previously set with the same name.
     *
     * @param name  Name of header. Not case sensitive. Prefer lowercase.
     * @param value New header value.
     * @return This collection.
     */
    public HttpHeaders set(final CharSequence name, final CharSequence value) {
        headers.set(name, value);
        return this;
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
    public HttpHeaders set(final CharSequence name, final Iterable<String> values) {
        headers.set(name, values);
        return this;
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
    public HttpHeaders set(final CharSequence name, final String... values) {
        return set(name, Arrays.asList(values));
    }

    /**
     * Sets header only if no such exists with the same name.
     *
     * @param name  Name of header. Not case sensitive. Prefer lowercase.
     * @param value New header value.
     * @return This collection.
     */
    public HttpHeaders setIfEmpty(final CharSequence name, final CharSequence value) {
        if (!headers.contains(name)) {
            headers.set(name, value);
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
    public HttpHeaders setIfEmpty(final CharSequence name, final Iterable<String> values) {
        if (!headers.contains(name)) {
            headers.set(name, values);
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
    public HttpHeaders setIfEmpty(final CharSequence name, final String... values) {
        if (!headers.contains(name)) {
            headers.set(name, Arrays.asList(values));
        }
        return this;
    }

    /**
     * Removes all headers with given {@code name} from this collection.
     *
     * @param name Name of pair to remove.
     * @return This collection.
     */
    public HttpHeaders remove(final CharSequence name) {
        headers.remove(name);
        return this;
    }

    /**
     * Removes all headers from this collection.
     *
     * @return This collection.
     */
    public HttpHeaders clear() {
        headers.clear();
        return this;
    }

    /**
     * <i>Internal API</i>. Might change in breaking ways between patch
     * versions of the Kalix library. Use is not advised.
     */
    @Internal
    public io.netty.handler.codec.http.HttpHeaders unwrap() {
        return headers;
    }
}
