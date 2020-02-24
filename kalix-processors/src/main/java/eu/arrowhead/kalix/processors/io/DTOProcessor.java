package eu.arrowhead.kalix.processors.io;

import com.squareup.javapoet.JavaFile;
import eu.arrowhead.kalix.util.io.Decodable;
import eu.arrowhead.kalix.util.io.Encodable;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DTOProcessor extends AbstractProcessor {
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return true;
        }
        try {
            for (final var interfaceType : collectDecodableAndEncodableInterfacesUsing(roundEnv)) {
                final var dataTarget = DTOTarget.createFrom(interfaceType);
                try {
                    JavaFile.builder(dataTarget.packageName(), dataTarget.typeSpec())
                        .build()
                        .writeTo(filer);
                }
                catch (final IOException exception) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "DTO " +
                        "class could not be generated; reason: " +
                        exception.getMessage(), dataTarget.interfaceElement());
                }
            }
        }
        catch (final DTOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage(), e.offendingElement());
        }
        return true;
    }

    private Collection<TypeElement> collectDecodableAndEncodableInterfacesUsing(final RoundEnvironment roundEnv) {
        final var subjects = new HashMap<Name, TypeElement>();
        getSupportedAnnotationClasses().forEach(annotation -> {
            for (final var element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (element instanceof TypeElement) {
                    final var typeElement = (TypeElement) element;
                    subjects.put(typeElement.getQualifiedName(), typeElement);
                }
                else {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Only " +
                        "interfaces may be annotated with @Decodable or " +
                        "@Encodable");
                }
            }
        });
        return subjects.values();
    }

    private Stream<Class<? extends Annotation>> getSupportedAnnotationClasses() {
        return Stream.of(Decodable.class, Encodable.class);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return getSupportedAnnotationClasses()
            .map(Class::getCanonicalName)
            .collect(Collectors.toSet());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
