/**
 * <h1>Arrowhead Access Control</h1>
 * There are four access control paradigms currently supported by the Arrowhead
 * Framework, which are
 * <ol>
 *     <li>access by {@link se.arkalix.security.access.AccessByCertificate
 *         certificate},</li>
 *     <li>access by {@link se.arkalix.security.access.AccessByWhitelist
 *         white-list},</li>
 *     <li>access by {@link se.arkalix.security.access.AccessByToken token}
 *         and</li>
 *     <li>{@link se.arkalix.security.access.AccessUnrestricted unrestricted}
 *         access.</li>
 * </ol>
 * <p>
 * The three former paradigms are only supported for systems running in
 * {@link se.arkalix.security secure mode}, while the unrestricted paradigm
 * is the only supported paradigm in {@link se.arkalix.security insecure mode}.
 * <p>
 * Each Arrowhead system is expected to enforce its access policies when other
 * systems try to consume the services its provide, as well as trying to comply
 * with the access requirements of the services it tries to consume itself.
 * <p>
 * More details about the access control paradigms can be read in the classes
 * representing them.
 *
 * @see se.arkalix.security.access.AccessByCertificate AccessByCertificate
 * @see se.arkalix.security.access.AccessByToken AccessByToken
 * @see se.arkalix.security.access.AccessByWhitelist AccessByWhitelist
 * @see se.arkalix.security.access.AccessUnrestricted AccessUnrestricted
 */
package se.arkalix.security.access;