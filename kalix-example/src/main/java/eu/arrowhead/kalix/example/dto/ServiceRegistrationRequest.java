package eu.arrowhead.kalix.example.dto;

import eu.arrowhead.kalix.descriptor.InterfaceDescriptor;
import eu.arrowhead.kalix.dto.Writable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Writable
public interface ServiceRegistrationRequest {
    String serviceDefinition();

    ServiceRegistrationRequestProviderSystem providerSystem();

    String serviceUri();

    Optional<String> endOfValidity();

    Optional<String> secure();

    Optional<Map<String, String>> metadata();

    Optional<Integer> version();

    List<InterfaceDescriptor> interfaces();
}
