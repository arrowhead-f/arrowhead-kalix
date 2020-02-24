package eu.arrowhead.kalix.processors.io;

import com.squareup.javapoet.*;
import eu.arrowhead.kalix.util.io.Encodable;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EncodableProcessor extends AbstractProcessor {
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

        for (final var element : roundEnv.getElementsAnnotatedWith(Encodable.class)) {
            try {
                final var target = DataTarget.createFrom(element);
                JavaFile.builder(target.packageName(), target.typeSpec())
                    .build()
                    .writeTo(filer);
            }
            catch (final EncodableException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage(), e.offendingElement());
            }
            catch (final IOException exception) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@Encodable " +
                    "class could not be generated; cause: " +
                    exception.getMessage(), element);
            }
        }

        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Stream.of(Encodable.class.getCanonicalName())
            .collect(Collectors.toSet());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
