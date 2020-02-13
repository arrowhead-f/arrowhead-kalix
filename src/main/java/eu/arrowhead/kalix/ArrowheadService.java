package eu.arrowhead.kalix;

/**
 * An abstract Arrowhead Framework (AHF) service.
 */
public abstract class ArrowheadService {
    private final String name;

    protected ArrowheadService(final String name) {
        this.name = name;
    }

    /**
     * @return The name, also known as the <i>service definition</i>, of this
     * service.
     */
    public String getName() {
        return name;
    }
}
