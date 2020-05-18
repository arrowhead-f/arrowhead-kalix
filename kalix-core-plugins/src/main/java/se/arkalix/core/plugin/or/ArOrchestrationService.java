package se.arkalix.core.plugin.or;

import se.arkalix.ArConsumer;
import se.arkalix.util.concurrent.Future;

/**
 * Represents an Arrowhead orchestration service.
 */
public interface ArOrchestrationService extends ArConsumer {
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
