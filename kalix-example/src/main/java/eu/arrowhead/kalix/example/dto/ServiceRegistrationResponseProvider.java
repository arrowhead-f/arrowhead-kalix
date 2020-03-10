package eu.arrowhead.kalix.example.dto;

import eu.arrowhead.kalix.dto.Readable;

@Readable
public interface ServiceRegistrationResponseProvider {
    int id();

    String systemName();

    String address();

    int port();

    String authenticationInfo();

    String createdAt();

    String updatedAt();
}
