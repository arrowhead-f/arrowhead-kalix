package se.arkalix.core.plugin.cp;

import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.util.Map;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * A parameterized reference to a contract template.
 * <p>
 * A contract template is a legal text where certain portions of the text must
 * be substituted for concrete values for it to become an actual contract. This
 * type names such a template and specifies values to be inserted into the
 * text at designated locations.
 * <p>
 * Instances of this type are trusted in the sense that they either (1) come
 * from trusted sources or (2) will be sent to systems that trust their senders.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoEqualsHashCode
@DtoToString
@SuppressWarnings("unused")
public interface TrustedContract {
    /**
     * Name of template parameterized by this contract.
     *
     * @return Template name.
     */
    String templateName();

    /**
     * Concrete template parameter name/value pairs.
     *
     * @return Contract arguments.
     */
    Map<String, String> arguments();
}
