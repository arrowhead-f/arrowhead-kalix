package eu.arrowhead.kalix.example;

import eu.arrowhead.kalix.descriptor.InterfaceDescriptor;
import eu.arrowhead.kalix.example.dto.ServiceRegistrationRequestBuilder;
import eu.arrowhead.kalix.example.dto.ServiceRegistrationRequestProviderSystemBuilder;
import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.net.http.client.HttpClient;
import eu.arrowhead.kalix.net.http.client.HttpClientRequest;

import java.net.InetSocketAddress;

public class Main {
    public static void main(final String[] args) {
        try {
            System.out.println("Hello, Example!");

            final var client = new HttpClient.Builder()
                .insecure()
                .build();

            client.send(new InetSocketAddress("127.0.0.1", 8443), new HttpClientRequest()
                .method(HttpMethod.POST)
                .uri("/serviceregistry/register")
                .body(new ServiceRegistrationRequestBuilder()
                    .serviceDefinition("example-palm2-service")
                    .providerSystem(new ServiceRegistrationRequestProviderSystemBuilder()
                        .systemName("example-palm2")
                        .address("127.0.0.1")
                        .port(13370)
                        .authenticationInfo("xyz")
                        .build())
                    .serviceUri("http://127.0.0.1/example-palm2")
                    .secure("NOT_SECURE")
                    .version(10)
                    .interfaces(InterfaceDescriptor.valueOf("HTTP-INSECURE-JSON"))
                    .build()))
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
                });
        }
        catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
