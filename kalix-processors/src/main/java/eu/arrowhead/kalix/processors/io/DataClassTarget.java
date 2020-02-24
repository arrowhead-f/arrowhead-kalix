package eu.arrowhead.kalix.processors.io;

import javax.annotation.processing.Messager;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;
import java.util.*;

/**
 * Holds data about some class annotated with {@code @Encodable}.
 */
public class DataClassTarget {
    private final String simpleName;
    private final String packageName;
    private final List<ExecutableElement> getters;
    private final List<ExecutableElement> setters;

    private DataClassTarget(
        final TypeElement root,
        final List<ExecutableElement> getters,
        final List<ExecutableElement> setters
    ) {
        final var simpleName = root.getSimpleName().toString();
        final var qualifiedName = root.getQualifiedName().toString();

        this.simpleName = "Data" + simpleName;
        this.packageName = simpleName.length() >= qualifiedName.length()
            ? ""
            : qualifiedName.substring(0, qualifiedName.length() - simpleName.length() - 1);

        this.getters = getters;
        this.setters = setters;
    }

    public static Optional<DataClassTarget> tryCreateFromOrReport(final Element root, final Messager messager) {
        error:
        {
            if (!(root instanceof TypeElement) || root.getKind() != ElementKind.INTERFACE) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@Encodable " +
                    "target must be interface", root);
                break error;
            }
            final var root0 = (TypeElement) root;
            if (root0.getTypeParameters().size() != 0) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@Encodable " +
                    "interface may not have type parameters", root);
                break error;
            }
            if (root.getSimpleName().toString().endsWith("Data")) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@Encodable " +
                    "interface may not have name ending with `Data`", root);
            }
            final var getters = new ArrayList<ExecutableElement>();
            final var setters = new ArrayList<ExecutableElement>();
            for (final var element : root0.getEnclosedElements()) {
                if (element.getKind() != ElementKind.METHOD ||
                    element.getModifiers().contains(Modifier.DEFAULT)
                ) {
                    continue;
                }
                final var element0 = (ExecutableElement) element;
                if (element0.getReturnType().getKind() != TypeKind.VOID &&
                    element0.getParameters().size() == 0
                ) {
                    getters.add(element0);
                    continue;
                }
                if (element0.getReturnType().getKind() == TypeKind.VOID &&
                    element0.getParameters().size() == 1
                ) {
                    setters.add(element0);
                    continue;
                }
                messager.printMessage(Diagnostic.Kind.ERROR, "@Encodable " +
                    "interface method must be getter, setter, static or " +
                    "default", element);
            }

            return Optional.of(new DataClassTarget(root0, getters, setters));
        }
        return Optional.empty();
    }

    public String simpleName() {
        return simpleName;
    }

    public String packageName() {
        return packageName;
    }

    public List<ExecutableElement> getters() {
        return getters;
    }

    public List<ExecutableElement> setters() {
        return setters;
    }
}
