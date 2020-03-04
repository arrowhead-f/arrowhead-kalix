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
public class HttpHeaders implements Iterable<Map.Entry<String, String>> {
    private final HashMap<String, String> map = new HashMap<>();

    /**
     * Creates new empty collection of HTTP header name/value pairs.
     */
    public HttpHeaders() {}

    /**
     * Adds name/value pair to collection without replacing any existing pair
     * with the same name.
     * <p>
     * Internally, instances of this class <i>may</i> resolve the situation of
     * a name already existing by joining their values with a comma
     * ({@code ,}). This behavior is in line with what is permitted by RFC
     * 7230, Section 3.2.2, which states that <i>"recipients MAY combine
     * multiple header fields with the same field name into one 'field-name:
     * field-value' pair, without changing the semantics of the message, by
     * appending each subsequent field value to the combined field value in
     * order, separated by a comma."</i> As this behavior is allowed by message
     * recipients, it must, as a consequence, also be allowed by message
     * senders.
     * <p>
     * Adhering to this policy comes with one complication, however, and that
     * is properly supporting "set-cookie" headers, as their value syntax does
     * not lend itself to joining values by commas. It is, however, highly
     * questionable whether cookies have a place at all in the context of
     * Arrowhead Framework, for which reason no guarantees about supporting
     * them properly are given here.
     *
     * @param name  Name of header. Not case sensitive.
     * @param value Value of header.
     * @return This collection.
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.2.2">RFC 7230, Section 3.2.2</a>
     */
    public HttpHeaders add(final String name, final String value) {
        Objects.requireNonNull(name, "Expected name");
        Objects.requireNonNull(value, "Expected value");
        map.compute(name.toLowerCase(), (k, v) -> v == null ? value : v + "," + value);
        return this;
    }

    /**
     * Acquires value associated with given name, if exists in this collection.
     * <p>
     * Note that HTTP headers are allowed to have multiple values separated by
     * commas ({@code ,}). If you expect a name to be associated with multiple
     * values, consider using {@link #getAll(String)} to ensure that they
     * are extracted properly.
     *
     * @param name Name associated with desired value. Not case sensitive.
     * @return Desired value, if available.
     */
    public Optional<String> get(final String name) {
        Objects.requireNonNull(name);
        return Optional.ofNullable(map.get(name.toLowerCase()));
    }

    /**
     * Acquires all values associated with provided header name.
     * <p>
     * Internally, instances of this class <i>may</i> store values associated
     * with the same name as comma-separated strings of text. If so, then this
     * method locates the sought-after header name and, if found, splits it
     * value and returns the chunks. Splitting is performed according to the
     * rules outlined in RFC 7230, Section 3.2.6, which means that escapes and
     * double quotes are honored. While RFC 7230, Section 3.2.6 lists all of
     * <i>(),/:;<=>?@[\]{}</i> as possible value delimiters, only the comma is
     * guaranteed to always function as a delimiter for all types of values
     * (see Section 3.2.2), with the exception of the "set-cookie" header.
     * However, "set-cookie" headers are not guaranteed to be given any special
     * treatment by this implementation, as their use within the Arrowhead
     * Framework is highly questionable.
     *
     * @param name Name of header value to get and split, as described above.
     *             Note that header names are not case sensitive.
     * @return List of values, if any.
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.2.2">RFC 7230, Section 3.2.2</a>
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.2.6">RFC 7230, Section 3.2.6</a>
     */
    public List<String> getAll(final String name) {
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
                var c = value.charAt(v0);
                if (c <= ' ') {
                    continue; // Skip white space and control characters.
                }
                if (c == '"') {
                    for (++v0; v0 < v1; ++v0) {
                        c = value.charAt(v0);
                        if (c == '\\' && v0 + 1 < v1) {
                            c = value.charAt(++v0); // Append whatever is after the backslash.
                        }
                        else if (c == '"') {
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
            result.add(builder.toString());
            if (result.size() == 0) {
                break empty;
            }
            return result;
        }
        return Collections.emptyList();
    }

    /**
     * Sets header name/value pair, potentially replacing a previously set
     * such with the same name.
     *
     * @param name  Name of header. Not case sensitive.
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
    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return map.entrySet().iterator();
    }
}
