/**
 * <h1>Arrowhead Security</h1>
 * Each and every Arrowhead Framework system runs in either <i>secure</i> or
 * <i>insecure</i> mode. If in the <i>secure</i> mode, the system in question
 * is always associated with a {@link se.arkalix.security.identity
 * cryptographic identity}. That identity is used for three things, which are
 * <ol>
 *     <li>to establish <i>secure transports</i>,</li>
 *     <li>to determine {@link se.arkalix.security.identity system
 *         identities and relationships}, as well as</li>
 *     <li>to make {@link se.arkalix.security.access access control}
 *         decisions.</li>
 * </ol>
 * Systems running in <i>insecure</i> mode do not have any cryptographic
 * identities, which means that none of the activities listed above can be
 * supported. The <i>insecure</i> mode is primarily intended for development or
 * evaluation scenarios, but could also be useful in context where security is
 * not a requirement.
 * <p>
 * Whether or not systems are to run securely or not is typically decided on a
 * per-cloud basis, meaning that either all systems part of the same local
 * cloud run in secure mode, or none of them do. There is nothing, however,
 * preventing a single device from running a secure system that is part of a
 * local cloud from also running an insecure version of the same system.
 *
 * @see se.arkalix.security.access Arrowhead Access Control
 * @see se.arkalix.security.identity Arrowhead Identity Management
 */
package se.arkalix.security;