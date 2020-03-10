package eu.arrowhead.kalix.example;

import eu.arrowhead.kalix.descriptor.InterfaceDescriptor;
import eu.arrowhead.kalix.example.dto.ErrorResponseData;
import eu.arrowhead.kalix.example.dto.ServiceRegistrationRequestBuilder;
import eu.arrowhead.kalix.example.dto.ServiceRegistrationRequestProviderSystemBuilder;
import eu.arrowhead.kalix.example.dto.ServiceRegistrationResponseData;
import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.net.http.client.HttpClientFactory;
import eu.arrowhead.kalix.net.http.client.HttpClientRequest;

import java.net.InetSocketAddress;

import static eu.arrowhead.kalix.descriptor.EncodingDescriptor.JSON;

public class Main {
    public static void main(final String[] args) {
        try {
            System.out.println("Hello, Example!");

            final var clientFactory = new HttpClientFactory.Builder()
                .insecure()
                .build();

            clientFactory.create(JSON, new InetSocketAddress("127.0.0.1", 8443))
                .onResult(result0 -> {
                    if (!result0.isSuccess()) {
                        result0.fault().printStackTrace();
                        return;
                    }
                    final var client = result0.value();
                    client.send(new HttpClientRequest()
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
                            client.close();
                        });
                });

        }
        catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
