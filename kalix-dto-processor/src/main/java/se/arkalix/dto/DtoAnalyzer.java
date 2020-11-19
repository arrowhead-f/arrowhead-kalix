package se.arkalix.dto;

import se.arkalix.dto.types.DtoInterface;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import java.util.stream.Collectors;

public class DtoAnalyzer {
    private final Elements elementUtils;
    private final DtoPropertyFactory propertyFactory;

    public DtoAnalyzer(final ProcessingEnvironment processingEnv) {
        elementUtils = processingEnv.getElementUtils();
        propertyFactory = new DtoPropertyFactory(elementUtils, processingEnv.getTypeUtils());
    }

    public DtoTarget analyze(final Element element) {
        final var dtoInterface = new DtoInterface(elementUtils, element);
        final var dtoProperties = dtoInterface.collectPropertyMethods()
            .map(propertyFactory::createFromMethod)
            .collect(Collectors.toUnmodifiableList());

        return new DtoTarget(dtoInterface, dtoProperties);
    }
}
