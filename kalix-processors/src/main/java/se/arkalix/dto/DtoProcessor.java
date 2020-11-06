package se.arkalix.dto;

import com.squareup.javapoet.JavaFile;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DtoProcessor extends AbstractProcessor {
    private Filer filer;
    private Messager messager;
    private Elements elementUtils;

    private DtoTargetFactory targetFactory;
    private DtoImplementerFactory implementerFactory;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();

        targetFactory = new DtoTargetFactory(elementUtils, processingEnv.getTypeUtils());
        implementerFactory = new DtoImplementerFactory(
            new DtoImplementerJson()
        );
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return true;
        }
        try {
            final var interfaceTypes = findAnnotatedInterfaces(roundEnv);
            for (final var interfaceType : interfaceTypes) {
                final var target = targetFactory.createFromInterface(interfaceType);
                final var specification = implementerFactory.createForTarget(target);

                final var packageName = elementUtils.getPackageOf(interfaceType)
                    .getQualifiedName().toString();

                JavaFile.builder(packageName, specification.implementation())
                    .indent("    ").build()
                    .writeTo(filer);

                JavaFile.builder(packageName, specification.builder())
                    .indent("    ").build()
                    .writeTo(filer);
            }
        }
        catch (final DtoException exception) {
            print(exception, exception.offendingElement());
        }
        catch (final Throwable throwable) {
            print(throwable, null);
        }
        return true;
    }

    private void print(final Throwable throwable, final Element offendingElement) {
        final var writer = new StringWriter();
        final var printer = new PrintWriter(writer);
        throwable.printStackTrace(printer);
        messager.printMessage(Diagnostic.Kind.ERROR, writer.toString(), offendingElement);
    }

    private Collection<TypeElement> findAnnotatedInterfaces(final RoundEnvironment roundEnv) {
        final var interfaces = new HashMap<Name, TypeElement>();
        getSupportedAnnotationClasses()
            .forEach(annotation -> roundEnv.getElementsAnnotatedWith(annotation)
                .forEach(element -> {
                    if (element.getKind() != ElementKind.INTERFACE) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Only " +
                            "interfaces may be annotated with @DtoReadableAs " +
                            "and/or @DtoWritableAs", element);
                        return;
                    }
                    final var typeElement = (TypeElement) element;
                    interfaces.put(typeElement.getQualifiedName(), typeElement);
                }));
        return interfaces.values();
    }

    private Stream<Class<? extends Annotation>> getSupportedAnnotationClasses() {
        return Stream.of(DtoReadableAs.class, DtoWritableAs.class);
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
