/**
 * <h1>Arrowhead Identity Management</h1>
 * There are five categories of identities with relevance to Arrowhead
 * Framework. Those are (1) systems, (2) operators, (3) clouds, (4) companies
 * and (5) Internet authorities. Out of these, only system identities are
 * always associated with computer applications. Operator identities, which are
 * really just special cases of system identities, are to be owned by machines
 * that only act as directed by human operators. The remaining three categories
 * serve as hierarchical groupings that allow systems to tell how they relate
 * to other systems, for example in the context of service {@link
 * se.arkalix.security.access.AccessPolicy access control}.
 *
 * <h2>Certificates</h2>
 * In order for a given system or operator to be able to reliably determine the
 * identity of some other system, all Arrowhead Framework systems <i>must</i>
 * be able to provide a valid <a href="https://tools.ietf.org/html/rfc5280">
 * x.509 certificate chain</a>, unless the systems in question are all running
 * in {@link se.arkalix.security insecure mode}. Certificates must be issued
 * according to the following hierarchy:
 * <ol>
 *     <li>SYSTEM / OPERATOR</li>
 *     <li>CLOUD</li>
 *     <li>COMPANY (optional)</li>
 *     <li>MASTER (optional)</li>
 *     <li>INTERNET (optional)</li>
 * </ol>
 * <h3>Cloud Certificates</h3>
 * Every system and operator certificate <i>must</i> be signed by a cloud
 * certificate (i.e. the issuer of every system and operator certificate is a
 * cloud certificate). All systems and operators with certificates issued by
 * the same cloud certificate are to be considered as members of the same
 * {@link se.arkalix local cloud}.
 *
 * <h3>Company Certificates</h3>
 * Cloud certificates <i>may</i>, in turn, be signed by company, master or
 * Internet certificates. If a cloud certificate is signed by a company
 * certificate, systems signed by the cloud certificate become able to reliably
 * tell if other systems are part of the same company, even if not part of the
 * same local cloud.
 *
 * <h3>Master Certificates</h3>
 * Company certificates <i>may</i> be signed by master or Internet
 * certificates. A master certificate places a local cloud or company in a
 * group of companies that potentially are cooperating or may want to cooperate
 * in the future. All systems that include the same master certificate in their
 * issuance chains can reliably tell whether other systems and operators belong
 * to a potentially trusted company.
 *
 * <h3>Internet Certificates</h3>
 * Internet certificates are intended to make system certificates useful also
 * when interacting with legacy systems or non-Arrowhead systems that also use
 * x.509 certificates. An Internet certificate can, typically, trace its
 * issuance chain back to the owner of a top-level DNS domain name, such as .eu
 * or .com.
 *
 * <h2>Certificate Naming Schema</h2>
 * The Arrowhead Framework mandates that a particular naming schema be used for
 * the <a href="https://tools.ietf.org/html/rfc4519#section-2.3">Common Names
 * (CNs)</a> part of the subject
 * <a href="https://tools.ietf.org/html/rfc5280#section-4.1.2.6">
 * Distinguished Names (DNs)</a> of each x.509 certificate representing a
 * system, operator, cloud, company or master. Exactly one CN must occur in
 * each subject DN. The naming schema is intended to reflect the above
 * certificate hierarchy and looks as follows:
 * <pre>
 *     {system}.{cloud}.{company}.{master}.{internet...}
 *     |_________________________________| |___________|
 *                      |                        |
 *                  MANDATORY                OPTIONAL
 * </pre>
 * Each named region enclosed in curly braces ({}) represents a
 * <a href="https://tools.ietf.org/html/rfc1035#section-2.3.1">DNS name
 * label</a>, except for the last which may include any number of labels. The
 * full CN <i>must</i> be a valid DNS name. The first three labels name a
 * system, cloud and company, respectively. All remaining labels are considered
 * to be the name of the master. The following is a concrete example of a
 * valid x.509 subject CN:
 * <pre>
 *     system-14.cloud-1.the-company.arrowhead.eu
 *     |_______| |_____| |_________| |__________|
 *         |        |         |           |
 *       SYSTEM   CLOUD    COMPANY      MASTER
 * </pre>
 * The CN of the <i>cloud-1</i> certificate, which issued the certificate named
 * in the above example, would be "cloud-1.the-company.arrowhead.eu". The CN
 * of the company certificate, if any, would be "the-company.arrowhead.eu".
 * Finally, the CN of the master, if any, would be "arrowhead.eu".
 * <p>
 * As it is not always relevant to use the full CNs when referring to systems,
 * clouds, and so on, these components are also referred to by their so-called
 * <i>names</i>. The name of the cloud in the above example would simply be
 * "cloud-1", while the name of the system would be "system-14". The name of
 * any master certificate is always identical to its CN.
 *
 * <h2>Owned and Trusted Identities</h2>
 * Each Arrowhead system running in {@link se.arkalix.security secure mode} is
 * said to <i>own</i> an identity, and to <i>trust</i> certain other
 * identities. Each secure Kalix {@link se.arkalix.ArSystem system} must, as a
 * consequence, have an {@link se.arkalix.security.identity.OwnedIdentity
 * OwnedIdentity}, which is typically loaded from a
 * <a href="https://tools.ietf.org/html/rfc7292">PKCS#12</a> key store file, as
 * well as a set of trusted identities, which are represented by a {@link
 * se.arkalix.security.identity.TrustStore TrustStore} instance also loaded
 * from a <a href="https://tools.ietf.org/html/rfc7292">PKCS#12</a> key store
 * file.
 * <p>
 * Not all identities trusted by a secure system originate from its {@link
 * se.arkalix.security.identity.TrustStore TrustStore}, however. The trust
 * store may concretely contains cloud, company, master and Internet
 * certificates. It does typically not contain any certificates of specific
 * systems or operators. The purpose of the trust store is, primarily, to help
 * systems with establishing whether or not other systems or operators they
 * contact have identities endorsed, or <i>issued</i>, by trusted issuers. It
 * isn't until a system is contacted for the first time that its certificate is
 * retrieved.
 * <p>
 * Before a certificate is known to belong to a specific system, but after that
 * it has been established that it is endorsed by a trusted issuer, it is
 * represented by a {@link se.arkalix.security.identity.TrustedIdentity
 * TrustedIdentity} instance, which can be {@link
 * se.arkalix.security.identity.SystemIdentity#SystemIdentity(se.arkalix.security.identity.TrustedIdentity)
 * promoted} to a so-called {@link se.arkalix.security.identity.SystemIdentity}
 * if it must be established that it represents a valid system identity.
 *
 * @see <a href="https://tools.ietf.org/html/rfc1035#section-2.3.1">RFC 1035, Section 2.3.1</a>
 * @see <a href="https://tools.ietf.org/html/rfc4512#section-1.4">RFC 4512, Section 1.4</a>
 * @see <a href="https://tools.ietf.org/html/rfc4514#section-3">RFC 4515, Section 3</a>
 * @see <a href="https://tools.ietf.org/html/rfc4519#section-2.3">RFC 4519, Section 2.3</a>
 * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
 * @see <a href="https://tools.ietf.org/html/rfc7292">RFC 7292</a>
 */
package se.arkalix.security.identity;