package eu.arrowhead.kalix.security;

import java.util.Optional;

public class ArrowheadName {
    private final String system;
    private final String cloud;
    private final String company;
    private final String master;

    public ArrowheadName(final String system, final String cloud, final String company, final String master) {
        this.system = system;
        this.cloud = cloud;
        this.company = company;
        this.master = master;
    }

    public static ArrowheadName fromCommonName(final String cn) {
        throw new UnsupportedOperationException(); // TODO: Implement.
    }

    public Optional<String> system() {
        return Optional.ofNullable(system);
    }

    public Optional<String> cloud() {
        return Optional.ofNullable(cloud);
    }

    public String company() {
        return company;
    }

    public String master() {
        return master;
    }
}
