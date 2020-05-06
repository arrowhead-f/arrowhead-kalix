package se.arkalix.core.plugin.or;

import se.arkalix.core.plugin.ServiceProvider;
import se.arkalix.core.plugin.SystemDetails;
import se.arkalix.core.plugin.sr.ServiceQuery;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.dto.json.JsonName;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * A request for orchestration.
 * <p>
 * An orchestration request, or query, ideally results in a useful
 * {@link OrchestrationQueryResult list of services}.
 */
@DtoWritableAs(JSON)
@DtoToString
public interface OrchestrationQuery {
    /**
     * Details about the system making the orchestration query.
     */
    @JsonName("requesterSystem")
    SystemDetails requester();

    /**
     * Description of the specific kind of service the querying system desires
     * to consume.
     */
    @JsonName("requestedService")
    Optional<ServiceQuery> service();

    /**
     * A list of systems the querying system prefers to provide the requested
     * service.
     */
    @JsonName("preferredProviders")
    List<ServiceProvider> providers();

    /**
     * A map of orchestration {@link OrchestrationOption options}.
     */
    @JsonName("orchestrationFlags")
    Map<OrchestrationOption, Boolean> options();
}
