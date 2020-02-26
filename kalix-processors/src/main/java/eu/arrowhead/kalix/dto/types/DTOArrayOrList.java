package eu.arrowhead.kalix.dto.types;

public interface DTOArrayOrList extends DTOType {
    DTOType element();

    @Override
    default boolean isCollection() {
        return true;
    }
}
