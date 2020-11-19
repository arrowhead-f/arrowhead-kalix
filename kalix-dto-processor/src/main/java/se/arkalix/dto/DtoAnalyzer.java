package se.arkalix.dto;

import se.arkalix.dto.types.DtoTypeInterface;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
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
        final var interface_ = new DtoTypeInterface(element);
        final var properties = elementUtils.getAllMembers(interface_.element())
            .stream()
            .filter(member -> {
                if (member.getEnclosingElement().getKind() != ElementKind.INTERFACE ||
                    member.getKind() != ElementKind.METHOD)
                {
                    return false;
                }
                final var modifiers = member.getModifiers();
                return !modifiers.contains(Modifier.DEFAULT) && !modifiers.contains(Modifier.STATIC);
            })
            .map(member -> (ExecutableElement) member)
            .map(propertyFactory::createFromMethod)
            .collect(Collectors.toUnmodifiableList());

        return new DtoTarget(interface_, properties);
    }
}
