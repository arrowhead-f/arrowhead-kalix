package eu.arrowhead.kalix.dto;

import com.squareup.javapoet.TypeSpec;

public interface DTOSpecificationFormat {
    Format format();

    void implementFor(DTOTarget target, TypeSpec.Builder implementation) throws DTOException;
}
