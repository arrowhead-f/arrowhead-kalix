package eu.arrowhead.kalix;

import eu.arrowhead.kalix.description.ServiceDescription;
import eu.arrowhead.kalix.util.concurrent.Future;

public interface AhfServiceHandle {
    ServiceDescription description();

    Future<?> dismiss();
}
