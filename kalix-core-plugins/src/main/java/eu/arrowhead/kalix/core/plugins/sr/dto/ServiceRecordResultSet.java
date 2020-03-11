package eu.arrowhead.kalix.core.plugins.sr.dto;

import eu.arrowhead.kalix.dto.Readable;
import eu.arrowhead.kalix.dto.json.JsonName;

import java.util.List;

@Readable
public interface ServiceRecordResultSet {
    @JsonName("serviceQueryData")
    ServiceRecord[] entries();

    int unfilteredHits();
}
