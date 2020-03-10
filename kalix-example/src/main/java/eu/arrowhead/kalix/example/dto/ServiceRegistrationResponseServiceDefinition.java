package eu.arrowhead.kalix.example.dto;

import eu.arrowhead.kalix.dto.Readable;

@Readable
public interface ServiceRegistrationResponseServiceDefinition {
    int id();

    String serviceDefinition();

    String createdAt();

    String updatedAt();
}
