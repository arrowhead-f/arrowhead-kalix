package eu.arrowhead.kalix.security;

import eu.arrowhead.kalix.internal.charset.Unicode;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class DistinguishedName {
    private DistinguishedName() {}

    public static String commonNameOf(final String dn) throws DistinguishedNameException {
        var error = "";

        error:
        {
            var d0 = 0;
            final var d1 = dn.length();
            char c;
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
                d0 = dn.indexOf("2.5.4.3");
                if (d0 == -1) {
                    error = "No CN";
                    break error;
                }
                d0 += 7;
                do {
                    c = dn.charAt(d0++);
                } while (c == ' ');
                if (c != '=') {
                    error = "No CN attribute value";
                    break error;
                }
            }

            final var buffer = new ByteArrayOutputStream();
            c = dn.charAt(d0);
            if (c == '#') { // Hex string.
                hexError:
                {
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
                            break hexError;
                        }
                        if (d0 == d1) {
                            break hexError;
                        }
                        c = Character.toUpperCase(dn.charAt(d0++));
                        if (c >= '0' && c <= '9') {
                            b |= (c - '0');
                        }
                        else if (c >= 'A' && c <= 'F') {
                            b |= (c - 'A' + 10);
                        }
                        else {
                            break hexError;
                        }
                        buffer.write(b);
                    }
                    return buffer.toString(StandardCharsets.UTF_8).trim();
                }
                error = "Invalid CN; bad attribute value of type hex string";
            }
            else { // Normal string.
                stringError:
                {
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
                            break stringError;

                        case '+':
                        case ',':
                            break string;

                        case '\\':
                            if (d0 == d1) {
                                break stringError;
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
                                    break stringError;
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
                                    break stringError;
                                }
                                c = Character.toUpperCase(dn.charAt(d0++));
                                if (c >= '0' && c <= '9') {
                                    b |= (c - '0');
                                }
                                else if (c >= 'A' && c <= 'F') {
                                    b |= (c - 'A' + 10);
                                }
                                else {
                                    break stringError;
                                }
                                buffer.write(b);
                                continue string;
                            }
                            break stringError;
                        }
                        Unicode.writeAsUtf8To(c, buffer);
                    }
                    return buffer.toString(StandardCharsets.UTF_8).trim();
                }
                error = "Invalid CN; bad attribute value of type string";
            }
        }
        throw new DistinguishedNameException(error);
    }
}
