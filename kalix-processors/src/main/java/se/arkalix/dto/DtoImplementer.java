package se.arkalix.dto;

import com.squareup.javapoet.TypeSpec;

public interface DtoImplementer {
    DtoEncodingSpec encoding();

    void implementFor(DtoTarget target, TypeSpec.Builder implementation) throws DtoException;
}
