package se.arkalix.dto.types;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;

public class DtoTypeCustom implements DtoType {
    private final TypeName typeName;
    private final TypeElement typeElement;
    private final Types typeUtils;
    private final Elements elementUtils;

    public DtoTypeCustom(
        final TypeMirror type,
        final TypeElement typeElement,
        final Types typeUtils,
        final Elements elementUtils
    ) {
        this.typeName = TypeName.get(type);
        this.typeElement = typeElement;
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
    }

    public boolean containsPublicMethod(final String returnType, final String name, final String... parameters) {
        return elementUtils.getAllMembers(typeElement)
            .stream()
            .anyMatch(element -> {
                if (element.getKind() != ElementKind.METHOD) {
                    return false;
                }

                final var modifiers = element.getModifiers();
                if (!modifiers.contains(Modifier.PUBLIC) || modifiers.contains(Modifier.STATIC))  {
                    return false;
                }

                final var executable = (ExecutableElement) element;
                if (!executable.getSimpleName().contentEquals(name)) {
                    return false;
                }

                final var returnType0 = elementUtils.getTypeElement(returnType);
                if (returnType0 == null) {
                    return false;
                }
                if (!typeUtils.isAssignable(executable.getReturnType(), returnType0.asType())) {
                    return false;
                }

                final var parameters0 = executable.getParameters();
                if (parameters.length != parameters0.size()) {
                    return false;
                }
                for (var i = 0; i < parameters.length; ++i) {
                    final var p0 = parameters0.get(i);
                    final var p1 = elementUtils.getTypeElement(parameters[i]);
                    if (p1 == null || !typeUtils.isSameType(p1.asType(), p0.asType())) {
                        return false;
                    }
                }
                return true;
            });
    }

    public boolean containsPublicStaticMethod(
        final String returnType,
        final String name,
        final String... parameters
    ) {
        return elementUtils.getAllMembers(typeElement)
            .stream()
            .anyMatch(element -> {
                if (element.getKind() != ElementKind.METHOD) {
                    return false;
                }

                final var modifiers = element.getModifiers();
                if (!modifiers.containsAll(List.of(Modifier.PUBLIC, Modifier.STATIC)))  {
                    return false;
                }

                final var executable = (ExecutableElement) element;
                if (!executable.getSimpleName().contentEquals(name)) {
                    return false;
                }

                final var returnType0 = elementUtils.getTypeElement(returnType);
                if (returnType0 == null) {
                    return false;
                }
                if (!typeUtils.isAssignable(executable.getReturnType(), returnType0.asType())) {
                    return false;
                }

                final var parameters0 = executable.getParameters();
                if (parameters.length != parameters0.size()) {
                    return false;
                }
                for (var i = 0; i < parameters.length; ++i) {
                    final var p0 = parameters0.get(i);
                    final var p1 = elementUtils.getTypeElement(parameters[i]);
                    if (p1 == null || !typeUtils.isSameType(p1.asType(), p0.asType())) {
                        return false;
                    }
                }
                return true;
            });
    }

    public TypeElement typeElement() {
        return typeElement;
    }

    @Override
    public DtoDescriptor descriptor() {
        return DtoDescriptor.CUSTOM;
    }

    @Override
    public TypeName interfaceTypeName() {
        return typeName;
    }

    @Override
    public String toString() {
        return typeName.toString();
    }
}
