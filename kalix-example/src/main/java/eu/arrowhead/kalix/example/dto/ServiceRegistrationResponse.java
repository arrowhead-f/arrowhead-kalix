package eu.arrowhead.kalix.example.dto;

import eu.arrowhead.kalix.dto.Readable;

import java.util.List;
import java.util.Map;

@Readable
public interface ServiceRegistrationResponse {
    int id();

    ServiceRegistrationResponseServiceDefinition serviceDefinition();

    ServiceRegistrationResponseProvider provider();

    String serviceUri();

    String endOfValidity();

    String secure();

    Map<String, String> metadata();

    int version();

    List<ServiceRegistrationResponseInterface> interfaces();

    String createdAt();

    String updatedAt();
}
