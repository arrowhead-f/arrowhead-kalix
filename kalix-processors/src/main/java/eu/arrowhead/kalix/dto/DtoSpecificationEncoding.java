package eu.arrowhead.kalix.dto;

import com.squareup.javapoet.TypeSpec;

public interface DtoSpecificationEncoding {
    DataEncoding encoding();

    void implementFor(DtoTarget target, TypeSpec.Builder implementation) throws DtoException;
}
