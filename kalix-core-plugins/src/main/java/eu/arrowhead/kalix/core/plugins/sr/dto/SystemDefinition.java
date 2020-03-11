package eu.arrowhead.kalix.core.plugins.sr.dto;

import eu.arrowhead.kalix.dto.Readable;
import eu.arrowhead.kalix.dto.json.JsonName;

import java.time.Instant;
import java.util.Optional;

@Readable
public interface SystemDefinition {
    long id();

    @JsonName("systemName")
    String name();

    @JsonName("address")
    String hostname();

    int port();

    @JsonName("authenticationInfo")
    Optional<String> publicKeyBase64();

    Instant createdAt();

    Instant updatedAt();
}
