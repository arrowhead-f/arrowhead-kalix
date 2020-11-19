package se.arkalix.dto;

import com.squareup.javapoet.TypeSpec;

// TODO: Make it possible to use custom DtoImplementer implementations.
public interface DtoGeneratorBackend {
    DtoCodec codec();

    String decodeMethodName();
    String encodeMethodName();

    void generateDecodeMethodFor(DtoTarget target, TypeSpec.Builder implementation);
    void generateEncodeMethodFor(DtoTarget target, TypeSpec.Builder implementation);
}
