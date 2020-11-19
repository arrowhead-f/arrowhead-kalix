package se.arkalix.dto.types;

public interface DtoTypeCollection extends DtoType {
    boolean containsInterfaceType();
    boolean containsOptional();
}
