package se.arkalix.core.plugin.or;

import se.arkalix.core.plugin.ServiceProvider;
import se.arkalix.core.plugin.SystemDetails;
import se.arkalix.core.plugin.sr.ServiceQuery;
import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.dto.json.DtoJsonName;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * A request for orchestration.
 * <p>
 * An orchestration request, or query, ideally results in a useful
 * {@link OrchestrationQueryResult list of services}.
 */
@DtoWritableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface OrchestrationQuery {
    /**
     * Details about the system making the orchestration query.
     *
     * @return Requester system details.
     */
    @DtoJsonName("requesterSystem")
    SystemDetails requester();

    /**
     * Description of the specific kind of service the querying system desires
     * to consume.
     *
     * @return Requested service query.
     */
    @DtoJsonName("requestedService")
    Optional<ServiceQuery> service();

    /**
     * A list of systems the querying system prefers to provide the requested
     * service.
     *
     * @return List of preferred service providers.
     */
    @DtoJsonName("preferredProviders")
    List<ServiceProvider> providers();

    /**
     * A map of orchestration {@link OrchestrationOption options}.
     *
     * @return Orchestration options map.
     */
    @DtoJsonName("orchestrationFlags")
    Map<OrchestrationOption, Boolean> options();
}
