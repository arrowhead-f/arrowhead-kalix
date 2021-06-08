/**
 * <h1>Arrowhead Kalix Basic Utilities</h1>
 * <h2>Arrowhead Basics</h2>
 * Arrowhead Framework defines three primary entities, which are (1) devices,
 * (2) systems and (3) services. A physical device, such as a server or IoT
 * device can host one or more systems, each of which can provide zero or more
 * services. A service could be thought of as a network API made available
 * through a well-defined set of {@link
 * se.arkalix.ServiceInterface network interface protocols}.
 * Systems can consume the services other systems provide, where consuming
 * concretely entails exchanging messages. More details about the Arrowhead
 * Framework can be read
 * <a href="https://arrowhead.eu/arrowheadframework/this-is-it">on the official
 * website</a>, while more information about Arrowhead Kalix is available on
 * <a href="https://arkalix.se">https://arkalix.se</a>.
 * <h2>About This Package</h2>
 * This package most significantly contains the {@link se.arkalix.ArSystem
 * ArSystem} class, which is what allows a Kalix application to host Arrowhead
 * systems. In addition, it significantly contains various interfaces that
 * represents the system's ability to provide and consume services.
 * <h2>Setting Up Kalix Logging</h2>
 * The Kalix libraries use <a href="http://slf4j.org">SLF4J</a> as logging
 * front-end, which means that a concrete logging framework has to be chosen
 * as <a href="http://slf4j.org/manual.html#projectDep">back-end</a> if wanting
 * logging messages to be generated. Example projects with logging set up can
 * be found on <a href="https://arkalix.se">https://arkalix.se</a>.
 */
package se.arkalix;