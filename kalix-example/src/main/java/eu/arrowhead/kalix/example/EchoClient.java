package eu.arrowhead.kalix.example;

import eu.arrowhead.kalix.dto.DataEncoding;
import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.net.http.client.HttpClient;
import eu.arrowhead.kalix.net.http.client.HttpClientRequest;
import eu.arrowhead.kalix.security.X509KeyStore;
import eu.arrowhead.kalix.security.X509TrustStore;
import eu.arrowhead.kalix.util.concurrent.FutureScheduler;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.time.Duration;

public class EchoClient {
    public static void main(final String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar example.jar <keyStorePath> <trustStorePath>");
            System.exit(1);
        }
        try {
            System.out.println("Running echo client ...");

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

            // Create Arrowhead client.
            final var client = new HttpClient.Builder()
                .keyStore(keyStore)
                .trustStore(trustStore)
                .build();

            final var echoSystemSocketAddress = new InetSocketAddress("localhost", 28080);

            // HTTP GET request. Callback hell.
            client.send(echoSystemSocketAddress, new HttpClientRequest()
                .method(HttpMethod.GET)
                .uri("/example/pings/32")
                .header("accept", "application/json"))
                .onResult(result -> {
                    if (result.isSuccess()) {
                        final var response = result.value();
                        if (response.status().isSuccess()) {
                            response
                                .bodyAsString()
                                .onResult(result1 -> {
                                    if (result1.isSuccess()) {
                                        System.err.println("\nGET /example/pings/32 result:");
                                        System.err.println(response.status());
                                        System.err.println(result1.value());
                                    }
                                    else {
                                        System.err.println("\nGET /example/pings/32 failed:");
                                        result1.fault().printStackTrace();
                                    }
                                });
                        }
                        else {
                            System.err.println("\nGET /example/pings/32 failed:");
                            System.err.println(response.status());
                        }
                    }
                    else {
                        System.err.println("\nGET /example/pings/32 failed:");
                        result.fault().printStackTrace();
                    }
                });

            // HTTP POST request. Function composition.
            client.send(echoSystemSocketAddress, new HttpClientRequest()
                .method(HttpMethod.POST)
                .uri("/example/pings")
                .body(DataEncoding.JSON, new PingBuilder()
                    .ping("pong!")
                    .build()))
                .flatMap(response -> response.bodyAsClassIfSuccess(DataEncoding.JSON, PingData.class))
                .map(body -> {
                    System.err.println("\nPOST /example/pings result:");
                    System.err.println(body.asString());
                    return null;
                })
                .onFailure(throwable -> {
                    System.err.println("\nPOST /example/pings failure:");
                    throwable.printStackTrace();
                });

            // HTTP DELETE request.
            client.send(echoSystemSocketAddress, new HttpClientRequest()
                .method(HttpMethod.DELETE)
                .uri("/example/runtime"))
                .onResult(result -> {
                    System.err.println("\nDELETE /example/runtime result:");
                    result.ifSuccess(response -> System.err.println(response.status()));
                    result.ifFailure(Throwable::printStackTrace);

                    // Exit in 0.5 seconds.
                    FutureScheduler.getDefault()
                        .scheduleAfter(() -> System.exit(0), Duration.ofMillis(500))
                        .onFailure(Throwable::printStackTrace);
                });
        }
        catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
