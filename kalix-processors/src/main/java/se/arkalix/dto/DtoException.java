package se.arkalix.dto;

import javax.lang.model.element.Element;

public class DtoException extends Exception {
    private final Element offendingElement;

    public DtoException(final Element offendingElement, final String message) {
        super(message);
        this.offendingElement = offendingElement;
    }

    public Element offendingElement() {
        return offendingElement;
    }
}
