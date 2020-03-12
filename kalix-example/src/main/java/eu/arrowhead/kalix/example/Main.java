package eu.arrowhead.kalix.example;

import eu.arrowhead.kalix.core.plugins.sr.HttpServiceRegistry;
import eu.arrowhead.kalix.core.plugins.sr.dto.ServiceRecordFormBuilder;
import eu.arrowhead.kalix.core.plugins.sr.dto.SystemDefinitionFormBuilder;
import eu.arrowhead.kalix.descriptor.InterfaceDescriptor;
import eu.arrowhead.kalix.descriptor.SecurityDescriptor;
import eu.arrowhead.kalix.dto.DataEncoding;
import eu.arrowhead.kalix.net.http.client.HttpClient;
import eu.arrowhead.kalix.security.X509KeyStore;
import eu.arrowhead.kalix.security.X509TrustStore;

import java.net.InetSocketAddress;
import java.nio.file.Path;

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

            final var password = new char[]{'1', '2', '3', '4', '5', '6'};
            final var keyStore = new X509KeyStore.Loader()
                .keyPassword(password)
                .keyStorePath(keyStorePath)
                .keyStorePassword(password)
                .load();

            final var trustStore = X509TrustStore.read(trustStorePath, password);

            final var client = new HttpClient.Builder()
                .keyStore(keyStore)
                .trustStore(trustStore)
                .build();

            final var serviceRegistry = new HttpServiceRegistry(
                client,
                DataEncoding.JSON,
                new InetSocketAddress("172.23.2.13", 8446));

            serviceRegistry.register(new ServiceRecordFormBuilder()
                .serviceName("data_consumer-service10")
                .provider(new SystemDefinitionFormBuilder()
                    .name("data_consumer")
                    .hostname("127.0.0.1")
                    .port(13370)
                    .publicKeyBase64(keyStore.publicKeyBase64())
                    .build())
                .basePath("/example-x")
                .security(SecurityDescriptor.TOKEN)
                .version(1)
                .supportedInterfaces(InterfaceDescriptor.HTTP_SECURE_JSON)
                .build())
                .onResult(result -> {
                    result.ifSuccess(record -> System.out.println("Success. Record ID: " + record.id()));
                    result.ifFailure(Throwable::printStackTrace);
                    System.exit(0);
                });
        }
        catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
