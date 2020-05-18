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
import se.arkalix.util.concurrent.Schedulers;

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
 *         .map(facade -&gt; (ArContractNegotiationTrustedPluginFacade) facade)
 *         .orElseThrow(() -&gt; new IllegalStateException("Negotiator facade not available"));
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
 *                 .ifSuccess(ignored -&gt; System.out.println("Rejected counter-offer"))
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
public class HttpJsonTrustedContractNegotiatorPlugin implements ArTrustedContractNegotiatorPlugin {
    private static final Logger logger = LoggerFactory.getLogger(HttpJsonTrustedContractNegotiatorPlugin.class);

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

                    final var handlerKey = new HandlerKey(offerorName, receiverName, negotiationId);
                    final var handler = handlerMap.get(handlerKey);
                    if (handler == null) {
                        logger.trace("HTTP/JSON contract negotiator received " +
                            "contract event identifying session not relevant " +
                            "to this system; ignoring event [data={}, " +
                            "metadata={}]", data, metadata);
                        return;
                    }

                    system.consume()
                        .using(HttpJsonTrustedContractObservationService.factory())
                        .flatMap(service -> service.getByNamesAndId(offerorName, receiverName, negotiationId)
                            .map(optionalSession -> optionalSession.orElseThrow(() -> new IllegalStateException("" +
                                "Advertised session [data=" + data +
                                ", metadata=" + metadata + "] not available " +
                                "via service \"" + service.service().name() +
                                "\"; cannot present session update to " +
                                "negotiation handler"))))
                        .ifSuccess(session -> {
                            switch (session.status()) {
                            case OFFERING:
                                handler.onOffer(session, new TrustedContractNegotiatorResponder() {
                                    @Override
                                    public Future<?> accept() {
                                        handler.close();
                                        return system.consume()
                                            .using(HttpJsonTrustedContractNegotiationService.factory())
                                            .flatMap(service -> service.accept(new TrustedContractAcceptanceBuilder()
                                                .negotiationId(negotiationId)
                                                .acceptedAt(Instant.now())
                                                .build()));
                                    }

                                    @Override
                                    public Future<?> offer(final SimplifiedContractCounterOffer offer) {
                                        final var counterOffer = new TrustedContractCounterOfferBuilder()
                                            .negotiationId(negotiationId)
                                            .offerorName(offerorName)
                                            .receiverName(receiverName)
                                            .validAfter(offer.validAfter())
                                            .validUntil(offer.validUntil())
                                            .contracts(offer.contracts())
                                            .offeredAt(offer.offeredAt())
                                            .build();
                                        handler.refresh(counterOffer);
                                        return system.consume()
                                            .using(HttpJsonTrustedContractNegotiationService.factory())
                                            .flatMap(service -> service.counterOffer(counterOffer));
                                    }

                                    @Override
                                    public Future<?> reject() {
                                        handler.close();
                                        return system.consume()
                                            .using(HttpJsonTrustedContractNegotiationService.factory())
                                            .flatMap(service -> service.reject(new TrustedContractRejectionBuilder()
                                                .negotiationId(negotiationId)
                                                .rejectedAt(Instant.now())
                                                .build()));
                                    }
                                });
                                break;

                            case ACCEPTED:
                                handler.onAccept(session);
                                break;

                            case REJECTED:
                                handler.onReject(session);
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

        private class Facade implements ArTrustedContractNegotiatorPluginFacade {
            @Override
            public void offer(final TrustedContractOfferDto offer, final TrustedContractNegotiatorHandler handler) {
                system.consume()
                    .using(HttpJsonTrustedContractNegotiationService.factory())
                    .flatMap(service -> service.offer(offer))
                    .ifSuccess(negotiationId -> {
                        final var key = new HandlerKey(offer.offerorName(), offer.receiverName(), negotiationId);
                        handlerMap.put(key, new Handler(offer, negotiationId, handler, () -> handlerMap.remove(key)));
                    })
                    .onFailure(handler::onFault);
            }
        }
    }

    private static class Handler {
        private final TrustedContractNegotiatorHandler handler;
        private final Runnable closeTask;

        private Future<?> expirationFuture;

        private Handler(
            final TrustedContractOfferDto offer,
            final long negotiationId,
            final TrustedContractNegotiatorHandler handler,
            final Runnable closeTask)
        {
            this.handler = Objects.requireNonNull(handler, "Expected handler");
            this.closeTask = Objects.requireNonNull(closeTask, "Expected closeTask");
            expirationFuture = Schedulers.fixed().schedule(offer.expiresIn(), () -> {
                closeTask.run();
                onExpiry(new TrustedContractNegotiationBuilder()
                    .id(negotiationId)
                    .offer(offer)
                    .status(ContractNegotiationStatus.EXPIRED)
                    .build());
            });
        }

        public synchronized void onAccept(final TrustedContractNegotiationDto session) {
            if (expirationFuture != null) {
                expirationFuture.cancel();
                expirationFuture = null;
            }
            try {
                handler.onAccept(session);
            }
            catch (final Throwable throwable) {
                logger.error("Negotiation handler unexpectedly threw " +
                    "exception while handling acceptance event", throwable);
            }
        }

        public synchronized void onOffer(final TrustedContractNegotiationDto session,
                                         final TrustedContractNegotiatorResponder responder)
        {
            if (expirationFuture != null) {
                expirationFuture.cancel();
            }
            expirationFuture = Schedulers.fixed().schedule(session.offer().expiresIn(), () -> {
                closeTask.run();
                onExpiry(new TrustedContractNegotiationBuilder()
                    .id(session.id())
                    .offer(session.offer())
                    .status(ContractNegotiationStatus.EXPIRED)
                    .build());
            });
            try {
                handler.onOffer(session, responder);
            }
            catch (final Throwable throwable) {
                logger.error("Negotiation handler unexpectedly threw " +
                    "exception while handling offer event", throwable);
            }
        }

        public synchronized void onReject(final TrustedContractNegotiationDto session) {
            if (expirationFuture != null) {
                expirationFuture.cancel();
                expirationFuture = null;
            }
            try {
                handler.onReject(session);
            }
            catch (final Throwable throwable) {
                logger.error("Negotiation handler unexpectedly threw " +
                    "exception while handling rejection event", throwable);
            }
        }

        private synchronized void onExpiry(final TrustedContractNegotiationDto session) {
            try {
                handler.onExpiry(session);
            }
            catch (final Throwable throwable) {
                logger.error("Negotiation handler unexpectedly threw " +
                    "exception while handling expiration event", throwable);
            }
        }

        public synchronized void onFault(final Throwable throwable0) {
            try {
                handler.onFault(throwable0);
            }
            catch (final Throwable throwable1) {
                throwable1.addSuppressed(throwable0);
                logger.error("Negotiation handler unexpectedly threw " +
                    "exception while handling another exception", throwable1);
            }
        }

        public synchronized void refresh(final TrustedContractCounterOfferDto counterOffer) {
            if (expirationFuture != null) {
                expirationFuture.cancel();
            }
            expirationFuture = Schedulers.fixed().schedule(counterOffer.expiresIn(), () -> {
                closeTask.run();
                onExpiry(new TrustedContractNegotiationBuilder()
                    .id(counterOffer.negotiationId())
                    .offer(new TrustedContractOfferBuilder()
                        .offerorName(counterOffer.offerorName())
                        .receiverName(counterOffer.receiverName())
                        .validAfter(counterOffer.validAfter())
                        .validUntil(counterOffer.validUntil())
                        .contracts(counterOffer.contractsAsDtos())
                        .offeredAt(counterOffer.offeredAt())
                        .build())
                    .status(ContractNegotiationStatus.EXPIRED)
                    .build());
            });
        }

        public synchronized void close() {
            if (expirationFuture != null) {
                expirationFuture.cancel();
                expirationFuture = null;
            }
            closeTask.run();
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
