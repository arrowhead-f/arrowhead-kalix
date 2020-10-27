package se.arkalix.net.media;

import se.arkalix.net.Encoding;
import se.arkalix.net.ToEncoding;

import java.util.*;

public final class MediaType implements ToEncoding {
    private final String original;

    private final String type;
    private final String facets;
    private final String subtype;
    private final String suffix;
    private final Map<String, String> parameters;

    private final Encoding encoding;

    private MediaType(
        final String original,
        final String type,
        final String facets,
        final String subtype,
        final String suffix,
        final Map<String, String> parameters,
        final Encoding encoding
    ) {
        this.original = original;
        this.type = Objects.requireNonNull(type);
        this.facets = facets;
        this.subtype = Objects.requireNonNull(subtype);
        this.suffix = suffix;
        this.parameters = Objects.requireNonNullElse(parameters, Collections.emptyMap());
        this.encoding = encoding;
    }

    public String type() {
        return type;
    }

    public String subtype() {
        return subtype;
    }

    public Optional<String> facets() {
        return Optional.ofNullable(facets);
    }

    public Optional<String> suffix() {
        return Optional.ofNullable(suffix);
    }

    public Map<String, String> parameters() {
        return parameters;
    }

    @Override
    public Encoding toEncoding() {
        return encoding;
    }

    @Override
    public String toString() {
        return original;
    }

    public static MediaType valueOf(final String string) {
        String error = null;
        error:
        {
            final int s4 = string.length();
            // s0 = end of type, s1 = end of facets, s2 = start of suffix, s3 = end of subtype
            int s0 = 0, s1 = 0, s2 = 0, s3 = 0;

            // Type.
            char ch;
            if (s0 < s4) {
                ch = string.charAt(s0++);
                if (isNotRestrictedNameFirst(ch)) {
                    error = "Invalid type lead character '" + ch + "'";
                    break error;
                }
                while (s0 < s4) {
                    ch = string.charAt(s0++);
                    if (ch == '/') {
                        break;
                    }
                    if (isNotRestrictedNameChar(ch)) {
                        error = "Invalid type character '" + ch + "'";
                        break error;
                    }
                }
            }
            if (s0 == s4) {
                break error;
            }

            // Subtype.
            int sx = s0;
            outer:
            while (sx < s4) {
                ch = string.charAt(sx++);
                if (isNotRestrictedNameFirst(ch)) {
                    error = "Invalid subtype or facet lead character '" + ch + "'";
                    break error;
                }
                while (sx < s4) {
                    ch = string.charAt(sx);
                    if (ch == '.') {
                        s1 = sx;
                        continue outer;
                    }
                    if (ch == '+') {
                        s2 = sx;
                        break outer;
                    }
                    if (ch == ';' || isWhitespace(ch)) {
                        s3 = sx;
                        break outer;
                    }
                    if (isNotRestrictedNameChar(ch)) {
                        error = "Invalid subtype or facet character '" + ch + "'";
                        break error;
                    }
                    sx++;
                }
            }

            // Suffix.
            if (s2 != 0) {
                outer: while (s3 < s4) {
                    ch = string.charAt(s3++);
                    if (isNotRestrictedNameFirst(ch)) {
                        error = "Invalid suffix lead character '" + ch + "'";
                        break error;
                    }
                    while (s3 < s4) {
                        ch = string.charAt(s3);
                        if (ch == ';' || isWhitespace(ch)) {
                            break outer;
                        }
                        if (isNotRestrictedNameChar(ch)) {
                            error = "Invalid suffix character '" + ch + "'";
                            break error;
                        }
                        s3++;
                    }
                }
                if (s3 - s2 == 0) {
                    error = "Empty suffixes not permitted";
                    break error;
                }
            }

            final String type;
            final String facets;
            final String subtype;
            final String suffix;
            final Encoding encoding;

            type = string.substring(0, s0 - 1);
            if (s1 == 0) {
                facets = null;
                if (s2 == 0) {
                    subtype = string.substring(s0, s3);
                    suffix = null;
                }
                else {
                    subtype = string.substring(s0, s2);
                    suffix = string.substring(s2, s3);
                }
            }
            else {
                facets = string.substring(s0, s1);
                if (s2 == 0) {
                    subtype = string.substring(s1, s3);
                    suffix = null;
                }
                else {
                    subtype = string.substring(s1, s2);
                    suffix = string.substring(s2, s3);
                }
            }

            if (suffix == null) {
                if (subtype.regionMatches(true, subtype.length() - 4, "-exi", 0, 4)) {
                    encoding = Encoding.EXI;
                }
                else {
                    encoding = Encoding.getOrCreate(subtype);
                }
            }
            else {
                encoding = Encoding.getOrCreate(suffix);
            }

            final Map<String, String> parameters;
            if (s3 < s4) {

                // s0 = start of parameter name, s1 = end of parameter name, s3 = start of value, s4 = end of value

                final var parameters0 = new HashMap<String, String>();
                do {
                    s0 = s3;

                    // Skip whitespace.
                    do {
                        ch = string.charAt(s0++);
                        if (!isWhitespace(ch)) {
                            break;
                        }
                    } while (s0 < s4);
                    if (s0 == s4) {
                        break;
                    }

                    if (ch != ';') {
                        break error;
                    }

                    // Parameter name.
                    s1 = s0;
                    ch = string.charAt(s1++);
                    if (isNotRestrictedNameFirst(ch)) {
                        error = "Invalid parameter lead character '" + ch + "'";
                        break error;
                    }
                    while (s1 < s4) {
                        ch = string.charAt(s1);
                        if (ch == '=' || isWhitespace(ch)) {
                            break;
                        }
                        if (isNotRestrictedNameChar(ch)) {
                            error = "Invalid parameter character '" + ch + "'";
                            break error;
                        }
                        s1++;
                    }

                    // Skip whitespace.
                    s2 = s1;
                    do {
                        ch = string.charAt(s2++);
                        if (!isWhitespace(ch)) {
                            break;
                        }
                    } while (s2 < s4);
                    if (s2 == s4) {
                        break error;
                    }

                    // Value.
                    s3 = s2;
                    while (s3 < s4) {
                        ch = string.charAt(s3++);
                        if (ch == ';') {
                            break;
                        }
                    }

                    parameters0.put(string.substring(s0, s1), string.substring(s2, s3));
                } while (s3 < s4);

                parameters = Collections.unmodifiableMap(parameters0);
            }
            else {
                parameters = null;
            }

            return new MediaType(string, type, facets, subtype, suffix, parameters, encoding);
        }
        if (error == null) {
            error = "Expected '<type>/<subtype>[;<parameter-name>=<parameter-value>]*'; got '" + string + "'";
        }
        throw new IllegalArgumentException(error);
    }

    private static boolean isNotRestrictedNameFirst(final char ch) {
        return (ch < '0' || ch > '9') && (ch < 'A' || ch > 'Z') && (ch < 'a' || ch > 'z');
    }

    private static boolean isNotRestrictedNameChar(final char ch) {
        return (ch < '0' || ch > '9') && (ch < 'A' || ch > 'Z') && (ch < 'a' || ch > 'z') &&
            ch != '!' && ch != '#' && ch != '$' && ch != '&' && ch != '-' && ch != '^' && ch != '_';
    }

    private static boolean isWhitespace(final char ch) {
        return ch == '\r' || ch == '\n' || ch == ' ';
    }
}
