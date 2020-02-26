package eu.arrowhead.kalix.dto;

import java.util.Optional;

public interface DTOSpecificationFormat {
    Format format();

    void implementFor(DTOTargetSpecification targetSpecification) throws DTOException;

    Optional<DTOUtilitySpecification> utilitySpecification();
}
