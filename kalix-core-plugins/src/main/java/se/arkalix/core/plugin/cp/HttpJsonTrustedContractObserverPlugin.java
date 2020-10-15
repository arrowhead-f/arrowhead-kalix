package se.arkalix.core.plugin.cp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.ArSystem;
import se.arkalix.core.plugin.eh.ArEventSubscriberPluginFacade;
import se.arkalix.core.plugin.eh.EventSubscriptionHandle;
import se.arkalix.core.plugin.eh.HttpJsonEventSubscriberPlugin;
import se.arkalix.plugin.Plugin;
import se.arkalix.plugin.PluginAttached;
import se.arkalix.plugin.PluginFacade;
import se.arkalix.util.concurrent.Future;

import java.util.*;

/**
 * A HTTP/JSON {@link Plugin plugin} that observes contract negotiation updates.
 * <p>
 * The plugin can be used as in following example:
 * <pre>
 *     // Provide plugin to created system.
 *     final var system = new ArSystem.Builder()
 *         .identity(identity)
 *         .trustStore(trustStore)
 *         .plugins(HttpJsonCloudPlugin.viaServiceRegistryAt(srSocketAddress),
 *             new HttpJsonContractObservationTrustedPlugin())
 *         .build();
 *
 *     // Collect the plugin's facade.
 *     final var observer = system.pluginFacadeOf(HttpJsonContractObservationTrustedPlugin.class)
 *         .map(facade -&gt; (ArContractObservationTrustedPluginFacade) facade)
 *         .orElseThrow(() -&gt; new IllegalStateException("Contract negotiation observation facade not available"));
 *
 *     // React to observed events.
 *     observer.observe(session -&gt; System.out.println(session));
 * </pre>
 * Use of this plugin requires that another plugin is available that performs
 * service resolution, such as the {@link
 * se.arkalix.core.plugin.HttpJsonCloudPlugin HttpJsonCloudPlugin}.
 */
@SuppressWarnings("unused")
public class HttpJsonTrustedContractObserverPlugin implements ArTrustedContractObserverPlugin {
    private static final Logger logger = LoggerFactory.getLogger(HttpJsonTrustedContractObserverPlugin.class);

    @Override
    public Set<Class<? extends Plugin>> dependencies() {
        return Collections.singleton(HttpJsonEventSubscriberPlugin.class);
    }

    @Override
    public Future<PluginAttached> attachTo(
        final ArSystem system,
        final Map<Class<? extends Plugin>, PluginFacade> dependencies)
    {
        final var eventSubscriber = dependencies.get(HttpJsonEventSubscriberPlugin.class);
        if (eventSubscriber == null) {
            throw new IllegalStateException("Expected HttpJsonEventSubscriberPlugin to provide plugin facade");
        }
        final var attached = new Attached(system, (ArEventSubscriberPluginFacade) eventSubscriber);
        return attached.subscribe()
            .ifSuccess(ignored -> {
                if (logger.isInfoEnabled()) {
                    logger.info("HTTP/JSON contract observer plugin attached to \"{}\"", system.name());
                }
            })
            .ifFailure(Throwable.class, throwable -> {
                if (logger.isErrorEnabled()) {
                    logger.error("HTTP/JSON contract observer plugin " +
                        "failed to attached to \"" + system.name() + "\"", throwable);
                }
            })
            .pass(attached);
    }

    private static class Attached implements PluginAttached {
        private final Facade facade = new Facade();
        private final ArSystem system;
        private final ArEventSubscriberPluginFacade eventSubscriber;
        private final Set<TrustedContractObserverHandler> observers = Collections.synchronizedSet(new HashSet<>());

        private EventSubscriptionHandle eventSubscriptionHandle = null;

        private Attached(final ArSystem system, final ArEventSubscriberPluginFacade eventSubscriber) {
            this.system = Objects.requireNonNull(system, "Expected system");
            this.eventSubscriber = Objects.requireNonNull(eventSubscriber, "Expected eventSubscriber");
        }

        public Future<?> subscribe() {
            return eventSubscriber
                .subscribe(ContractNegotiationConstants.TOPIC_UPDATE, (metadata, data) -> {
                    final long negotiationId;
                    try {
                        negotiationId = Long.parseLong(data);
                    }
                    catch (final Throwable throwable) {
                        logger.warn("HTTP/JSON contract negotiator received " +
                            "contract event with an invalid session " +
                            "identifier; cannot process event [data=" + data +
                            ", metadata=" + metadata + "]", throwable);
                        return;
                    }

                    final var offerorName = metadata.get("offeror");
                    if (offerorName == null) {
                        logger.warn("HTTP/JSON contract negotiator received " +
                            "contract event without a named offeror; " +
                            "cannot process event [data={}, metadata={}]", data, metadata);
                        return;
                    }

                    final var receiverName = metadata.get("receiver");
                    if (receiverName == null) {
                        logger.warn("HTTP/JSON contract negotiator received " +
                            "contract event without a named receiver; " +
                            "cannot process event [data={}, metadata={}]", data, metadata);
                        return;
                    }

                    system.consume()
                        .oneUsing(HttpJsonTrustedContractObservationService.factory())
                        .flatMap(service -> service.getByNamesAndId(offerorName, receiverName, negotiationId)
                            .map(optionalSession -> optionalSession.orElseThrow(() -> new IllegalStateException("" +
                                "Advertised session [data=" + data +
                                ", metadata=" + metadata + "] not available " +
                                "via service \"" + service.service().name() +
                                "\"; cannot present session update to " +
                                "negotiation observers"))))
                        .ifSuccess(session -> {
                            for (final var observer : observers) {
                                try {
                                    observer.onUpdate(session);
                                }
                                catch (final Throwable throwable) {
                                    logger.error("HTTP/JSON contract " +
                                        "observer unexpectedly caught " +
                                        "exception thrown by contract event " +
                                        "subscriber " + observer, throwable);
                                }
                            }
                        })
                        .onFailure(throwable -> logger.error("" +
                            "HTTP/JSON contract observer caught unexpected " +
                            "exception while trying to query session update", throwable));
                })
                .ifSuccess(handle -> {
                    synchronized (this) {
                        eventSubscriptionHandle = handle;
                    }
                });
        }

        @Override
        public Optional<PluginFacade> facade() {
            return Optional.of(facade);
        }

        @Override
        public void onDetach() {
            synchronized (this) {
                eventSubscriptionHandle.unsubscribe();
                eventSubscriptionHandle = null;
            }
            if (logger.isInfoEnabled()) {
                logger.info("HTTP/JSON contract observer plugin detached " +
                    "from \"{}\"", system.name());
            }
        }

        @Override
        public void onDetach(final Throwable cause) {
            if (logger.isErrorEnabled()) {
                logger.error("HTTP/JSON contract observer plugin forcibly " +
                    "detached from \"" + system.name() + "\"", cause);
            }
        }

        private class Facade implements ArTrustedContractObserverPluginFacade {
            @Override
            public void observe(final TrustedContractObserverHandler observer) {
                observers.add(observer);
            }
        }
    }
}