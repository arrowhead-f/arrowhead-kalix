package se.arkalix.internal.security.identity;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

public class X509Certificates {
    private X509Certificates() {}

    public static X509Certificate[] castOrThrow(final Certificate[] certificates) {
        return castOrThrow(certificates, (index, certificate) -> new ClassCastException("" +
            "Failed to cast Certificate[] to X509Certificate[]; the " +
            "certificate at index " + index + " is of type " +
            certificate.getClass()));
    }

    public static Optional<X509Certificate[]> cast(final Certificate[] certificates) {
        Objects.requireNonNull(certificates);

        final var x509Certificates = new X509Certificate[certificates.length];
        for (var i = 0; i < certificates.length; ++i) {
            final var certificate = certificates[i];
            if (!(certificate instanceof X509Certificate)) {
                return Optional.empty();
            }
            x509Certificates[i] = (X509Certificate) certificates[i];
        }
        return Optional.of(x509Certificates);
    }

    public static <X extends Throwable> X509Certificate[] castOrThrow(
        final Certificate[] certificates,
        final BiFunction<Integer, Certificate, ? extends X> exceptionFunction
    ) throws X
    {
        Objects.requireNonNull(certificates);
        Objects.requireNonNull(exceptionFunction);

        final var x509Certificates = new X509Certificate[certificates.length];
        for (var i = 0; i < certificates.length; ++i) {
            final var certificate = certificates[i];
            if (!(certificate instanceof X509Certificate)) {
                throw exceptionFunction.apply(i, certificate);
            }
            x509Certificates[i] = (X509Certificate) certificates[i];
        }
        return x509Certificates;
    }
}