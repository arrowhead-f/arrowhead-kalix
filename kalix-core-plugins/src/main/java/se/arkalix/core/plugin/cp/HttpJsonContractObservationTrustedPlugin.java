package se.arkalix.core.plugin.cp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.ArSystem;
import se.arkalix.core.plugin.eh.ArEventSubscriberPluginFacade;
import se.arkalix.core.plugin.eh.ArEventSubscriptionHandle;
import se.arkalix.core.plugin.eh.HttpJsonEventSubscriberPlugin;
import se.arkalix.plugin.Plugin;
import se.arkalix.plugin.PluginAttached;
import se.arkalix.plugin.PluginFacade;
import se.arkalix.util.concurrent.Future;

import java.util.*;

@SuppressWarnings("unused")
public class HttpJsonContractObservationTrustedPlugin implements Plugin {
    private static final Logger logger = LoggerFactory.getLogger(HttpJsonContractObservationTrustedPlugin.class);

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
            .pass(attached);
    }

    private static class Attached implements PluginAttached {
        private final Facade facade = new Facade();
        private final ArSystem system;
        private final ArEventSubscriberPluginFacade eventSubscriber;
        private final Set<ArTrustedNegotiationObserver> observers = Collections.synchronizedSet(new HashSet<>());

        private ArEventSubscriptionHandle eventSubscriptionHandle = null;

        private Attached(final ArSystem system, final ArEventSubscriberPluginFacade eventSubscriber) {
            this.system = Objects.requireNonNull(system, "Expected system");
            this.eventSubscriber = Objects.requireNonNull(eventSubscriber, "Expected eventSubscriber");
        }

        public Future<?> subscribe() {
            return eventSubscriber
                .subscribe(ArContractProxyConstants.TOPIC_SESSION_UPDATE, (metadata, data) -> {
                    final long sessionId;
                    try {
                        final var colonIndex = data.indexOf(':');
                        if (colonIndex == -1) {
                            throw new IllegalStateException("Expected event " +
                                "data to consist of two colon-separated " +
                                "numbers (<sessionId>:<candidateSeq>); no " +
                                "colon (:) found in data");
                        }
                        sessionId = Long.parseLong(data, 0, colonIndex, 10);
                    }
                    catch (final Throwable throwable) {
                        logger.warn("HTTP/JSON contract observer received " +
                            "contract event with invalid session and " +
                            "candidate identifiers; cannot process event " +
                            "[data=" + data + ", metadata=" + metadata +
                            "]", throwable);
                        return;
                    }

                    final var offerorName = metadata.get("offeror");
                    if (offerorName == null) {
                        logger.warn("HTTP/JSON contract observer received " +
                            "contract event without a named offeror; " +
                            "cannot process event [sessionId={}, metadata={}]", data, metadata);
                        return;
                    }

                    final var receiverName = metadata.get("receiver");
                    if (receiverName == null) {
                        logger.warn("HTTP/JSON contract observer received " +
                            "contract event without a named receiver; " +
                            "cannot process event [sessionId={}, metadata={}]", data, metadata);
                        return;
                    }

                    system.consume()
                        .using(HttpJsonContractNegotiationTrustedSession.factory())
                        .flatMap(service -> service.getByNamesAndId(offerorName, receiverName, sessionId))
                        .ifSuccess(session -> {
                            final var status = session.status();
                            final var candidate = session.candidate();
                            for (final var observer : observers) {
                                try {
                                    observer.onUpdate(status, candidate);
                                }
                                catch (final Throwable throwable) {
                                    logger.error("HTTP/JSON contract " +
                                        "observer unexpectedly caught " +
                                        "exception thrown by contract event " +
                                        "subscriber " + observer, throwable);
                                }
                            }
                        });
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
        }

        private class Facade implements ArContractObservationTrustedPluginFacade {
            @Override
            public void observe(final ArTrustedNegotiationObserver observer) {
                observers.add(observer);
            }
        }
    }
}