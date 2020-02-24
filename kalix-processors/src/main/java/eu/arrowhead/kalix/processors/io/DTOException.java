package eu.arrowhead.kalix.processors.io;

import javax.lang.model.element.Element;

public class DTOException extends Exception {
    private final Element offendingElement;

    public DTOException(final Element offendingElement, final String message) {
        super(message);
        this.offendingElement = offendingElement;
    }

    public Element offendingElement() {
        return offendingElement;
    }
}
