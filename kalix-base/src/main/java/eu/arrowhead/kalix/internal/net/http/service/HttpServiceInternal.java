package eu.arrowhead.kalix.internal.net.http.service;

import eu.arrowhead.kalix.AhfService;
import eu.arrowhead.kalix.description.ServiceDescription;
import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.net.http.HttpStatus;
import eu.arrowhead.kalix.net.http.service.*;
import eu.arrowhead.kalix.util.annotation.Internal;
import eu.arrowhead.kalix.util.concurrent.Future;
import eu.arrowhead.kalix.util.concurrent.Futures;

import java.util.*;
import java.util.stream.Collectors;

@Internal
public class HttpServiceInternal implements AhfService {
    private final ServiceDescription description;
    private final List<EncodingDescriptor> encodings;
    private final List<HttpRouteSequence> routeSequences;

    public HttpServiceInternal(final HttpService service) {
        description = service.describe();

        final var basePath = description.qualifier();
        if (!HttpPaths.isValidPathWithoutPercentEncodings(basePath)) {
            throw new IllegalArgumentException("HttpService basePath \"" +
                basePath + "\" must start with a forward slash (/) and then " +
                "contain only the following characters: A–Z a–z 0–9 " +
                "-._~!$%&'()*+,;/=:@");
        }
        if (basePath.length() > 1 && basePath.charAt(basePath.length() - 1) == '/') {
            throw new IllegalArgumentException("HttpService basePath may " +
                "not end with a forward slash (/) unless it is the root " +
                "path \"/\"");
        }

        encodings = service.encodings();
        if (encodings.size() == 0) {
            throw new IllegalArgumentException("Expected HttpService encodings.size() > 0");
        }

        final var routeSequenceFactory = new HttpRouteSequenceFactory(service.catchers(), service.validators());
        routeSequences = service.routes().stream()
            .sorted(HttpRoutables::compare)
            .map(routeSequenceFactory::createRouteSequenceFor)
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * @return Service name.
     */
    public String name() {
        return description.name();
    }

    /**
     * @return Base path that the paths of all requests targeted at this
     * service.
     */
    public String basePath() {
        return description.qualifier();
    }

    /**
     * @return The encoding to use by default.
     */
    public EncodingDescriptor defaultEncoding() {
        return encodings.get(0);
    }

    /**
     * @return Data encodings supported by this service.
     */
    public List<EncodingDescriptor> encodings() {
        return encodings;
    }

    /**
     * Delegates handling of an {@link HttpServiceRequest} to this service.
     *
     * @param request  Incoming HTTP request.
     * @param response Modifiable HTTP response object, destined to be sent
     *                 back to the original request sender.
     * @return Future completed with {@code null} value when handling has
     * finished.
     */
    public Future<?> handle(final HttpServiceRequest request, final HttpServiceResponse response) {
        final var task = new HttpRouteTask.Builder()
            .basePath(basePath())
            .request(request)
            .response(response)
            .build();

        return Futures.flatReducePlain(routeSequences, false,
            (isHandled, routeSequence) -> {
                if (isHandled) {
                    return Future.success(true);
                }
                return routeSequence.tryHandle(task);
            })
            .map(isHandled -> {
                if (!isHandled) {
                    response
                        .status(HttpStatus.NOT_FOUND)
                        .clearHeaders()
                        .clearBody();
                }
                return null;
            });
    }

    @Override
    public ServiceDescription describe() {
        return description;
    }
}
