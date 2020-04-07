package se.arkalix;

import se.arkalix.description.ServiceDescription;

/**
 * Some object useful for consuming one particular Arrowhead service.
 * <p>
 * The term <i>consume</i> does, in the context of Arrowhead Framework, imply
 * that a service, provided by a <i>system</i>, is exchanged messages with.
 * Implementors of this interface are meant to make this consumption more
 * convenient by abstracting away details about things such as IP addresses,
 * authorization, required message headers, etc.
 *
 * @see ArSystem#consume()
 */
public interface ArConsumer {
    /**
     * @return Description of consumed service.
     */
    ServiceDescription service();
}
