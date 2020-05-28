package se.arkalix.core.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.ArService;
import se.arkalix.ArSystem;
import se.arkalix.core.plugin.or.HttpJsonOrchestrationService;
import se.arkalix.core.plugin.or.OrchestrationStrategy;
import se.arkalix.core.plugin.sr.HttpJsonServiceDiscoveryService;
import se.arkalix.core.plugin.sr.ServiceQueryBuilder;
import se.arkalix.core.plugin.sr.ServiceRegistration;
import se.arkalix.description.ProviderDescription;
import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.descriptor.InterfaceDescriptor;
import se.arkalix.internal.security.identity.X509Keys;
import se.arkalix.util.concurrent.*;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.consumer.HttpConsumer;
import se.arkalix.plugin.Plugin;
import se.arkalix.plugin.PluginAttached;
import se.arkalix.plugin.PluginFacade;
import se.arkalix.query.ServiceQuery;
import se.arkalix.security.access.AccessByToken;
import se.arkalix.security.identity.SystemIdentity;
import se.arkalix.security.identity.UnsupportedKeyAlgorithm;
import se.arkalix.util.Result;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

import static se.arkalix.descriptor.SecurityDescriptor.CERTIFICATE;
import static se.arkalix.descriptor.SecurityDescriptor.NOT_SECURE;
import static se.arkalix.descriptor.TransportDescriptor.HTTP;

/**
 * HTTP/JSON cloud plugin.
 * <p>
 * This class helps one {@link se.arkalix.ArSystem system} to join a local
 * cloud by communicating with the mandatory Arrowhead core services of that
 * cloud using HTTP and JSON. More precisely, it (1) registers and unregisters
 * the {@link se.arkalix.ArSystem#provide(ArService) services provided} by its
 * system, (2) retrieves the public key required to {@link
 * se.arkalix.security.access.AccessByToken validate consumer tokens}, as well
 * as (3) helps resolve {@link se.arkalix.ArSystem#consume() service
 * consumption queries}.
 * <p>
 * Note that the plugin currently assumes that the service registry,
 * authorization system public key and orchestrator of the cloud in question
 * will never change. It also never adds the service discovery or orchestration
 * services to the {@link ArSystem#consumedServices() service caches} of the
 * systems using them, making them inaccessible via the
 * {@link ArSystem#consume()} method.
 */
public class HttpJsonCloudPlugin implements Plugin {
    private static final Logger logger = LoggerFactory.getLogger(HttpJsonCloudPlugin.class);

    private final InetSocketAddress serviceRegistrySocketAddress;
    private final String serviceDiscoveryBasePath;
    private final OrchestrationStrategy orchestrationStrategy;

    private HttpJsonCloudPlugin(final Builder builder) {
        serviceDiscoveryBasePath = Objects.requireNonNullElse(builder.serviceDiscoveryBasePath, "/serviceregistry");
        serviceRegistrySocketAddress = Objects.requireNonNull(builder.serviceRegistrySocketAddress,
            "Expected serviceRegistrySocketAddress");
        orchestrationStrategy = Objects.requireNonNullElse(builder.orchestrationStrategy,
            OrchestrationStrategy.STORED_ONLY);
    }

    /**
     * Creates new HTTP/JSON cloud plugin that tries to make its systems enter
     * a certain local cloud via the service registry system available at the
     * specified {@code socketAddress}.
     * <p>
     * If more control over the behavior of the core integrator is desired,
     * please use the {@link Builder builder class} instead.
     *
     * @param socketAddress IP address or hostname and port of service registry
     *                      system to use for entering local cloud.
     * @return New core integrator.
     */
    public static HttpJsonCloudPlugin viaServiceRegistryAt(final InetSocketAddress socketAddress) {
        return new HttpJsonCloudPlugin.Builder()
            .serviceRegistrySocketAddress(socketAddress)
            .build();
    }

    @Override
    public int ordinal() {
        return -1000;
    }

    @Override
    public Future<PluginAttached> attachTo(
        final ArSystem system,
        final Map<Class<? extends Plugin>, PluginFacade> dependencies)
        throws SSLException
    {
        return Future.success(new Attached(system));
    }

    private class Attached implements PluginAttached {
        private final ArSystem system;
        private final SystemDetailsDto systemDetails;
        private final HttpClient client;

        private FutureAnnouncement<HttpJsonServiceDiscoveryService> serviceDiscoveryAnnouncement = null;
        private FutureAnnouncement<HttpJsonOrchestrationService> orchestrationAnnouncement = null;
        private FutureAnnouncement<PublicKey> authorizationKeyAnnouncement = null;

        Attached(final ArSystem system) throws SSLException {
            this.system = Objects.requireNonNull(system, "Expected system");
            this.systemDetails = SystemDetails.from(system);
            this.client = HttpClient.from(system);

            if (logger.isInfoEnabled()) {
                logger.info("HTTP/JSON cloud plugin attached to \"{}\"", system.name());
            }
        }

        @Override
        public void onDetach() {
            if (logger.isInfoEnabled()) {
                logger.info("HTTP/JSON cloud plugin detached from \"{}\"", system.name());
            }
        }

        @Override
        public void onDetach(final Throwable cause) {
            if (logger.isErrorEnabled()) {
                logger.error("HTTP/JSON cloud plugin forcibly detached " +
                    "from \"" + system.name() + "\"", cause);
            }
        }

        @Override
        public Future<?> onServicePrepared(final ArService service) {
            final var accessPolicy = service.accessPolicy();
            if (accessPolicy instanceof AccessByToken) {
                 return requestAuthorizationKey()
                    .ifSuccess(((AccessByToken) accessPolicy)::authorizationKey);
            }
            return Future.done();
        }

        @Override
        public Future<?> onServiceProvided(final ServiceDescription service) {
            if (logger.isInfoEnabled()) {
                logger.info("HTTP/JSON cloud plugin registering \"{}\" " +
                    "provided by \"{}\" ...", service.name(), system.name());
            }
            final var provider = service.provider();
            final var providerSocketAddress = provider.socketAddress();
            final var registration = ServiceRegistration.from(service);

            return requestServiceDiscovery()
                .flatMap(serviceDiscovery -> serviceDiscovery
                    .register(registration)
                    .flatMapCatch(ErrorResponseException.class, fault -> {
                        final var error = fault.error();
                        if ("INVALID_PARAMETER".equals(error.type())) {
                            return serviceDiscovery.unregister(
                                service.name(),
                                provider.name(),
                                providerSocketAddress.getHostString(),
                                providerSocketAddress.getPort())
                                .flatMap(ignored -> serviceDiscovery.register(registration)
                                    .pass(null));
                        }
                        return Future.failure(fault);
                    })
                    .mapResult(result -> {
                        if (result.isSuccess()) {
                            if (logger.isInfoEnabled()) {
                                logger.info("HTTP/JSON cloud plugin " +
                                        "registered the \"{}\" service " +
                                        "provided by the \"{}\" system",
                                    service.name(), system.name());
                            }
                        }
                        else {
                            if (logger.isErrorEnabled()) {
                                logger.error("HTTP/JSON cloud plugin failed " +
                                    "to register the \"" + service.name() +
                                    "\" service provided by the \"" + system.name() +
                                    "\" system", result.fault());
                            }
                        }
                        return result;
                    }));
        }

        @Override
        public void onServiceDismissed(final ServiceDescription service) {
            if (logger.isInfoEnabled()) {
                logger.info("HTTP/JSON cloud plugin unregistering the \"{}\"" +
                    "service provided by the \"{}\" system ...", service.name(), system.name());
            }
            final var provider = service.provider();
            final var providerSocketAddress = provider.socketAddress();
            requestServiceDiscovery()
                .flatMap(serviceDiscovery -> serviceDiscovery.unregister(
                    service.name(),
                    provider.name(),
                    providerSocketAddress.getHostString(),
                    providerSocketAddress.getPort()))
                .onResult(result -> {
                    if (result.isSuccess()) {
                        if (logger.isInfoEnabled()) {
                            logger.info("HTTP/JSON cloud plugin " +
                                "unregistered the \"{}\" service " +
                                "provided by the \"{}\" system", service.name(), system.name());
                        }
                    }
                    else {
                        if (logger.isWarnEnabled()) {
                            logger.warn("HTTP/JSON cloud plugin failed to " +
                                "unregister the \"" + service.name() +
                                "\" service provided by the \"" + system.name() +
                                "\" system", result.fault());
                        }
                    }
                });
        }

        @Override
        public Future<Collection<ServiceDescription>> onServiceQueried(final ServiceQuery query) {
            return Futures.flatReducePlain(
                orchestrationStrategy.requests(),
                new ArrayList<>(),
                (services, request) -> {
                    if (services.stream().anyMatch(query::matches)) {
                        return Future.success(services);
                    }
                    return requestOrchestration()
                        .flatMap(orchestration -> orchestration.query(request.toQuery(systemDetails, query)))
                        .map(queryResult -> queryResult.services()
                            .stream()
                            .map(ServiceConsumable::toServiceDescription)
                            .collect(Collectors.toUnmodifiableList()));
                });
        }

        private synchronized Future<HttpJsonServiceDiscoveryService> requestServiceDiscovery() {
            if (serviceDiscoveryAnnouncement == null) {
                if (logger.isInfoEnabled()) {
                    logger.info("HTTP/JSON cloud plugin connecting to " +
                        "\"service_registry\" system at {} ...", serviceRegistrySocketAddress);
                }
                serviceDiscoveryAnnouncement = client.connect(serviceRegistrySocketAddress)
                    .mapResult(result -> {
                        if (result.isFailure()) {
                            return Result.failure(result.fault());
                        }
                        final var connection = result.value();
                        final var isSecure = connection.isSecure();
                        if (isSecure != system.isSecure()) {
                            return Result.failure(new CloudException("" +
                                "HTTP/JSON cloud plugin connected to system " +
                                "at " + serviceRegistrySocketAddress +
                                " and found that it is " + (isSecure
                                ? "running in secure mode, while this system is not"
                                : "not running in secure mode, while this system is")
                                + "; failed to resolve service discovery service "));
                        }
                        final ProviderDescription provider;
                        if (isSecure) {
                            final var identity = new SystemIdentity(connection.certificateChain());
                            final var name = identity.name();
                            if (!Objects.equals(name, "service_registry")) {
                                return Result.failure(new CloudException("" +
                                    "HTTP/JSON cloud plugin connected to " +
                                    "system at " + serviceRegistrySocketAddress +
                                    " and found that its certificate name " +
                                    "is \"" + name + "\" while expecting it " +
                                    "to be \"service_registry\"; failed to " +
                                    "resolve service discovery service "));
                            }
                            provider = new ProviderDescription(name, serviceRegistrySocketAddress, identity.publicKey());
                        }
                        else {
                            provider = new ProviderDescription("service_registry", serviceRegistrySocketAddress);
                        }

                        final var serviceDiscovery = new HttpJsonServiceDiscoveryService(client,
                            new ServiceDescription.Builder()
                                .name("service-discovery")
                                .provider(provider)
                                .uri(serviceDiscoveryBasePath)
                                .security(isSecure ? CERTIFICATE : NOT_SECURE)
                                .interfaces(InterfaceDescriptor.getOrCreate(HTTP, isSecure, EncodingDescriptor.JSON))
                                .build());

                        connection.close();

                        if (logger.isInfoEnabled()) {
                            logger.info("HTTP/JSON cloud plugin " +
                                "connected to \"service_registry\" system " +
                                "at {}", serviceRegistrySocketAddress);
                        }

                        return Result.success(serviceDiscovery);
                    })
                    .ifFailure(Throwable.class, fault -> {
                        if (logger.isErrorEnabled()) {
                            logger.error("HTTP/JSON cloud plugin failed to " +
                                "connect to \"service_registry\" system at " +
                                serviceRegistrySocketAddress, fault);
                        }
                    })
                    .toAnnouncement();
            }
            return serviceDiscoveryAnnouncement.subscribe();
        }

        private synchronized Future<PublicKey> requestAuthorizationKey() {
            if (authorizationKeyAnnouncement == null) {
                if (logger.isInfoEnabled()) {
                    logger.info("HTTP/JSON cloud plugin requesting authorization key ...");
                }
                authorizationKeyAnnouncement = requestServiceDiscovery()
                    .flatMap(serviceDiscovery -> serviceDiscovery.query(new ServiceQueryBuilder()
                        .name("auth-public-key")
                        .build()))
                    .mapResult(result -> {
                        if (result.isFailure()) {
                            return Result.failure(result.fault());
                        }
                        final var services = result.value().services();
                        if (services.size() == 0) {
                            return Result.failure(new CloudException("" +
                                "No \"auth-public-key\" service seems to be " +
                                "available via the service registry at: " +
                                serviceRegistrySocketAddress + "; token " +
                                "authorization not possible"));
                        }

                        String publicKeyBase64 = null;
                        for (final var service : services) {
                            final var key = service.provider().publicKeyBase64();
                            if (key.isPresent()) {
                                publicKeyBase64 = key.get();
                                break;
                            }
                        }
                        if (publicKeyBase64 == null) {
                            return Result.failure(new CloudException("" +
                                "Even though the service registry provided " +
                                "descriptions for " + services.size() + " " +
                                "\"auth-public-key\" service(s), none of them " +
                                "contains an authorization system public key; " +
                                "token authorization not possible"));
                        }

                        final PublicKey publicKey;
                        try {
                            publicKey = X509Keys.parsePublicKey(publicKeyBase64);
                        }
                        catch (final UnsupportedKeyAlgorithm exception) {
                            return Result.failure(new CloudException("" +
                                "The \"auth-public-key\" service provider public " +
                                "key seems to use an unsupported key algorithm; " +
                                "token authorization not possible", exception));
                        }

                        if (logger.isInfoEnabled()) {
                            logger.info("Authorization key retrieved: {}", publicKeyBase64);
                        }

                        return Result.success(publicKey);
                    })
                    .ifFailure(Throwable.class, fault -> {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Failed to retrieve authorization key", fault);
                        }
                    })
                    .toAnnouncement();
            }
            return authorizationKeyAnnouncement.subscribe();
        }

        private synchronized Future<HttpJsonOrchestrationService> requestOrchestration() {
            if (orchestrationAnnouncement == null) {
                if (logger.isInfoEnabled()) {
                    logger.info("HTTP/JSON cloud plugin connecting to " +
                        "\"orchestrator\" system ...");
                }
                final var isSecure = client.isSecure();
                orchestrationAnnouncement = requestServiceDiscovery()
                    .flatMap(serviceDiscovery -> serviceDiscovery.query(new ServiceQueryBuilder()
                        .name("orchestration-service")
                        .interfaces(InterfaceDescriptor.getOrCreate(HTTP, isSecure, EncodingDescriptor.JSON))
                        .securityModes(isSecure ? CERTIFICATE : NOT_SECURE)
                        .build()))
                    .flatMapResult(result -> {
                        if (result.isFailure()) {
                            return Future.failure(result.fault());
                        }
                        final var queryResult = result.value();
                        final var services = queryResult.services();
                        if (services.isEmpty()) {
                            return Future.failure(new CloudException("" +
                                "No orchestration service available; cannot " +
                                "request orchestration rules"));
                        }
                        final var orchestration = new HttpJsonOrchestrationService(new HttpConsumer(
                            client,
                            services.get(0).toServiceDescription(),
                            Collections.singleton(EncodingDescriptor.JSON)));

                        if (logger.isInfoEnabled()) {
                            logger.info("HTTP/JSON cloud plugin resolved " +
                                    "orchestration service at {}",
                                orchestration.service().provider().socketAddress());
                        }

                        return Future.success(orchestration);
                    })
                    .ifFailure(Throwable.class, fault -> {
                        if (logger.isErrorEnabled()) {
                            logger.error("HTTP/JSON cloud plugin failed " +
                                "to connect to \"orchestrator\" system", fault);
                        }
                    })
                    .toAnnouncement();
            }
            return orchestrationAnnouncement.subscribe();
        }
    }

    /**
     * Builder useful for constructing {@link HttpJsonCloudPlugin} instances.
     */
    @SuppressWarnings("unused")
    public static class Builder {
        private String serviceDiscoveryBasePath;
        private InetSocketAddress serviceRegistrySocketAddress;
        private OrchestrationStrategy orchestrationStrategy;

        /**
         * Sets base path, or <i>service URI</i>, of the service discovery
         * service provided by {@link
         * #serviceRegistrySocketAddress(InetSocketAddress)} the designated
         * service registry system}. If not specified, a default that should
         * work with most service registries will be used.
         *
         * @param serviceDiscoveryBasePath Base path of service discovery
         *                                 service.
         * @return This builder.
         */
        public Builder serviceDiscoveryBasePath(final String serviceDiscoveryBasePath) {
            this.serviceDiscoveryBasePath = serviceDiscoveryBasePath;
            return this;
        }

        /**
         * Sets hostname/IP-address and port of the service registry system to
         * use for entering into an Arrowhead local cloud. <b>Must be
         * specified.</b>
         *
         * @param serviceRegistrySocketAddress Service registry system socket
         *                                     address.
         * @return This builder.
         */
        public Builder serviceRegistrySocketAddress(final InetSocketAddress serviceRegistrySocketAddress) {
            this.serviceRegistrySocketAddress = serviceRegistrySocketAddress;
            return this;
        }

        /**
         * Sets {@link OrchestrationStrategy orchestration strategy} to use
         * when {@link se.arkalix.ArSystem#consume() resolving what services}
         * to consume.
         *
         * @param orchestrationStrategy Desired orchestration strategy.
         * @return This builder.
         */
        public Builder orchestrationStrategy(final OrchestrationStrategy orchestrationStrategy) {
            this.orchestrationStrategy = orchestrationStrategy;
            return this;
        }

        /**
         * @return New {@link HttpJsonCloudPlugin}.
         */
        public HttpJsonCloudPlugin build() {
            return new HttpJsonCloudPlugin(this);
        }
    }
}
