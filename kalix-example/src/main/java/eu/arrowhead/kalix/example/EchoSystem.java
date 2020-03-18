package eu.arrowhead.kalix.example;

import eu.arrowhead.kalix.AhfSystem;
import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.descriptor.SecurityDescriptor;
import eu.arrowhead.kalix.net.http.HttpStatus;
import eu.arrowhead.kalix.net.http.service.HttpService;
import eu.arrowhead.kalix.security.X509KeyStore;
import eu.arrowhead.kalix.security.X509TrustStore;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

public class EchoSystem {
    public static void main(final String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar example.jar <keyStorePath> <trustStorePath>");
            System.exit(1);
        }
        try {
            // Load keystore and truststore.
            // The key store represents the systems own identity, while the
            // trust store represents all identities that are to be trusted.
            final var password = new char[]{'1', '2', '3', '4', '5', '6'};
            final var keyStore = new X509KeyStore.Loader()
                .keyPassword(password)
                .keyStorePath(Path.of(args[0]))
                .keyStorePassword(password)
                .load();
            final var trustStore = X509TrustStore.read(Path.of(args[1]), password);

            // Create Arrowhead system.
            final var system = new AhfSystem.Builder()
                .keyStore(keyStore)
                .trustStore(trustStore)
                .localPort(28080)

                // This plugin registers all services provided by the system
                // with the HTTP-SECURE-JSON service registry at the specified
                // IP address.
                //.plugins(new HttpServiceRegistrationPlugin.Builder()
                //    .encoding(DataEncoding.JSON)
                //    .remoteSocketAddress(new InetSocketAddress("localhost", 8446))
                //    .build())

                .build();

            // Cause the Arrowhead system to provide an HTTP service.
            system.provide(new HttpService()

                // Mandatory service configuration details.
                .name("kalix-example-service")
                .encodings(EncodingDescriptor.JSON)
                .security(SecurityDescriptor.TOKEN)
                .basePath("/example")

                // HTTP GET endpoint that uses a DTO class, "Ping", which is
                // defined in this folder. This particular endpoint ignores the
                // body of the request, if any.
                .get("/pings/#id", (request, response) -> {
                    response
                        .status(HttpStatus.OK)
                        .body(new PingBuilder()
                            .ping("pong")
                            // #id is the first path parameter and has index 0.
                            .id(request.pathParameter(0))
                            .timestamp(Instant.now())
                            .build());

                    return Future.done();
                })

                // HTTP POST endpoint that accepts a "PingData" object and
                // returns it to its sender.
                .post("/pings", (request, response) ->
                    request.bodyAs(PingData.class)
                        .map(body -> response
                            .status(HttpStatus.CREATED)
                            .body(body)))

                // HTTP POST endpoint that echoes back whatever body is in the
                // requests it receives. Note that since EncodingDescriptor.JSON
                // was specified via the ".encodings()" method above, only
                // requests that claims to carry JSON bodies, or have no bodies
                // at all, are accepted and reach the endpoints specified here.
                .post("/echoes", (request, response) ->
                    request.bodyAsString()
                        .map(body -> response
                            .status(HttpStatus.OK)
                            .header("content-type", request.header("content-type")
                                .orElse("application/json"))
                            .body(body)))

                // HTTP DELETE endpoint that causes the application to exit.
                .delete("/runtime", (request, response) -> {
                    response.status(HttpStatus.NO_CONTENT);

                    // Shutting down the system will not terminate the
                    // application as it would be kept alive by its scheduler,
                    // which is not shut down automatically. If, instead,
                    // shutting down the scheduler directly, the system, and
                    // all other systems that would be using the scheduler, are
                    // automatically shut down.
                    system.scheduler().shutdown(Duration.ofSeconds(1))
                        .onFailure(Throwable::printStackTrace);

                    return Future.done();
                })
            ).onFailure(Throwable::printStackTrace);

            System.out.println("Echo system running ...");
        }
        catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
