package se.arkalix.core.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.ArService;
import se.arkalix.core.plugin.dto.ErrorDto;
import se.arkalix.core.plugin.dto.ServiceQueryBuilder;
import se.arkalix.core.plugin.dto.ServiceRegistrationBuilder;
import se.arkalix.core.plugin.dto.SystemDetailsBuilder;
import se.arkalix.description.ServiceDescription;
import se.arkalix.internal.security.identity.X509Keys;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.client.HttpClientResponseRejectedException;
import se.arkalix.plugin.Plug;
import se.arkalix.plugin.Plugin;
import se.arkalix.query.ServiceQuery;
import se.arkalix.security.access.AccessByToken;
import se.arkalix.util.Result;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.concurrent.FutureAnnouncement;

import java.net.InetSocketAddress;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.Objects;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * HTTP/JSON core integration plug-in.
 * <p>
 * This class helps one or more {@link se.arkalix.ArSystem systems} with
 * joining a local cloud by communicating with the mandatory Arrowhead core
 * services of that cloud using HTTP and JSON. More precisely, it (1) registers
 * and unregisters the {@link se.arkalix.ArSystem#provide(ArService) services
 * provided} by its systems, (2) retrieves the public key required to {@link
 * se.arkalix.security.access.AccessByToken validate consumer tokens}, as well
 * as (3) helps resolve {@link se.arkalix.ArSystem#consume(String) service
 * consumption queries}.
 */
public class HttpJsonCoreIntegrator implements Plugin {
    private static final Logger logger = LoggerFactory.getLogger(HttpJsonCoreIntegrator.class);

    private final String serviceRegistryBasePath;
    private final InetSocketAddress serviceRegistrySocketAddress;

    private HttpClient client = null;
    private HttpJsonServiceDiscovery serviceRegistry = null;
    private FutureAnnouncement<PublicKey> authorizationKeyAnnouncement = null;

    private HttpJsonCoreIntegrator(
        final InetSocketAddress serviceRegistrySocketAddress,
        final String serviceRegistryBasePath)
    {
        this.serviceRegistryBasePath = serviceRegistryBasePath;
        this.serviceRegistrySocketAddress = serviceRegistrySocketAddress;
    }

    /**
     * Creates new HTTP/JSON core service integrator that tries to enter a
     * local cloud using the service registry system at the specified {@code
     * socketAddress} as starting point.
     * <p>
     * The service URI, or base path, of the service registry system is assumed
     * to be {@code "/serviceregistry"}.
     *
     * @param socketAddress IP address or hostname and port of service registry
     *                      system to use for entering local cloud.
     * @return New core integrator.
     */
    public static HttpJsonCoreIntegrator enterViaServiceRegistry(final InetSocketAddress socketAddress) {
        return enterViaServiceRegistry(socketAddress, "/serviceregistry");
    }

    /**
     * Creates new HTTP/JSON core service integrator that tries to enter a
     * local cloud using the service registry system at the specified {@code
     * socketAddress} as starting point.
     * <p>
     * If the service URI, or base path, of the service registry system is not
     * known, please use {@link #enterViaServiceRegistry(InetSocketAddress)}.
     *
     * @param socketAddress IP address or hostname and port of service registry
     *                      system to use for entering local cloud.
     * @param basePath      The base path that HTTP request URIs must be
     *                      prefixed with in order for them to be received by
     *                      the service discovery service of the service
     *                      registry system available via {@code
     *                      socketAddress}.
     * @return New core integrator.
     */
    public static HttpJsonCoreIntegrator enterViaServiceRegistry(
        final InetSocketAddress socketAddress,
        final String basePath)
    {
        Objects.requireNonNull(socketAddress, "Expected socketAddress");
        return new HttpJsonCoreIntegrator(socketAddress, basePath);
    }

    @Override
    public void onAttach(final Plug plug) throws Exception {
        client = HttpClient.from(plug.system());

        serviceRegistry = new HttpJsonServiceDiscovery.Builder()
            .basePath(serviceRegistryBasePath)
            .client(client)
            .remoteSocketAddress(serviceRegistrySocketAddress)
            .build();

        if (logger.isInfoEnabled()) {
            logger.info("HTTP/JSON core integrator attached to \"{}\"", plug.system().name());
        }
    }

    @Override
    public void onDetach(final Plug plug) {
        if (logger.isInfoEnabled()) {
            logger.info("HTTP/JSON core integrator detached from \"{}\"", plug.system().name());
        }
    }

    @Override
    public void onDetach(final Plug plug, final Throwable cause) {
        if (logger.isErrorEnabled()) {
            logger.error("HTTP/JSON core integrator forcibly detached " +
                "from \"" + plug.system().name() + "\"", cause);
        }
    }

    @Override
    public Future<?> onServicePrepared(final Plug plug, final ArService service) {
        final var accessPolicy = service.accessPolicy();
        if (accessPolicy instanceof AccessByToken) {
            synchronized (this) {
                if (authorizationKeyAnnouncement == null) {
                    authorizationKeyAnnouncement = requestAuthorizationKey();
                }
            }
            return authorizationKeyAnnouncement.subscribe()
                .map(authorizationKey -> {
                    ((AccessByToken) accessPolicy).authorizationKey(authorizationKey);
                    return null;
                });
        }
        return Future.done();
    }

    private FutureAnnouncement<PublicKey> requestAuthorizationKey() {
        if (logger.isInfoEnabled()) {
            logger.info("HTTP/JSON core integrator requesting authorization key ...");
        }
        return serviceRegistry.query(new ServiceQueryBuilder()
            .name("auth-public-key")
            .build())
            .mapResult(result -> {
                if (result.isSuccess()) {
                    final var services = result.value().services();
                    if (services.size() == 0) {
                        return Result.failure(new ArCoreIntegrationException("" +
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
                        return Result.failure(new ArCoreIntegrationException("" +
                            "Even though the service registry provided " +
                            "descriptions for " + services.size() + " " +
                            "\"auth-public-key\" service(s), none of them " +
                            "contains an authorization system public key; " +
                            "token authorization not possible"));
                    }

                    final var publicKeyDer = Base64.getDecoder().decode(publicKeyBase64);
                    final var algorithm = X509Keys.identifyPublicKeyAlgorithm(publicKeyDer);
                    if (algorithm.isEmpty()) {
                        return Result.failure(new ArCoreIntegrationException("" +
                            "The \"auth-public-key\" service provider public " +
                            "key seems to use an unsupported key algorithm; " +
                            "token authorization not possible"));
                    }
                    final var keySpec = new X509EncodedKeySpec(publicKeyDer);
                    final var keyFactory = KeyFactory.getInstance(algorithm.get());
                    final var publicKey = keyFactory.generatePublic(keySpec);

                    if (logger.isInfoEnabled()) {
                        logger.info("Authorization key retrieved: {}", publicKeyBase64);
                    }

                    return Result.success(publicKey);
                }
                return Result.failure(result.fault());
            })
            .mapFault(Throwable.class, fault -> {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to retrieve authorization key", fault);
                }
                return fault;
            })
            .toAnnouncement();
    }

    @Override
    public Future<?> onServiceProvided(final Plug plug, final ServiceDescription service) {
        if (logger.isInfoEnabled()) {
            logger.info("System \"{}\" is now registering \"{}\" ...", plug.system().name(), service.name());
        }
        final var provider = service.provider();
        final var providerSocketAddress = provider.socketAddress();
        final var registration = new ServiceRegistrationBuilder()
            .name(service.name())
            .provider(new SystemDetailsBuilder()
                .name(provider.name())
                .hostname(providerSocketAddress.getHostString())
                .port(providerSocketAddress.getPort())
                .publicKeyBase64(provider.isSecure()
                    ? Base64.getEncoder().encodeToString(provider.identity().publicKey().getEncoded())
                    : null)
                .build())
            .uri(service.uri())
            .security(service.security())
            .metadata(service.metadata())
            .version(service.version())
            .interfaces(service.interfaces())
            .build();

        return serviceRegistry.register(registration)
            .pass(null)
            .flatMapCatch(HttpClientResponseRejectedException.class, fault -> {
                if (fault.status() == HttpStatus.BAD_REQUEST) {
                    return fault.unwrap()
                        .bodyAs(JSON, ErrorDto.class)
                        .flatMap(error -> {
                            if (!"INVALID_PARAMETER".equals(error.type())) {
                                return Future.done();
                            }
                            return serviceRegistry.unregister(
                                service.name(),
                                provider.name(),
                                providerSocketAddress.getHostString(),
                                providerSocketAddress.getPort())
                                .flatMap(ignored -> serviceRegistry.register(registration).pass(null));
                        });
                }
                return Future.failure(fault);
            })
            .mapResult(result -> {
                if (result.isSuccess()) {
                    if (logger.isInfoEnabled()) {
                        logger.info("System \"{}\" has registered \"{}\"", plug.system().name(), service.name());
                    }
                }
                else {
                    if (logger.isErrorEnabled()) {
                        logger.error("System \"" + plug.system().name() +
                            "\" failed to register \"" + service.name() +
                            "\"", result.fault());
                    }
                }
                return result;
            });
    }

    @Override
    public void onServiceDismissed(final Plug plug, final ServiceDescription service) {
        if (logger.isInfoEnabled()) {
            logger.info("System \"{}\" is now unregistering \"{}\" ...", plug.system().name(), service.name());
        }
        final var provider = service.provider();
        final var providerSocketAddress = provider.socketAddress();
        serviceRegistry.unregister(
            service.name(),
            provider.name(),
            providerSocketAddress.getHostString(),
            providerSocketAddress.getPort())
            .onResult(result -> {
                if (result.isSuccess()) {
                    if (logger.isInfoEnabled()) {
                        logger.info("System \"{}\" has unregistered \"{}\"", plug.system().name(), service.name());
                    }
                }
                else {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Failed to unregister service \"" + service.name() + "\"", result.fault());
                    }
                }
            });
    }

    @Override
    public Future<Collection<ServiceDescription>> onServiceQueried(final Plug plug, final ServiceQuery query) {
        throw new UnsupportedOperationException(); // TODO_ Orchestrator
    }
}
