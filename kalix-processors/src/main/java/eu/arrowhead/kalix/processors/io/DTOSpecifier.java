package eu.arrowhead.kalix.processors.io;

import com.squareup.javapoet.*;
import eu.arrowhead.kalix.util.io.DTO;

import javax.lang.model.element.Modifier;
import java.util.Set;

public class DTOSpecifier {
    private DTOSpecifier() {}

    public static TypeSpec specifyClass(final DTOClass dtoClass) {
        final var origin = dtoClass.origin();
        final var builder = TypeSpec.classBuilder(dtoClass.name())
            .addJavadoc("{@link " + origin.getSimpleName() + "} Data Transfer " +
                "Object (DTO).\n<p>\nAllows creation of concrete {@code " +
                origin.getSimpleName() + "} instances, which may either\n" +
                "be encodable, decodable or both, depending on which out " +
                "of\n{@code @DTO.Encodable} and {@code @DTO.Decodable} it " +
                "is annotated with.")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(TypeName.get(origin.asType()));

        dtoClass.properties().forEach(property -> {
            final var name = property.defaultName();
            final var type = TypeName.get(property.type());

            builder.addField(FieldSpec.builder(type, name, Modifier.PRIVATE).build());
            builder.addMethod(MethodSpec.methodBuilder(name)
                .addJavadoc("{@inheritDoc}")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(type)
                .addCode("return " + name + ";")
                .build());
        });

        if (dtoClass.isToBeDecodableAs(DTO.Format.JSON)) {
            builder
                .addSuperinterface(DTO.EncodableJSON.class)
                .addMethod(DTOSpecifierJSON.specifyDecodeMethodFor(dtoClass));
        }

        if (dtoClass.isToBeEncodableAs(DTO.Format.JSON)) {
            builder
                .addSuperinterface(DTO.DecodableJSON.class)
                .addMethod(DTOSpecifierJSON.specifyEncodeMethodFor(dtoClass));
        }

        return builder.build();
    }

    public static TypeSpec specifyDecoderMap(final Set<DTOClass> dtoClasses) {
        return TypeSpec.classBuilder("DTODecoderMap")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .build())
            .addMethod(DTOSpecifierJSON.specifyDecoderLookupFor(dtoClasses))
            .build();
    }
}
