package se.arkalix.dto.types;

import com.squareup.javapoet.ClassName;
import se.arkalix.dto.DtoException;
import se.arkalix.dto.DtoTarget;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.Objects;

public class DtoTypeInterface implements DtoType {
    private final TypeElement interfaceElement;
    private final ClassName interfaceTypeName;
    private final ClassName generatedTypeName;

    public DtoTypeInterface(final Element element) {
        Objects.requireNonNull(element, "element");

        if (element.getKind() != ElementKind.INTERFACE) {
            throw new DtoException(element, "Only interfaces may " +
                "be annotated with @DtoReadableAs and/or @DtoWritableAs");
        }

        interfaceElement = (TypeElement) element;

        if (interfaceElement.getTypeParameters().size() != 0) {
            throw new DtoException(interfaceElement, "interfaces annotated " +
                "with @DtoReadableAs and/or @DtoWritableAs must not have " +
                "any generic type parameters");
        }

        final String interfaceSimpleName = interfaceElement.getSimpleName().toString();

        if (interfaceSimpleName.endsWith(DtoTarget.DATA_SUFFIX)) {
            throw new DtoException(interfaceElement, "interfaces annotated " +
                "with @DtoReadableAs and/or @DtoWritableAs must not have " +
                "names ending with \"" + DtoTarget.DATA_SUFFIX + "\"");
        }

        interfaceTypeName = ClassName.get(interfaceElement);

        final var generatedSimpleName = interfaceSimpleName + DtoTarget.DATA_SUFFIX;
        generatedTypeName = ClassName.get(packageNameOf(interfaceElement), generatedSimpleName);
    }

    public TypeElement element() {
        return interfaceElement;
    }

    @Override
    public DtoDescriptor descriptor() {
        return DtoDescriptor.INTERFACE;
    }

    @Override
    public ClassName interfaceTypeName() {
        return interfaceTypeName;
    }

    @Override
    public ClassName generatedTypeName() {
        return generatedTypeName;
    }

    @Override
    public String toString() {
        return interfaceTypeName.toString();
    }

    private static String packageNameOf(Element element) {
        while (element != null) {
            if (element instanceof PackageElement) {
                return ((PackageElement) element).getQualifiedName().toString();
            }
            element = element.getEnclosingElement();
        }
        return "";
    }
}
