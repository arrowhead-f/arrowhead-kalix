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
import se.arkalix.util.concurrent.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A HTTP/JSON {@link Plugin plugin} that helps manage the sending and
 * receiving of contract negotiation messages.
 * <p>
 * The plugin can be used as in following example:
 * <pre>
 *     // Provide plugin to created system.
 *     final var system = new ArSystem.Builder()
 *         .identity(identity)
 *         .trustStore(trustStore)
 *         .plugins(HttpJsonCloudPlugin.viaServiceRegistryAt(srSocketAddress),
 *             new HttpJsonContractNegotiationTrustedPlugin())
 *         .build();
 *
 *     // Collect the plugin's facade.
 *     final var negotiator = system.pluginFacadeOf(HttpJsonContractNegotiationTrustedPlugin.class)
 *         .map(facade -> (ArContractNegotiationTrustedPluginFacade) facade)
 *         .orElseThrow(() -> new IllegalStateException("Negotiator facade not available"));
 *
 *     // Send a negotiation offer to some relevant party and register response handlers.
 *     negotiator.offer(someOffer, new ArTrustedNegotiationHandler() {
 *         &#64;Override
 *         public void onAccept(final TrustedSessionCandidate candidate) {
 *             System.out.println("Accepted " + candidate);
 *         }
 *
 *         &#64;Override
 *         public void onOffer(final TrustedSessionCandidate candidate, final ArTrustedNegotiationResponder responder) {
 *             System.out.println("Received counter-offer " + candidate);
 *             System.out.println("Rejecting counter-offer ...");
 *             responder.reject()
 *                 .ifSuccess(ignored -> System.out.println("Rejected counter-offer"))
 *                 .onFailure(Throwable::printStackTrace);
 *         }
 *
 *         &#64;Override
 *         public void onReject(final TrustedSessionCandidate candidate) {
 *             System.out.println("Rejected " + candidate);
 *         }
 *     });
 * </pre>
 * Use of this plugin requires that another plugin is available that performs
 * service resolution, such as the {@link
 * se.arkalix.core.plugin.HttpJsonCloudPlugin HttpJsonCloudPlugin}.
 */
@SuppressWarnings("unused")
public class HttpJsonContractNegotiationTrustedPlugin implements Plugin {
    private static final Logger logger = LoggerFactory.getLogger(HttpJsonContractNegotiationTrustedPlugin.class);

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
                    logger.info("HTTP/JSON contract negotiator plugin attached to \"{}\"", system.name());
                }
            })
            .ifFailure(Throwable.class, throwable -> {
                if (logger.isErrorEnabled()) {
                    logger.error("HTTP/JSON contract negotiator plugin " +
                        "failed to attached to \"" + system.name() + "\"", throwable);
                }
            })
            .pass(attached);
    }

    private static class Attached implements PluginAttached {
        private final Facade facade = new Facade();
        private final ArSystem system;
        private final ArEventSubscriberPluginFacade eventSubscriber;
        private final Map<HandlerKey, Handler> handlerMap = new ConcurrentHashMap<>();

        private ArEventSubscriptionHandle eventSubscriptionHandle = null;

        private Attached(final ArSystem system, final ArEventSubscriberPluginFacade eventSubscriber) {
            this.system = Objects.requireNonNull(system, "Expected system");
            this.eventSubscriber = Objects.requireNonNull(eventSubscriber, "Expected eventSubscriber");
        }

        public Future<?> subscribe() {
            return eventSubscriber
                .subscribe(ArContractNegotiationConstants.TOPIC_SESSION_UPDATE, (metadata, data) -> {
                    final long sessionId;
                    final long candidateSeq;
                    try {
                        final var colonIndex = data.indexOf(':');
                        if (colonIndex == -1) {
                            throw new IllegalStateException("Expected event " +
                                "data to consist of two colon-separated " +
                                "numbers (<sessionId>:<candidateSeq>); no " +
                                "colon (:) found in data");
                        }
                        sessionId = Long.parseLong(data, 0, colonIndex, 10);
                        candidateSeq = Long.parseLong(data, colonIndex + 1, data.length(), 10);
                    }
                    catch (final Throwable throwable) {
                        logger.warn("HTTP/JSON contract negotiator received " +
                            "contract event with invalid session and " +
                            "candidate identifiers; cannot process event " +
                            "[data=" + data + ", metadata=" + metadata +
                            "]", throwable);
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

                    final var handlerKey = new HandlerKey(offerorName, receiverName, sessionId);
                    final var handler = handlerMap.get(handlerKey);
                    if (handler == null) {
                        logger.trace("HTTP/JSON contract negotiator received " +
                            "contract event identifying session not relevant " +
                            "to this system; ignoring event [data={}, " +
                            "metadata={}]", data, metadata);
                        return;
                    }

                    system.consume()
                        .using(HttpJsonContractNegotiationTrustedSession.factory())
                        .flatMap(service -> service.getByNamesAndId(offerorName, receiverName, sessionId)
                            .map(optionalSession -> optionalSession.orElseThrow(() -> new IllegalStateException("" +
                                "Advertised session [data=" + data +
                                ", metadata=" + metadata + "] not available " +
                                "via service \"" + service.service().name() +
                                "\"; cannot present session update to " +
                                "negotiation handler"))))
                        .ifSuccess(session -> {
                            final var candidate = session.offer();
                            switch (session.status()) {
                            case OFFERING:
                                final var expirationDelay = Duration.between(
                                    candidate.validUntil(),
                                    candidate.validAfter());
                                handler.setExpirationTask(expirationDelay, () -> {
                                    if (handlerMap.remove(handlerKey) != null) {
                                        handler.onExpiry(candidate);
                                    }
                                });

                                handler.onOffer(candidate, new ArTrustedNegotiationResponder() {
                                    @Override
                                    public Future<?> accept() {
                                        handlerMap.remove(handlerKey);
                                        return system.consume()
                                            .using(HttpJsonContractNegotiationTrusted.factory())
                                            .flatMap(service -> service.accept(new TrustedAcceptanceBuilder()
                                                .sessionId(sessionId)
                                                .offerSeq(candidateSeq)
                                                .acceptedAt(Instant.now())
                                                .build()));
                                    }

                                    @Override
                                    public Future<?> offer(final TrustedCounterOffer offer) {
                                        final var expirationDelay = Duration.between(
                                            offer.validUntil(),
                                            offer.validAfter());
                                        handler.setExpirationTask(expirationDelay, () -> {
                                            if (handlerMap.remove(handlerKey) != null) {
                                                handler.onExpiry(candidate);
                                            }
                                        });
                                        return system.consume()
                                            .using(HttpJsonContractNegotiationTrusted.factory())
                                            .flatMap(service -> service.offer(new TrustedOfferBuilder()
                                                .sessionId(sessionId)
                                                .offerSeq(candidateSeq)
                                                .offerorName(offerorName)
                                                .receiverName(receiverName)
                                                .validAfter(offer.validAfter())
                                                .validUntil(offer.validUntil())
                                                .contracts(offer.contracts())
                                                .offeredAt(offer.offeredAt())
                                                .build()));
                                    }

                                    @Override
                                    public Future<?> reject() {
                                        handlerMap.remove(handlerKey);
                                        return system.consume()
                                            .using(HttpJsonContractNegotiationTrusted.factory())
                                            .flatMap(service -> service.reject(new TrustedRejectionBuilder()
                                                .sessionId(sessionId)
                                                .offerSeq(candidateSeq)
                                                .rejectedAt(Instant.now())
                                                .build()));
                                    }
                                });
                                break;

                            case ACCEPTED:
                                handler.onAccept(candidate);
                                break;

                            case REJECTED:
                                handler.onReject(candidate);
                                break;
                            }
                        })
                        .onFailure(handler::onFault);
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
                logger.info("HTTP/JSON contract negotiator plugin detached " +
                    "from \"{}\"", system.name());
            }
        }

        @Override
        public void onDetach(final Throwable cause) {
            if (logger.isErrorEnabled()) {
                logger.error("HTTP/JSON contract negotiator plugin forcibly " +
                    "detached from \"" + system.name() + "\"", cause);
            }
        }

        private class Facade implements ArContractNegotiationTrustedPluginFacade {
            @Override
            public void offer(final TrustedOfferDto offer, final ArTrustedNegotiationHandler handler) {
                system.consume()
                    .using(HttpJsonContractNegotiationTrusted.factory())
                    .flatMap(service -> service.offer(offer))
                    .ifSuccess(ignored -> {
                        final var key = new HandlerKey(offer.offerorName(), offer.receiverName(), offer.sessionId());
                        final var handler0 = new Handler(handler);
                        handlerMap.put(key, handler0);

                        final var expirationDelay = Duration.between(offer.validUntil(), offer.validAfter());
                        handler0.setExpirationTask(expirationDelay, () -> {
                            if (handlerMap.remove(key) != null) {
                                handler.onExpiry(new TrustedSessionOfferBuilder()
                                    .offerSeq(offer.offerSeq())
                                    .offerorName(offer.offerorName())
                                    .receiverName(offer.receiverName())
                                    .validAfter(offer.validAfter())
                                    .validUntil(offer.validUntil())
                                    .contracts(offer.contractsAsDtos())
                                    .createdAt(offer.offeredAt())
                                    .build());
                            }
                        });
                    })
                    .onFailure(handler::onFault);
            }
        }
    }

    private static class Handler implements ArTrustedNegotiationHandler {
        private final ArTrustedNegotiationHandler handler;

        private Future<?> expirationTask = null;

        private Handler(final ArTrustedNegotiationHandler handler) {
            this.handler = handler;
        }

        public synchronized void setExpirationTask(final Duration delay, final Runnable task) {
            if (expirationTask != null) {
                expirationTask.cancel();
            }
            expirationTask = Schedulers.fixed().schedule(delay, task);
        }

        @Override
        public synchronized void onAccept(final TrustedSessionOffer candidate) {
            handler.onAccept(candidate);
        }

        @Override
        public synchronized void onOffer(final TrustedSessionOffer candidate, final ArTrustedNegotiationResponder responder) {
            handler.onOffer(candidate, responder);
        }

        @Override
        public synchronized void onReject(final TrustedSessionOffer candidate) {
            handler.onReject(candidate);
        }

        @Override
        public void onExpiry(final TrustedSessionOffer candidate) {
            handler.onExpiry(candidate);
        }
    }

    private static class HandlerKey {
        private final String name1;
        private final String name2;
        private final long id;

        private HandlerKey(final String name1, final String name2, final long id) {
            this.name1 = Objects.requireNonNull(name1, "Expected name1");
            this.name2 = Objects.requireNonNull(name2, "Expected name2");
            this.id = id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name1.hashCode() ^ name2.hashCode(), id);
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) { return true; }
            if (other == null || getClass() != other.getClass()) { return false; }
            final HandlerKey that = (HandlerKey) other;
            return id == that.id &&
                ((name1.equals(that.name1) && name2.equals(that.name2)) ||
                    (name1.equals(that.name2) && name2.equals(that.name1)));
        }
    }
}
