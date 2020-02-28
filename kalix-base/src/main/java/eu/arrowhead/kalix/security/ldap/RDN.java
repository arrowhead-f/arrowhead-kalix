package eu.arrowhead.kalix.security.ldap;

/**
 * @see <a href="https://tools.ietf.org/html/rfc4514">RFC 4514</a>
 */
public class RDN {

    /*
      relativeDistinguishedName = attributeTypeAndValue
          *( PLUS attributeTypeAndValue )
      attributeTypeAndValue = attributeType EQUALS attributeValue
     */
    public static RDN valueOf(final String string) {
        final var builder = new StringBuilder();

        final int s1 = string.length();
        int s0 = 0;
        int sx;

        error:
        {
            char c;
            while (s0 < s1) {
                String type;
                sx = s0;
                c = string.charAt(s0++);
                if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z') {

                }
                else if (c >= '1' && c <= '9') {
                    while (s0 < s1 && string.charAt(s0) == '.') {
                        s0 += 1;
                        c = string.charAt(s0++);
                        if (c < '0' || c > '9') {
                            break error;
                        }
                    }
                    type = string.substring(sx, s0);
                }
                else {
                    break error;
                }

                String value;
                sx = s0;
            }
            return null;
        }
        // TODO: Replace with better exception.
        throw new IllegalStateException("Unexpected character at offset: " + s0);
    }

    /*
      descr / numericoid
     */
    private static String attributeType(final String source, int offset) {
        return null;
    }

    /*
      descr = keystring
      keystring = leadkeychar *keychar
      leadkeychar = ALPHA
      keychar = ALPHA / DIGIT / HYPHEN
      number  = DIGIT / ( LDIGIT 1*DIGIT )

      ALPHA   = %x41-5A / %x61-7A   ; "A"-"Z" / "a"-"z"
      DIGIT   = %x30 / LDIGIT       ; "0"-"9"
      LDIGIT  = %x31-39             ; "1"-"9"
      HEX     = DIGIT / %x41-46 / %x61-66 ; "0"-"9" / "A"-"F" / "a"-"f"
     */
    private static String descr(final String source, int offset) {
        return null;
    }

    /*
      numericoid = number 1*( DOT number )
     */
    private static String numericoid(final String source, int offset) {
        return null;
    }

    /*
       string / hexstring
     */
    private static String attributeValue(final String source, int offset) {
        return null;
    }

    /*
      ; The following characters are to be escaped when they appear
      ; in the value to be encoded: ESC, one of <escaped>, leading
      ; SHARP or SPACE, trailing SPACE, and NULL.
      string =   [ ( leadchar / pair ) [ *( stringchar / pair )
         ( trailchar / pair ) ] ]

      leadchar = LUTF1 / UTFMB
      LUTF1 = %x01-1F / %x21 / %x24-2A / %x2D-3A /
         %x3D / %x3F-5B / %x5D-7F

      trailchar  = TUTF1 / UTFMB
      TUTF1 = %x01-1F / %x21 / %x23-2A / %x2D-3A /
         %x3D / %x3F-5B / %x5D-7F

      stringchar = SUTF1 / UTFMB
      SUTF1 = %x01-21 / %x23-2A / %x2D-3A /
         %x3D / %x3F-5B / %x5D-7F

      pair = ESC ( ESC / special / hexpair )
      special = escaped / SPACE / SHARP / EQUALS
      escaped = DQUOTE / PLUS / COMMA / SEMI / LANGLE / RANGLE
      hexpair = HEX HEX
     */
    private static String string(final String source, int offset) {
        return null;
    }

    /*
      hexstring = "#" 1*hexpair
      hexpair = HEX HEX
     */
    private static String hexString(final String source, int offset) {
        return null;
    }
}
