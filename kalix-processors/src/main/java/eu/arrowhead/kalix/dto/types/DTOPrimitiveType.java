package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.TypeKind;

public enum DTOPrimitiveType {
    BOOLEAN,
    BYTE,
    CHARACTER,
    DOUBLE,
    FLOAT,
    INTEGER,
    LONG,
    SHORT;

    public static DTOPrimitiveType valueOf(final TypeKind typeKind) {
        switch (typeKind) {
        case BOOLEAN: return BOOLEAN;
        case BYTE: return BYTE;
        case SHORT: return SHORT;
        case INT: return INTEGER;
        case LONG: return LONG;
        case CHAR: return CHARACTER;
        case FLOAT: return FLOAT;
        case DOUBLE: return DOUBLE;
        default:
            throw new IllegalArgumentException("Not a primitive type: " + typeKind);
        }
    }
}
