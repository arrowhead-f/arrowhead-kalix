package se.arkalix.dto;

import com.squareup.javapoet.JavaFile;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.PrintWriter;
import java.io.StringWriter;
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
        annotations.stream()
            .flatMap(annotation -> roundEnv.getElementsAnnotatedWith(annotation).stream())
            .distinct()
            .forEach(element -> {
                try {
                    final var target = targetFactory.createFromInterface(element);
                    final var specification = implementerFactory.createForTarget(target);

                    final var packageName = elementUtils.getPackageOf(element)
                        .getQualifiedName().toString();

                    JavaFile.builder(packageName, specification.implementation())
                        .indent("    ").build()
                        .writeTo(filer);

                    JavaFile.builder(packageName, specification.builder())
                        .indent("    ").build()
                        .writeTo(filer);
                }
                catch (final Throwable throwable) {
                    final var writer = new StringWriter();
                    final var printer = new PrintWriter(writer);
                    throwable.printStackTrace(printer);
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        writer.toString(),
                        throwable instanceof DtoException
                            ? ((DtoException) throwable).offendingElement()
                            : element
                    );
                }
            });
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Stream.of(DtoReadableAs.class, DtoWritableAs.class)
            .map(Class::getCanonicalName)
            .collect(Collectors.toSet());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
