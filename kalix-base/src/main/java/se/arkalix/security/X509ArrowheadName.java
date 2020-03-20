package se.arkalix.security;

import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Describes an Arrowhead system, operator, cloud or company certificate.
 * <p>
 * As the name of this class implies, the data it contains are assumed to
 * originate from x.509 certificates used to represent Arrowhead entities.
 * <p>
 * An x.509 Arrowhead name describes a certificate owned by either an Arrowhead
 * system, operator, cloud or company, depending on which of these fields are
 * present in the descriptor. If only a company and master name is part of the
 * descriptor, it describes a company certificate, as follows:
 * <pre>
 *     company-inc.arrowhead.eu
 *     |_________| |__________|
 *          |           |
 *       Company      Master
 * </pre>
 * If a cloud name, company name and master name is present, it describes a
 * cloud, as follows:
 * <pre>
 *     cloud24.company-inc.arrowhead.eu
 *     |_____| |_________| |__________|
 *        |         |           |
 *      Cloud    Company      Master
 * </pre>
 * And, lastly, if all names are present, it describes either a system or an
 * operator, as follows:
 * <pre>
 *     system3.cloud24.company-inc.arrowhead.eu
 *     |_____| |_____| |_________| |__________|
 *        |       |         |           |
 *     System   Cloud    Company      Master
 * </pre>
 *
 * @see X509Certificates#subjectArrowheadNameOf(X509Certificate)
 */
public class X509ArrowheadName {
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^(?:([^.\\s]+)\\.)?(?:([^.\\s]+)\\.)?([^.\\s]+)\\.arrowhead\\.eu$"
    );

    private final String system;
    private final String cloud;
    private final String company;

    private String cachedStringRepresentation;

    public X509ArrowheadName(final String system, final String cloud, final String company) {
        if (cloud == null && system != null) {
            throw new IllegalArgumentException("If system is provided, cloud must not be null");
        }
        this.system = system;
        this.cloud = cloud;
        this.company = Objects.requireNonNull(company);
    }

    private X509ArrowheadName(final String system, final String cloud, final String company, final String name) {
        this.system = system;
        this.cloud = cloud;
        this.company = company;

        cachedStringRepresentation = name;
    }

    /**
     * Attempts to produce certificate descriptor form provided name.
     * <p>
     * The master part of the name must be {@code "arrowhead.eu"}, as it
     * otherwise would be impossible to tell where the master part begins. The
     * reason for this is that the parts of a certificate descriptor are
     * separated by dots, and the only way of telling them apart is by counting
     * the dots. As the master part is allowed to contain any number of dots,
     * the number of dots in the master part must be known in order for the
     * other parts to be identifiable.
     *
     * @param name Certificate LDAP DN to parse.
     * @return Certificate descriptor.
     * @throws IllegalArgumentException If provided {@code name} is not a valid
     *                                  certificate descriptor.
     */
    public static X509ArrowheadName valueOf(final String name) {
        final var matcher = NAME_PATTERN.matcher(name);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid Arrowhead certificate descriptor \"" +
                name + "\"; must match \"" + NAME_PATTERN + "\"");
        }
        var group1 = matcher.group(1);
        var group2 = matcher.group(2);
        if (group2 == null) {
            group2 = group1;
            group1 = null;
        }
        return new X509ArrowheadName(group1, group2, matcher.group(3), name);
    }

    /**
     * @return Name of system represented by the described certificate, if any.
     */
    public Optional<String> system() {
        return Optional.ofNullable(system);
    }

    /**
     * @return Name of cloud related to or represented by the described
     * certificate, if any.
     */
    public Optional<String> cloud() {
        return Optional.ofNullable(cloud);
    }

    /**
     * @return Name of company related to or represented by the described
     * certificate, if any.
     */
    public String company() {
        return company;
    }

    /**
     * @return Name of master certificate.
     */
    public String master() {
        return "arrowhead.eu";
    }

    @Override
    public String toString() {
        if (cachedStringRepresentation == null) {
            cachedStringRepresentation = (system != null ? system + "." : "") +
                (cloud != null ? cloud + "." : "") + company + ".arrowhead.eu";
        }
        return cachedStringRepresentation;
    }
}
