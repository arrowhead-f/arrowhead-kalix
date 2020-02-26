package eu.arrowhead.kalix.dto;

import com.squareup.javapoet.TypeSpec;

import java.util.Optional;

public interface DTOSpecificationFormat {
    Format format();

    void implementFor(DTOTarget target, TypeSpec.Builder implementation) throws DTOException;

    Optional<DTOUtilitySpecification> utilitySpecification();
}
