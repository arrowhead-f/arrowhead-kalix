package eu.arrowhead.kalix.core.plugins.srv.dto;

import eu.arrowhead.kalix.dto.Writable;
import eu.arrowhead.kalix.dto.json.JsonName;

import java.util.Optional;

@Writable
public interface SystemDefinitionForm {
    @JsonName("systemName")
    String name();

    @JsonName("address")
    String hostname();

    int port();

    @JsonName("authenticationInfo")
    Optional<String> publicKeyBase64();
}
