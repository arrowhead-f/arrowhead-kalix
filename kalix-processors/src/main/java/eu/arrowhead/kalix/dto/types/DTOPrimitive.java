package eu.arrowhead.kalix.dto.types;

public interface DTOPrimitive extends DTOType {
    DTOPrimitiveType primitiveType();

    @Override
    default boolean isCollection() {
        return false;
    }
}
