package se.arkalix.internal.net.dns;

import se.arkalix.util.annotation.Internal;

/**
 * Utilities for working with DNS domain names.
 *
 * @see <a href="https://tools.ietf.org/html/rfc1035">RFC 1035</a>
 */
@Internal
public class DnsNames {
    private DnsNames() {}

    /**
     * Extracts first DNS domain name label from given DNS domain {@code name}.
     *
     * @param name Name to get first label from.
     * @return {@code true} only if {@code name} is a valid RFC 1035 domain
     * name label.
     * @throws IllegalArgumentException If the first available label is not
     *                                  valid as defined by RFC 1035, Section
     *                                  2.3.1.
     * @see <a href="https://tools.ietf.org/html/rfc1035#section-2.3.1">RFC 1035, Section 2.3.1</a>
     */
    public static String firstLabelOf(final String name) {
        var n0 = 0;
        final var n1 = name.length();
        char c;

        badEnd:
        {
            c = name.charAt(n0++);
            if (!(c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')) {
                throw new IllegalArgumentException("Invalid DNS label start " +
                    "character '" + c + "'; expected A-Z a-z");
            }
            while (n0 < n1) {
                c = name.charAt(n0++);
                if (c == '-') {
                    if (n0 == n1 || n0 + 1 == n1 && name.charAt(n0) == '.') {
                        break badEnd;
                    }
                    continue;
                }
                else if (c == '.') {
                    if (n0 == n1) {
                        break badEnd;
                    }
                    return name.substring(0, n0 - 1);
                }
                if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')) {
                    throw new IllegalArgumentException("Invalid DNS label " +
                        "character '" + c + "'; expected 0-9 A-Z a-z . -");
                }
            }
            return name;
        }
        throw new IllegalArgumentException("Invalid DNS label " +
            "end character '" + c + "'; expected 0-9 A-Z a-z");
    }

    /**
     * Determines whether or not the given {@code label} is a valid DNS domain
     * name label.
     * <p>
     * More formally, for {@code true} to be returned, {@code label} must
     * satisfy the following ABNF grammar:
     * <pre>
     * &lt;label&gt;       ::= &lt;letter&gt; [ [ &lt;ldh-str&gt; ] &lt;let-dig&gt; ]
     * &lt;ldh-str&gt;     ::= &lt;let-dig-hyp&gt; | &lt;let-dig-hyp&gt; &lt;ldh-str&gt;
     * &lt;let-dig-hyp&gt; ::= &lt;let-dig&gt; | "-"
     * &lt;let-dig&gt;     ::= &lt;letter&gt; | &lt;digit&gt;
     * </pre>
     *
     * @param label Label to test.
     * @return {@code true} only if {@code name} is a valid RFC 1035 domain
     * name label.
     * @see <a href="https://tools.ietf.org/html/rfc1035#section-2.3.1">RFC 1035, Section 2.3.1</a>
     */
    public static boolean isLabel(final String label) {
        var l0 = 0;
        final var l1 = label.length();
        char c;

        c = label.charAt(l0++);
        if (!(c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')) {
            return false; // May not start with digit or dash.
        }
        while (l0 < l1) {
            c = label.charAt(l0++);
            if (c == '-') {
                if (l0 == l1) {
                    return false; // May not end with dash.
                }
                continue;
            }
            if (c >= '0' && c <= '9' || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z') {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * Determines whether or not the given {@code name} is a valid DNS domain
     * name.
     * <p>
     * More formally, for {@code true} to be returned, {@code name} must
     * satisfy the following ABNF grammar:
     * <pre>
     * &lt;domain&gt;      ::= &lt;subdomain&gt; | " "
     * &lt;subdomain&gt;   ::= &lt;label&gt; | &lt;subdomain&gt; "." &lt;label&gt;
     * &lt;label&gt;       ::= &lt;letter&gt; [ [ &lt;ldh-str&gt; ] &lt;let-dig&gt; ]
     * &lt;ldh-str&gt;     ::= &lt;let-dig-hyp&gt; | &lt;let-dig-hyp&gt; &lt;ldh-str&gt;
     * &lt;let-dig-hyp&gt; ::= &lt;let-dig&gt; | "-"
     * &lt;let-dig&gt;     ::= &lt;letter&gt; | &lt;digit&gt;
     * </pre>
     *
     * @param name Name to test.
     * @return {@code true} only if {@code name} is a valid RFC 1035 domain
     * name.
     * @see <a href="https://tools.ietf.org/html/rfc1035#section-2.3.1">RFC 1035, Section 2.3.1</a>
     */
    public static boolean isName(final String name) {
        var n0 = 0;
        final var n1 = name.length();

        label:
        while (n0 < n1) {
            var c = name.charAt(n0++); // Only alpha characters may start a label.
            if (!(c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')) {
                return false;
            }
            while (n0 < n1) {
                c = name.charAt(n0++);

                if (c == '-') { // A hyphen must never end a label.
                    if (n0 == n1) {
                        return false;
                    }
                    c = name.charAt(n0);
                    if (c == '.') {
                        return false;
                    }
                    continue;
                }
                else if (c == '.') { // A dot must never end a name.
                    if (n0 == n1) {
                        return false;
                    }
                    continue label; // End of label, start over.
                }
                if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')) {
                    return false;
                }
            }
        }
        return true;
    }
}
