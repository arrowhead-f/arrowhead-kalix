package se.arkalix.core.plugin.dto;

import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.dto.json.JsonName;

import static se.arkalix.dto.DtoEncoding.JSON;

@DtoWritableAs(JSON)
@DtoToString
public interface ServiceProvider {
    @JsonName("providerSystem")
    SystemDetails system();

    @JsonName("providerCloud")
    CloudDetails cloud();
}
