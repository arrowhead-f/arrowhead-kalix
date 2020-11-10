package se.arkalix.net.http.service._internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.ArService;
import se.arkalix.ArSystem;
import se.arkalix.ServiceRecord;
import se.arkalix.codec.CodecType;
import se.arkalix.net.Uris;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.*;
import se.arkalix.security.access.AccessPolicy;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;

import java.util.*;

@Internal
public class HttpServerService {
    private static final Logger logger = LoggerFactory.getLogger(HttpServerService.class);

    private final AccessPolicy accessPolicy;
    private final String basePath;
    private final ArService service;
    private final List<CodecType> codecTypes;
    private final ArSystem provider;
    private final HttpRouteSequence[] routeSequences;

    public HttpServerService(final ArSystem provider, final HttpService service) {
        this.provider = Objects.requireNonNull(provider, "provider");
        this.service = Objects.requireNonNull(service, "service");
        accessPolicy = service.accessPolicy();

        final var basePath = service.uri();
        if (!Uris.isValidPathWithoutPercentEncodings(basePath)) {
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
        this.basePath = !Objects.equals(basePath, "/") ? basePath : null;

        codecTypes = service.codecType();
        if (codecTypes.size() == 0) {
            throw new IllegalArgumentException("Expected HttpService codecs.size() > 0");
        }

        final var routeSequenceFactory = new HttpRouteSequenceFactory(service.catchers(), service.filters());
        routeSequences = service.routes().stream()
            .sorted(HttpRoutables::compare)
            .map(routeSequenceFactory::createRouteSequenceFor)
            .toArray(HttpRouteSequence[]::new);
    }

    /**
     * @return Service name.
     */
    public String name() {
        return service.name();
    }

    /**
     * @return Base path of all endpoints provided by this service, if any.
     */
    public Optional<String> basePath() {
        return Optional.ofNullable(basePath);
    }

    /**
     * @return Service access policy.
     */
    public AccessPolicy accessPolicy() {
        return accessPolicy;
    }

    /**
     * @return The codec to use by default.
     */
    public CodecType defaultCodecType() {
        return codecTypes.get(0);
    }

    /**
     * @return Data codecs supported by this service.
     */
    public List<CodecType> codecs() {
        return codecTypes;
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
        if (logger.isTraceEnabled()) {
            logger.trace("About to handle (basePath: {}) {}", basePath, request);
        }

        final var task = new HttpRouteTask.Builder()
            .basePath(basePath)
            .request(request)
            .response(response)
            .build();

        return trySequences(task, 0)
            .map(isHandled -> {
                if (!isHandled) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("No route sequence of service {} matched (basePath: {}) {}", name(), basePath, request);
                    }
                    response
                        .status(HttpStatus.NOT_FOUND)
                        .clearHeaders()
                        .clearBody();
                }
                return null;
            });
    }

    private Future<Boolean> trySequences(final HttpRouteTask task, final int index) {
        if (index >= routeSequences.length) {
            return Future.success(false);
        }
        final var routeSequence = routeSequences[index];
        if (logger.isTraceEnabled()) {
            logger.trace("Attempting sequence {}", routeSequence);
        }
        return routeSequence.tryHandle(task)
            .flatMap(isHandled -> {
                if (isHandled) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Matched {}", routeSequence);
                    }
                    return Future.success(true);
                }
                else if (logger.isTraceEnabled()) {
                    logger.trace("Failed to match {}", routeSequence);
                }
                return trySequences(task, index + 1);
            });
    }

    public ServiceRecord description() {
        return service.describeAsIfProvidedBy(provider);
    }
}
