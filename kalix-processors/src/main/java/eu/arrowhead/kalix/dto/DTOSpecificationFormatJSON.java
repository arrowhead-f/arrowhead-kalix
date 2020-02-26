package eu.arrowhead.kalix.dto;

import java.util.Optional;

public class DTOSpecificationFormatJSON implements DTOSpecificationFormat {
    @Override
    public Format format() {
        return Format.JSON;
    }

    @Override
    public void implementFor(final DTOTargetSpecification targetSpecification) throws DTOException {

    }

    @Override
    public Optional<DTOUtilitySpecification> utilitySpecification() {
        return Optional.empty();
    }
}
