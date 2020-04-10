/**
 * <h1>Arrowhead Kalix Basic Utilities</h1>
 * <p>
 * Arrowhead Framework defines three primary entities, which are (1) devices,
 * (2) systems and (3) services. A physical device, such as a server or IoT
 * device can host one or more systems, each of which can provide zero or more
 * services. Systems can, in turn, consume the services other systems provide.
 * <p>
 * A service could be thought of as a network API made available through a
 * concrete set of {@link se.arkalix.descriptor.InterfaceDescriptor network
 * interfaces}. Consuming a service concretely entails sending messages to it.
 * <p>
 * This package most significantly contains the {@link se.arkalix.ArSystem
 * ArSystem} class, which is what allows a Kalix application to host Arrowhead
 * systems. In addition, it significantly contains various interfaces that
 * represents the system's ability to provide and consume services.
 */
package se.arkalix;