package eu.arrowhead.kalix.dto;

import eu.arrowhead.kalix.dto.types.*;
import eu.arrowhead.kalix.processors.io.DTOException;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;

public class DTOPropertyFactory {
    private final Elements elementUtils;
    private final Types typeUtils;
    private final DeclaredType booleanType;
    private final DeclaredType byteType;
    private final DeclaredType characterType;
    private final DeclaredType doubleType;
    private final DeclaredType floatType;
    private final DeclaredType integerType;
    private final DeclaredType listType;
    private final DeclaredType longType;
    private final DeclaredType mapType;
    private final DeclaredType optionalType;
    private final DeclaredType shortType;
    private final DeclaredType stringType;

    public DTOPropertyFactory(final Elements elementUtils, final Types typeUtils) {
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;

        booleanType = getDeclaredTypeOf(elementUtils, Boolean.class);
        byteType = getDeclaredTypeOf(elementUtils, Byte.class);
        characterType = getDeclaredTypeOf(elementUtils, Character.class);
        doubleType = getDeclaredTypeOf(elementUtils, Double.class);
        floatType = getDeclaredTypeOf(elementUtils, Float.class);
        integerType = getDeclaredTypeOf(elementUtils, Integer.class);
        listType = getDeclaredTypeOf(elementUtils, List.class);
        longType = getDeclaredTypeOf(elementUtils, Long.class);
        mapType = getDeclaredTypeOf(elementUtils, Map.class);
        optionalType = getDeclaredTypeOf(elementUtils, Optional.class);
        shortType = getDeclaredTypeOf(elementUtils, Short.class);
        stringType = getDeclaredTypeOf(elementUtils, String.class);
    }

    private static DeclaredType getDeclaredTypeOf(final Elements elementUtils, final Class<?> class_) {
        return (DeclaredType) elementUtils.getTypeElement(class_.getCanonicalName()).asType();
    }

    public DTOProperty createFromMethod(final Element method) throws DTOException {
        if (!(method instanceof ExecutableElement) || method.getKind() != ElementKind.METHOD) {
            throw new DTOException(method, "Must be method");
        }
        final var builder = new DTOProperty.Builder()
            .name(method.getSimpleName().toString())
            .formatNames(collectFormatNamesFrom(method));

        var type = ((ExecutableElement) method).getReturnType();

        if (type.getKind().isPrimitive()) {
            return builder
                .type(toPrimitiveType(type))
                .isOptional(false)
                .build();
        }
        if (type.getKind() == TypeKind.ARRAY) {
            return builder
                .type(toArrayType(type))
                .isOptional(false)
                .build();
        }

        if (!(type instanceof DeclaredType)) {
            throw new DTOException(method, "Getter return type must " +
                "be primitive, boxed primitive, T[], List<T>, Map<K, V>, " +
                "enum, an enum-like class or be annotated with @Readable " +
                "and/or @Writable");
        }

        if (typeUtils.isSameType(typeUtils.erasure(type), optionalType)) {
            final var declaredType = (DeclaredType) type;
            final var argumentType = declaredType.getTypeArguments().get(0);
            return builder
                .type(resolveType(argumentType))
                .isOptional(true)
                .build();
        }

        return builder
            .type(resolveType(type))
            .isOptional(false)
            .build();
    }

    private Map<Format, String> collectFormatNamesFrom(final Element method) {
        final var formatNames = new HashMap<Format, String>();
        final var nameJSON = method.getAnnotation(NameJSON.class);
        if (nameJSON != null) {
            formatNames.put(Format.JSON, nameJSON.value());
        }
        return formatNames;
    }

    private DTOType resolveType(final TypeMirror type) throws DTOException {
        if (type.getKind() == TypeKind.ARRAY) {
            return toArrayType(type);
        }
        if (typeUtils.isSameType(booleanType, type) ||
            typeUtils.isSameType(byteType, type) ||
            typeUtils.isSameType(characterType, type) ||
            typeUtils.isSameType(doubleType, type) ||
            typeUtils.isSameType(floatType, type) ||
            typeUtils.isSameType(integerType, type) ||
            typeUtils.isSameType(longType, type) ||
            typeUtils.isSameType(shortType, type)
        ) {
            return toBoxedPrimitiveType(type);
        }
        if (typeUtils.asElement(type).getKind() == ElementKind.ENUM) {
            return toEnumType(type);
        }
        if (typeUtils.isSameType(listType, typeUtils.erasure(type))) {
            return toListType(type);
        }
        if (typeUtils.isSameType(mapType, typeUtils.erasure(type))) {
            return toMapType(type);
        }
        if (typeUtils.isSameType(stringType, type)) {
            return toStringType(type);
        }
        if (isEnumLike(type)) {
            return toEnumLikeType(type);
        }
        return toDTOType(type);
    }

    private boolean isEnumLike(final TypeMirror type) {
        final var element = typeUtils.asElement(type);
        if (element == null || element.getKind() != ElementKind.CLASS) {
            return false;
        }
        final var typeElement = (TypeElement) element;
        for (final var enclosed : typeElement.getEnclosedElements()) {
            if (enclosed.getKind() != ElementKind.METHOD &&
                !Objects.equals(enclosed.getSimpleName().toString(), "valueOf")
            ) {
                continue;
            }
            if (!enclosed.getModifiers().containsAll(Arrays.asList(Modifier.PUBLIC, Modifier.STATIC))) {
                continue;
            }
            final var executable = (ExecutableElement) enclosed;
            final var parameters = executable.getParameters();
            if (parameters.size() != 1 || !typeUtils.isSameType(parameters.get(0).asType(), stringType)) {
                continue;
            }
            return true;
        }
        return false;
    }

    private DTOTypeArray toArrayType(final TypeMirror type) throws DTOException {
        final var arrayType = (ArrayType) type;
        return new DTOTypeArray(arrayType, resolveType(arrayType.getComponentType()));
    }

    private DTOTypeBoxedPrimitive toBoxedPrimitiveType(final TypeMirror type) {
        return new DTOTypeBoxedPrimitive((DeclaredType) type);
    }

    private DTOTypeDTO toDTOType(final TypeMirror type) throws DTOException {
/*
        if (type.getAnnotation(Readable.class) != null ||
            type.getAnnotation(Writable.class) != null
        ) {
            return true;
        }
        if (type.getKind() == TypeKind.DECLARED) {
            final var element = ((DeclaredType) type).asElement();
            if (element.getKind().isInterface()) {
                final var interfaceElement = (TypeElement) element;
                interfaceElement.getInterfaces()
            }
        }*/

        return null;
    }

    private DTOTypeEnum toEnumType(final TypeMirror type) {
        return null;
    }

    private DTOTypeEnumLike toEnumLikeType(final TypeMirror type) {
        return null;
    }

    private DTOTypeList toListType(final TypeMirror type) {
        return null;
    }

    private DTOTypeMap toMapType(final TypeMirror type) {
        return null;
    }

    private DTOTypeString toStringType(final TypeMirror type) {
        return null;
    }

    private DTOTypePrimitive toPrimitiveType(final TypeMirror type) {
        return new DTOTypePrimitive((PrimitiveType) type);
    }
}
