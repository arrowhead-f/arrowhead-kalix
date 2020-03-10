package eu.arrowhead.kalix.example.dto;

import eu.arrowhead.kalix.dto.Readable;

@Readable
public interface ServiceRegistrationResponseInterface {
    int id();

    String interfaceName();

    String createdAt();

    String updatedAt();
}
