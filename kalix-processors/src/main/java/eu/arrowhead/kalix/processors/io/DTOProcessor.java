package eu.arrowhead.kalix.processors.io;

import com.squareup.javapoet.JavaFile;
import eu.arrowhead.kalix.dto.Readable;
import eu.arrowhead.kalix.dto.Writable;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DTOProcessor extends AbstractProcessor {
    private Filer filer;
    private Messager messager;
    private Elements elementUtils;
    private Types typeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return true;
        }
        try {
            final var interfaceTypes = collectDTOsUsing(roundEnv);
            for (final var interfaceType : interfaceTypes) {
                final var dtoClass = new DTOClass(interfaceType, elementUtils, typeUtils);
                final var packageName = elementUtils.getPackageOf(interfaceType)
                    .getQualifiedName().toString();
                final var dtoClassSpec = DTOSpecifier.specifyClass(dtoClass);
                JavaFile.builder(packageName, dtoClassSpec)
                    .indent("    ").build()
                    .writeTo(filer);
            }
        }
        catch (final DTOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage(), e.offendingElement());
        }
        catch (final IOException exception) {
            messager.printMessage(Diagnostic.Kind.ERROR, "DTO class could " +
                "not be generated; reason: " + exception);
        }
        return true;
    }

    private Collection<TypeElement> collectDTOsUsing(final RoundEnvironment roundEnv) {
        final var subjects = new HashMap<Name, TypeElement>();
        getSupportedAnnotationClasses().forEach(annotation -> {
            for (final var element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (element instanceof TypeElement) {
                    final var typeElement = (TypeElement) element;
                    subjects.put(typeElement.getQualifiedName(), typeElement);
                }
                else {
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "DTO must be of type `interface`",
                        element
                    );
                }
            }
        });
        return subjects.values();
    }

    private Stream<Class<? extends Annotation>> getSupportedAnnotationClasses() {
        return Stream.of(Readable.class, Writable.class);
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
