package eu.arrowhead.kalix.example.dto;

import eu.arrowhead.kalix.dto.Writable;

import java.util.Optional;

@Writable
public interface ServiceRegistrationRequestProviderSystem {
    String systemName();

    String address();

    int port();

    String authenticationInfo();
}
