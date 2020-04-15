package se.arkalix.core.plugin;

import se.arkalix.ArConsumer;
import se.arkalix.core.plugin.dto.OrchestrationQueryDto;
import se.arkalix.core.plugin.dto.OrchestrationQueryResultDto;
import se.arkalix.util.concurrent.Future;

/**
 * Represents an Arrowhead orchestration service.
 */
public interface ArOrchestration extends ArConsumer {
    /**
     * Queries orchestration service for services that should be consumed.
     *
     * @param query Description of the requesting system and its wants and
     *              needs related to service consumption.
     * @return Future completed with the results of the query, if no errors
     * occurred.
     */
    Future<OrchestrationQueryResultDto> query(OrchestrationQueryDto query);
}
