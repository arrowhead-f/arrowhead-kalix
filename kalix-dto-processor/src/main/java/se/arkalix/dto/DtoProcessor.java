package se.arkalix.dto;

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

    private DtoAnalyzer analyzer;
    private DtoGenerator generator;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();

        analyzer = new DtoAnalyzer(processingEnv);
        generator = new DtoGenerator(
            new DtoGeneratorBackendJson()
        );
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        annotations.stream()
            .flatMap(annotation -> roundEnv.getElementsAnnotatedWith(annotation).stream())
            .distinct()
            .forEach(element -> {
                try {
                    final var target = analyzer.analyze(element);
                    final var packageName = elementUtils.getPackageOf(element)
                        .getQualifiedName()
                        .toString();

                    generator.writeTo(target, packageName, filer);
                }
                catch (final Throwable throwable) {
                    final var writer = new StringWriter();
                    final var printer = new PrintWriter(writer);
                    throwable.printStackTrace(printer);
                    //printer.append(throwable.getMessage());
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
