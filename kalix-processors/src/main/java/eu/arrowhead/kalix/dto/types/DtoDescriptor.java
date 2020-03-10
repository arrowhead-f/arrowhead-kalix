package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.TypeKind;

public enum DtoDescriptor {
    ARRAY(0x1100),
    BIG_DECIMAL(0x2000),
    BIG_INTEGER(0x2000),
    BOOLEAN_BOXED(0x0601),
    BOOLEAN_UNBOXED(0x0201),
    BYTE_BOXED(0x2602),
    BYTE_UNBOXED(0x2202),
    CHARACTER_BOXED(0x0604),
    CHARACTER_UNBOXED(0x0204),
    DOUBLE_BOXED(0x2608),
    DOUBLE_UNBOXED(0x2208),
    ENUM(0x0800),
    FLOAT_BOXED(0x2610),
    FLOAT_UNBOXED(0x2210),
    INTEGER_BOXED(0x2620),
    INTEGER_UNBOXED(0x2220),
    INTERFACE(0x0000),
    LIST(0x1100),
    LONG_BOXED(0x2640),
    LONG_UNBOXED(0x2240),
    MAP(0x0100),
    SHORT_BOXED(0x2680),
    SHORT_UNBOXED(0x2280),
    STRING(0x0800);

    private final short mask;

    DtoDescriptor(final int mask) {
        this.mask = (short) mask;
    }

    public boolean isArrayOrList() {
        return (mask & 0x1100) == 0x1100;
    }

    public boolean isBoolean() {
        return (mask & 0x0001) == 0x0001;
    }

    public boolean isByte() {
        return (mask & 0x0002) == 0x0002;
    }

    public boolean isCharacter() {
        return (mask & 0x0004) == 0x0004;
    }

    public boolean isCollection() {
        return (mask & 0x0100) == 0x0100;
    }

    public boolean isDouble() {
        return (mask & 0x0008) == 0x0008;
    }

    public boolean isFloat() {
        return (mask & 0x0010) == 0x0010;
    }

    public boolean isInteger() {
        return (mask & 0x0020) == 0x0020;
    }

    public boolean isLong() {
        return (mask & 0x0040) == 0x0040;
    }

    public boolean isNumber() {
        return (mask & 0x2000) == 0x2000;
    }

    public boolean isPrimitive() {
        return (mask & 0x0200) == 0x0200;
    }

    public boolean isPrimitiveBoxed() {
        return (mask & 0x0600) == 0x0600;
    }

    public boolean isPrimitiveUnboxed() {
        return (mask & 0x0600) == 0x0200;
    }

    public boolean isShort() {
        return (mask & 0x0080) == 0x0080;
    }

    public boolean isStringOrEnum() {
        return (mask & 0x0800) == 0x0800;
    }

    public static DtoDescriptor valueOf(final TypeKind typeKind) {
        switch (typeKind) {
        case BOOLEAN: return BOOLEAN_UNBOXED;
        case BYTE: return BYTE_UNBOXED;
        case SHORT: return SHORT_UNBOXED;
        case INT: return INTEGER_UNBOXED;
        case LONG: return LONG_UNBOXED;
        case CHAR: return CHARACTER_UNBOXED;
        case FLOAT: return FLOAT_UNBOXED;
        case DOUBLE: return DOUBLE_UNBOXED;
        default:
            throw new IllegalArgumentException("Not a primitive type: " + typeKind);
        }
    }
}
