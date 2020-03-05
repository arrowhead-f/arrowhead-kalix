package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.dto.DataReadable;
import eu.arrowhead.kalix.net.http.service.HttpServiceRequestBody;
import eu.arrowhead.kalix.util.concurrent.Future;

public class NettyHttpServiceRequestBody implements HttpServiceRequestBody {
    @Override
    public <R extends DataReadable> Future<R> bodyAs(final Class<R> class_) {
        return null;
    }
}
