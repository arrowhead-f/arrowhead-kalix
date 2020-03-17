package eu.arrowhead.kalix.example;

import eu.arrowhead.kalix.AhfSystem;
import eu.arrowhead.kalix.core.plugins.srv.HttpServiceRegistrationPlugin;
import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.descriptor.SecurityDescriptor;
import eu.arrowhead.kalix.dto.DataEncoding;
import eu.arrowhead.kalix.net.http.HttpStatus;
import eu.arrowhead.kalix.net.http.service.HttpService;
import eu.arrowhead.kalix.security.X509KeyStore;
import eu.arrowhead.kalix.security.X509TrustStore;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Arrays;

public class Main {
    public static void main(final String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar example.jar -- <keyStorePath> <trustStorePath>");
            System.exit(1);
        }
        try {
            final var keyStorePath = Path.of(args[0]);
            final var trustStorePath = Path.of(args[1]);

            final var password = new char[]{'1', '2', '3', '4', '5', '6'};
            final var keyStore = new X509KeyStore.Loader()
                .keyPassword(password)
                .keyStorePath(keyStorePath)
                .keyStorePassword(password)
                .load();
            final var trustStore = X509TrustStore.read(trustStorePath, password);
            Arrays.fill(password, '0');

            final var system = new AhfSystem.Builder()
                .keyStore(keyStore)
                .trustStore(trustStore)
                .plugins(new HttpServiceRegistrationPlugin.Builder()
                    .encoding(DataEncoding.JSON)
                    .remoteSocketAddress(new InetSocketAddress("172.23.2.13", 8446))
                    .build())
                .build();

            system.provide(new HttpService()
                .name("kalix-example-service")
                .encodings(EncodingDescriptor.JSON)
                .security(SecurityDescriptor.TOKEN)
                .basePath("/example")
                .get("/ping", (request, response) -> {
                    response
                        .status(HttpStatus.OK)
                        .header("content-type", "application/json")
                        .body("{\"ping\":\"pong\"}");

                    return Future.done();
                })
                .post("/echoes", ((request, response) ->
                    request.bodyAsString()
                        .map(body -> {
                            response
                                .status(HttpStatus.OK)
                                .header("content-type", request.header("content-type").orElse("text/plain"))
                                .body(body);

                            return null;
                        })))
            );
        }
        catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
