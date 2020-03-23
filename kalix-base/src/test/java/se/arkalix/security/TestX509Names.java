package se.arkalix.security;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.arkalix.internal.security.identity.x509.X509Names;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestX509Names {
    @ParameterizedTest
    @MethodSource("validDistinguishedNameProvider")
    void shouldExtractCommonNamesCorrectly(final String dn, final String expected) throws Exception {
        final var actual = X509Names.commonNameOf(dn);
        assertEquals(expected, actual.orElse(""));
    }

    static Stream<Arguments> validDistinguishedNameProvider() {
        return Stream.of(
            arguments("CN=system1.cloud1.company1.arrowhead.eu", "system1.cloud1.company1.arrowhead.eu"),
            arguments("cn=Example CA,dc=example,dc=com", "Example CA"),
            arguments("EMAIL=martin.smith@example.com+Cn=Martin K Smith+OU=HR", "Martin K Smith"),
            arguments("cN=M\\61rty Smith+T=Senior Software Engineer", "Marty Smith"),
            arguments("CN=printer12,OU=Basement", "printer12"),
            arguments("CN= printer9\\+8 +OU=Basement ", "printer9+8"),
            arguments("CN =#6162636E6fC3A5", "abcnoå"),
            arguments("CN = M\\c3\\A5ns", "Måns"),
            arguments("2.5.4.6=US,2.5.4.3=Hello", "Hello"),
            arguments("OU=2.5.4.3\\=+CN=x", "x"),
            arguments("OU=CN \\=,CN=y", "y")
        );
    }
}
