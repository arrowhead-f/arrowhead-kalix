package eu.arrowhead.kalix.descriptor;

import java.util.Optional;

public class NameDescriptor {
    private final String system;
    private final String cloud;
    private final String company;
    private final String master;

    public NameDescriptor(final String system, final String cloud, final String company, final String master) {
        this.system = system;
        this.cloud = cloud;
        this.company = company;
        this.master = master;
    }

    public static NameDescriptor fromCommonName(final String cn) {
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
