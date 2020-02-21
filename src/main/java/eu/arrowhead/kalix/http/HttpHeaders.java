package eu.arrowhead.kalix.http;

import java.util.*;

/**
 * A collection of HTTP header name/value pairs.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.2">RFC 7230, Section 3.2</a>
 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5">RFC 7231, Section 5</a>
 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7">RFC 7231, Section 7</a>
 * @see <a href="https://www.iana.org/assignments/message-headers/message-headers.xhtml">IANA Message Headers</a>
 */
public class HttpHeaders {
    private final HashMap<String, String> map = new HashMap<>();

    /**
     * Creates new empty collection of HTTP header name/value pairs.
     */
    public HttpHeaders() {}

    /**
     * Adds name/value pair to collection without replacing any existing pair
     * with the same name. In the case of a name already existing, a comma
     * ({@code ,}) and the provided value are appended to the value associated
     * with that name.
     * <p>
     * This behavior is in line with what is permitted by RFC 7230, Section
     * 3.2.2, which states that <i>"recipients MAY combine multiple header
     * fields with the same field name into one 'field-name: field-value' pair,
     * without changing the semantics of the message, by appending each
     * subsequent field value to the combined field value in order, separated
     * by a comma."</i> As this behavior is allowed by message recipients, it
     * must, as a consequence, also be allowed by message senders. Enforcing
     * this policy comes with one complication, however, and that is properly
     * supporting "set-cookie" headers, as their value syntax does not lend
     * itself to joining values by commas. It is, however, highly questionable
     * whether cookies has a place at all in the context of Arrowhead
     * Framework, for which reason no room is provided by this collection type
     * for properly representing more than one "set-cookie" pair.
     *
     * @param name  Name of header.
     * @param value Value of header.
     * @return This collection.
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.2.2">RFC 7230, Section 3.2.2</a>
     */
    public HttpHeaders add(final String name, final String value) {
        Objects.requireNonNull(name);
        map.compute(name.toLowerCase(), (k, v) -> v == null ? value : v + "," + value);
        return this;
    }

    /**
     * Acquires value associated with given name, if exists in this collection.
     * <p>
     * Note that HTTP headers are allowed to have multiple values separated by
     * commas ({@code ,}). If you expect a name to be associated with multiple
     * values, consider using {@link #getAndSplit(String)} to ensure that they
     * are extracted properly.
     *
     * @param name Name associated with desired value.
     * @return Desired value, if available.
     */
    public Optional<String> get(final String name) {
        Objects.requireNonNull(name);
        return Optional.ofNullable(map.get(name.toLowerCase()));
    }

    /**
     * Acquires values, separated by commas ({@code ,}), associated with given
     * name.
     * <p>
     * While RFC 7230, Section 3.2.6 lists all of <i>(),/:;<=>?@[\]{}</i> as
     * possible value delimiters, only the comma is guaranteed to always
     * function as a delimiter for all types of values (see Section 3.2.2).
     * Furthermore, Section 3.2.6 states that a <i>"string of text is parsed as
     * a single value if it is quoted using double-quote marks"</i>, as well as
     * describing an escaping mechanism for including quotes inside double
     * quoted values. While this method takes care of commas and double quoted
     * strings, it does nothing with the other delimiters. Finally, values are
     * assumed to be well-formed, which means no time is spent verifying them
     * and no exceptions are ever thrown due to syntax inconsistencies.
     *
     * @param name Name of header value to get and split, as described above.
     * @return List of values, if any.
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.2.2">RFC 7230, Section 3.2.2</a>
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.2.6">RFC 7230, Section 3.2.6</a>
     */
    public Optional<String[]> getAndSplit(final String name) {
        empty:
        {
            Objects.requireNonNull(name);
            final var value = map.get(name.toLowerCase());
            if (value == null) {
                break empty;
            }
            final var result = new ArrayList<String>(4);
            final var builder = new StringBuilder();
            final int v1 = value.length();
            for (var v0 = 0; v0 < v1; ++v0) {
                var c = value.charAt(v0++);
                if (c < ' ') {
                    continue; // Skip white space and control characters.
                }
                if (c == '"') {
                    for (++v0; v0 < v1; ++v0) {
                        c = value.charAt(v0);
                        if (c == '\\' && v0 + 1 < v1) {
                            v0 += 1; // Skip whatever is after the backslash.
                            continue;
                        }
                        if (c == '"') {
                            break;
                        }
                        builder.append(c);
                    }
                    continue;
                }
                if (c == ',') {
                    result.add(builder.toString());
                    builder.setLength(0);
                    continue;
                }
                builder.append(c);
            }
            if (result.size() == 0) {
                break empty;
            }
            return Optional.of(result.toArray(new String[0]));
        }
        return Optional.empty();
    }

    /**
     * Sets header name/value pair, potentially replacing a previously set
     * such with the same name.
     *
     * @param name  Name of header.
     * @param value Value of header.
     * @return This collection.
     */
    public HttpHeaders set(final String name, final String value) {
        Objects.requireNonNull(name);
        map.put(name.toLowerCase(), value);
        return this;
    }

    /**
     * @return Iterator over all header name/value pairs.
     */
    public Iterator<Map.Entry<String, String>> iterator() {
        return map.entrySet().iterator();
    }
}
