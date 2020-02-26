package eu.arrowhead.kalix.dto.types;

import eu.arrowhead.kalix.dto.Format;

public interface DTOArrayOrList extends DTOType {
    DTOType element();

    @Override
    default boolean isCollection() {
        return true;
    }

    @Override
    default boolean isReadable(final Format format) {
        return element().isReadable(format);
    }

    @Override
    default boolean isWritable(final Format format) {
        return element().isWritable(format);
    }
}
