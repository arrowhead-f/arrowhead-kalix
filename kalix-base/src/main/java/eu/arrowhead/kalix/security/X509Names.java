package eu.arrowhead.kalix.security;

import eu.arrowhead.kalix.internal.util.charset.Unicode;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Various utilities for working with LDAP Distinguished Names (DNs), which
 * are, most significantly, used to identify the issuer and subject of x.509
 * certificates.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5280#section-4.1">RFC 5280, Section 4.1</a>
 */
public class X509Names {
    private X509Names() {}

    private static final String ERROR_BAD_ESCAPE = "Invalid CN; bad escape sequence";
    private static final String ERROR_BAD_HEX_CHARACTER = "Invalid CN; bad hex pair character";
    private static final String ERROR_INCOMPLETE_HEX_PAIR = "Invalid CN; incomplete hex pair";
    private static final String ERROR_TERMINATING_BACKSLASH = "Invalid CN; terminating backslash";
    private static final String ERROR_UNEXPECTED_CHARACTER = "Invalid CN; unexpected reserved character";

    /**
     * Searches for and extracts the Common Name (CN) from given DN, and then
     * converts it into an {@link X509ArrowheadName}.
     * <p>
     * As an LDAP DN is hierarchical, with the top level being the rightmost
     * Relative DN (RDN), the leftmost RDN containing a CN is assumed to
     * contain the most specific domain name. If multiple CNs are stated in
     * provided DN, only the leftmost CN is converted and returned.
     *
     * @param dn String representation of LDAP DN, as described in RFC 4515,
     *           Section 3, as well as RFC 4512, Section 1.4.
     * @return x.509 Arrowhead name, if a valid CN is present in given DN
     * string.
     * @throws X509NameException If provided DN string is not standards-
     *                           compliant.
     * @see <a href="https://tools.ietf.org/html/rfc4514#section-3">RFC 4515, Section 3</a>
     * @see <a href="https://tools.ietf.org/html/rfc4512#section-1.4">RFC 4512, Section 1.4</a>
     */
    public static Optional<X509ArrowheadName> arrowheadNameOf(final String dn) throws X509NameException {
        return commonNameOf(dn).map(X509ArrowheadName::valueOf);
    }

    /**
     * Identifies and extracts leftmost Common Name (CN) in given LDAP DN.
     * <p>
     * As an LDAP DN is hierarchical, with the top level being the rightmost
     * Relative DN (RDN), the leftmost RDN containing a CN is assumed to
     * contain the most specific domain name. If multiple CNs are stated in
     * provided DN, only the leftmost CN is returned.
     *
     * @param dn String representation of an LDAP DN, as described in RFC 4515,
     *           Section 3, as well as RFC 4512, Section 1.4.
     * @return Extracted, escaped and whitespace-trimmed CN, if any such exists
     * in given DN.
     * @throws X509NameException If provided DN string is not standards-
     *                           compliant.
     * @see <a href="https://tools.ietf.org/html/rfc4514#section-3">RFC 4515, Section 3</a>
     * @see <a href="https://tools.ietf.org/html/rfc4512#section-1.4">RFC 4512, Section 1.4</a>
     */
    public static Optional<String> commonNameOf(final String dn) throws X509NameException {
        var error = "";

        var d0 = 0;
        final var d1 = dn.length();
        char c;

        error:
        {
            while (d0 < d1) {
                c = dn.charAt(d0++);
                if (c != 'c' && c != 'C') {
                    continue;
                }
                c = dn.charAt(d0++);
                if (c != 'n' && c != 'N') {
                    continue;
                }
                do {
                    c = dn.charAt(d0++);
                } while (c == ' ');
                if (c != '=') {
                    continue;
                }
                break;
            }
            if (d0 == d1) {
                do {
                    d0 = dn.indexOf("2.5.4.3");
                    if (d0 == -1) {
                        return Optional.empty();
                    }
                    d0 += 7;
                    do {
                        c = dn.charAt(d0++);
                    } while (c == ' ');
                    if (c == '=') {
                        break;
                    }
                } while (d0 < d1);
            }
            if (d0 == d1) {
                return Optional.empty();
            }

            final var buffer = new ByteArrayOutputStream();
            c = dn.charAt(d0);
            if (c == '#') { // Hex string.
                d0 += 1;
                while (d0 < d1) {
                    var b = 0;
                    c = Character.toUpperCase(dn.charAt(d0++));
                    if (c >= '0' && c <= '9') {
                        b = ((c - '0') << 4);
                    }
                    else if (c >= 'A' && c <= 'F') {
                        b = ((c - 'A' + 10) << 4);
                    }
                    else {
                        error = ERROR_BAD_HEX_CHARACTER;
                        break error;
                    }
                    if (d0 == d1) {
                        error = ERROR_INCOMPLETE_HEX_PAIR;
                        break error;
                    }
                    c = Character.toUpperCase(dn.charAt(d0++));
                    if (c >= '0' && c <= '9') {
                        b |= (c - '0');
                    }
                    else if (c >= 'A' && c <= 'F') {
                        b |= (c - 'A' + 10);
                    }
                    else {
                        error = ERROR_BAD_HEX_CHARACTER;
                        break error;
                    }
                    buffer.write(b);
                }
            }
            else { // Normal string.
                string:
                while (d0 < d1) {
                    c = dn.charAt(d0++);
                    character:
                    switch (c) {
                    case '\0':
                    case '\"':
                    case ';':
                    case '<':
                    case '>':
                        error = ERROR_UNEXPECTED_CHARACTER;
                        break error;

                    case '+':
                    case ',':
                        break string;

                    case '\\':
                        if (d0 == d1) {
                            error = ERROR_TERMINATING_BACKSLASH;
                            break error;
                        }
                        c = dn.charAt(d0++);
                        switch (c) {
                        case ' ':
                        case '=':
                        case '\"':
                        case '+':
                        case ',':
                        case ';':
                        case '<':
                        case '>':
                            break character;
                        }
                        if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f') {
                            if (d0 == d1) {
                                error = ERROR_INCOMPLETE_HEX_PAIR;
                                break error;
                            }
                            var b = 0;
                            c = Character.toUpperCase(c);
                            if (c <= '9') {
                                b = ((c - '0') << 4);
                            }
                            else if (c <= 'F') {
                                b = ((c - 'A' + 10) << 4);
                            }
                            else {
                                error = ERROR_BAD_HEX_CHARACTER;
                                break error;
                            }
                            c = Character.toUpperCase(dn.charAt(d0++));
                            if (c >= '0' && c <= '9') {
                                b |= (c - '0');
                            }
                            else if (c >= 'A' && c <= 'F') {
                                b |= (c - 'A' + 10);
                            }
                            else {
                                error = ERROR_BAD_HEX_CHARACTER;
                                break error;
                            }
                            buffer.write(b);
                            continue string;
                        }
                        error = ERROR_BAD_ESCAPE;
                        break error;
                    }
                    Unicode.writeAsUtf8To(c, buffer);
                }
            }
            return Optional.of(buffer.toString(StandardCharsets.UTF_8).trim());
        }
        throw new X509NameException(error, dn, d0);
    }
}
