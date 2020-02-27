package eu.arrowhead.kalix.dto;

import com.squareup.javapoet.TypeSpec;

public interface DtoSpecificationFormat {
    Format format();

    void implementFor(DtoTarget target, TypeSpec.Builder implementation) throws DtoException;
}
