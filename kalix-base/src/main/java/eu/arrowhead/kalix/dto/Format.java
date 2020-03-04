package eu.arrowhead.kalix.dto;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;

/**
 * The encoding formats that can be read and written by the Kalix DTO package.
 */
public enum Format {
    JSON(EncodingDescriptor.JSON);

    private final EncodingDescriptor descriptor;

    Format(final EncodingDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * @return Arrowhead descriptor corresponding to this format.
     */
    public EncodingDescriptor asDescriptor() {
        return descriptor;
    }
}
