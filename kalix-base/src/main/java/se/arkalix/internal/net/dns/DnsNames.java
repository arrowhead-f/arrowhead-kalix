package se.arkalix.internal.net.dns;

public class DnsNames {
    private DnsNames() {}

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
    public static boolean isValid(final String name) {
        final var n1 = name.length();

        label:
        for (var n0 = 0; n0 < n1; ++n0) {

            var c = name.charAt(n0); // Only alpha characters may start a label.
            if (!(c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')) {
                return false;
            }

            do {
                n0 += 1;
                if (n0 == n1) {
                    return true;
                }
                c = name.charAt(n0);

                if (c == '-') { // A hyphen must never end a label.
                    final var nx = n0 + 1;
                    if (nx == n1) {
                        return false;
                    }
                    c = name.charAt(nx);
                    if (c == '.') {
                        return false;
                    }
                    continue;
                }
                else if (c == '.') { // A dot must never end a name.
                    if (n0 + 1 == n1) {
                        return false;
                    }
                    continue label; // End of label, start over.
                }
                if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')) {
                    return false;
                }
            } while (n0 < n1);
        }
        return true;
    }
}
