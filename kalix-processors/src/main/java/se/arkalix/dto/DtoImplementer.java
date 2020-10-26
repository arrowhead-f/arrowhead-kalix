package se.arkalix.dto;

import com.squareup.javapoet.TypeSpec;

public interface DtoImplementer {
    DtoEncoding encoding();

    void implementFor(DtoTarget target, TypeSpec.Builder implementation) throws DtoException;
}
