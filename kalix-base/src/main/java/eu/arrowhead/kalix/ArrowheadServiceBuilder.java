package eu.arrowhead.kalix;

/**
 * Represents a class useful for constructing {@link ArrowheadService}
 * instances.
 */
public interface ArrowheadServiceBuilder {
    /**
     * Completes construction of {@link ArrowheadService}.
     *
     * @return New {@link ArrowheadService}.
     * @throws NullPointerException     If any mandatory field was left
     *                                  unspecified.
     * @throws IllegalArgumentException If the service specification is
     *                                  invalid.
     * @throws IllegalStateException    If the service conflicts with an
     *                                  existing such, such as by not having a
     *                                  unique name or qualifier.
     */
    ArrowheadService build();
}
