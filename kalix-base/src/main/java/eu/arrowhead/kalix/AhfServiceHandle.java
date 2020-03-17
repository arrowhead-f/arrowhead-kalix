package eu.arrowhead.kalix;

import eu.arrowhead.kalix.description.ServiceDescription;

public interface AhfServiceHandle {
    ServiceDescription description();

    void dismiss();
}
