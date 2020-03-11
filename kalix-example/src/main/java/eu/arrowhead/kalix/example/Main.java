package eu.arrowhead.kalix.example;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.descriptor.InterfaceDescriptor;
import eu.arrowhead.kalix.example.dto.ServiceRegistrationRequestBuilder;
import eu.arrowhead.kalix.example.dto.ServiceRegistrationRequestProviderSystemBuilder;
import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.net.http.client.HttpClient;
import eu.arrowhead.kalix.net.http.client.HttpClientRequest;
import eu.arrowhead.kalix.security.X509KeyStore;
import eu.arrowhead.kalix.security.X509TrustStore;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Base64;

public class Main {
    public static void main(final String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar example.jar -- <keyStorePath> <trustStorePath>");
            System.exit(1);
        }
        try {
            System.out.println("Hello, Example!");

            final var keyStorePath = Path.of(args[0]);
            final var trustStorePath = Path.of(args[1]);

            final var keyStore = new X509KeyStore.Loader()
                .keyPassword(new char[]{'1', '2', '3', '4', '5', '6'})
                .keyStorePath(keyStorePath)
                .keyStorePassword(new char[]{'1', '2', '3', '4', '5', '6'})
                .load();

            final var trustStore = X509TrustStore.read(trustStorePath, new char[]{'1', '2', '3', '4', '5', '6'});

            final var client = new HttpClient.Builder()
                .encodings(EncodingDescriptor.JSON)
                .keyStore(keyStore)
                .trustStore(trustStore)
                .build();

            client.connect(new InetSocketAddress("172.23.2.13", 8446))
                .flatMap(connection -> {
                    System.out.println(connection.certificate());
                    return connection.sendAndClose(new HttpClientRequest()
                        .method(HttpMethod.POST)
                        .uri("/serviceregistry/register")
                        .body(new ServiceRegistrationRequestBuilder()
                            .serviceDefinition("data_consumer-service3")
                            .providerSystem(new ServiceRegistrationRequestProviderSystemBuilder()
                                .systemName("data_consumer")
                                .address("127.0.0.1")
                                .port(13370)
                                .authenticationInfo(keyStore.publicKeyAsString())
                                .build())
                            .serviceUri("http://127.0.0.1/example-palm3")
                            .secure("CERTIFICATE")
                            .version(10)
                            .interfaces(InterfaceDescriptor.HTTP_SECURE_JSON)
                            .build()));
                })
                .flatMap(response -> {
                    System.out.println(response.status());
                    return response.bodyAsString();
                })
                .onResult(result1 -> {
                    if (result1.isSuccess()) {
                        System.out.println(result1.value());
                    }
                    else {
                        result1.fault().printStackTrace();
                    }
                    System.exit(0);
                });
        }
        catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
