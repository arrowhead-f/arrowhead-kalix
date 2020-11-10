package se.arkalix.core.plugin;

import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.dto.json.DtoJsonName;

import static se.arkalix.dto.DtoCodec.JSON;

@DtoWritableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface ServiceProvider {
    @DtoJsonName("providerSystem")
    SystemDetails system();

    @DtoJsonName("providerCloud")
    CloudDetails cloud();
}
