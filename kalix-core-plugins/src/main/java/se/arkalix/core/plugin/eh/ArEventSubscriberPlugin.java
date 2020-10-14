package se.arkalix.core.plugin.eh;

import se.arkalix.ArSystem;
import se.arkalix.description.SystemDescription;
import se.arkalix.plugin.Plugin;

import java.util.Collection;
import java.util.Map;

/**
 * Event subscriber plugin.
 * <p>
 * An implementation of this plugin interface helps manage subscriptions for
 * events using implementations of the {@link ArEventSubscribeService} and
 * {@link ArEventUnsubscribeService} services.
 */
@SuppressWarnings("unused")
public interface ArEventSubscriberPlugin extends Plugin {
    /**
     * Registers new default event subscription.
     * <p>
     * The default event subscriptions added to this plugin are added to each
     * system it is {@link #attachTo(ArSystem, Map) attached} to. For this
     * reason, these subscriptions cannot be cancelled in any other way than
     * {@link ArSystem#shutdown() shutting down} the systems this plugin is
     * attached to. If wanting to create subscriptions that are more readily
     * cancellable, use the {@link ArSystem#pluginFacadeOf(Class)} method and
     * cast its successful result to {@link ArEventSubscriberPluginFacade},
     * like so:
     * <pre>
     *     var system = new System.Builder()
     *         (...)
     *         .plugins(new HttpJsonEventSubscriberPlugin())
     *         .build();
     *     system.pluginFacadeOf(HttpJsonEventSubscriberPlugin.class)
     *         .map(facade -&gt; (ArEventSubscriberPluginFacade) facade)
     *         .ifPresent(facade -&gt; {...});
     * </pre>
     *
     * @param topic   Topic, or "eventType", that must be matched by
     *                received events. Topics are case insensitive and can
     *                only be subscribed to once.
     * @param handler Handler to receive matching events.
     * @return Future completed when subscription is registered.
     */
    default HttpJsonEventSubscriberPlugin subscribe(final String topic, final EventSubscriptionHandler handler) {
        return subscribe(topic, null, null, handler);
    }

    /**
     * Registers new default event subscription.
     * <p>
     * The default event subscriptions added to this plugin are added to each
     * system it is {@link #attachTo(ArSystem, Map) attached} to. For this
     * reason, these subscriptions cannot be cancelled in any other way than
     * {@link ArSystem#shutdown() shutting down} the systems this plugin is
     * attached to. If wanting to create subscriptions that are more readily
     * cancellable, use the {@link ArSystem#pluginFacadeOf(Class)} method and
     * cast its successful result to {@link ArEventSubscriberPluginFacade},
     * like so:
     * <pre>
     *     var system = new System.Builder()
     *         (...)
     *         .plugins(new HttpJsonEventSubscriberPlugin())
     *         .build();
     *     system.pluginFacadeOf(HttpJsonEventSubscriberPlugin.class)
     *         .map(facade -&gt; (ArEventSubscriberPluginFacade) facade)
     *         .ifPresent(facade -&gt; {...});
     * </pre>
     *
     * @param topic    Topic, or "eventType", that must be matched by
     *                 received events. Topics are case insensitive and can
     *                 only be subscribed to once.
     * @param metadata Metadata pairs that must be matched by received
     *                 events.
     * @param handler  Handler to receive matching events.
     * @return Future completed when subscription is registered.
     */
    default HttpJsonEventSubscriberPlugin subscribe(
        final String topic,
        final Map<String, String> metadata,
        final EventSubscriptionHandler handler)
    {
        return subscribe(topic, metadata, null, handler);
    }

    /**
     * Registers new default event subscription.
     * <p>
     * The default event subscriptions added to this plugin are added to each
     * system it is {@link #attachTo(ArSystem, Map) attached} to. For this
     * reason, these subscriptions cannot be cancelled in any other way than
     * {@link ArSystem#shutdown() shutting down} the systems this plugin is
     * attached to. If wanting to create subscriptions that are more readily
     * cancellable, use the {@link ArSystem#pluginFacadeOf(Class)} method and
     * cast its successful result to {@link ArEventSubscriberPluginFacade},
     * like so:
     * <pre>
     *     var system = new System.Builder()
     *         (...)
     *         .plugins(new HttpJsonEventSubscriberPlugin())
     *         .build();
     *     system.pluginFacadeOf(HttpJsonEventSubscriberPlugin.class)
     *         .map(facade -&gt; (ArEventSubscriberPluginFacade) facade)
     *         .ifPresent(facade -&gt; {...});
     * </pre>
     *
     * @param topic     Topic, or "eventType", that must be matched by
     *                  received events. Topics are case insensitive and
     *                  can only be subscribed to once.
     * @param providers Event providers to receive events from.
     * @param handler   Handler to receive matching events.
     * @return Future completed when subscription is registered.
     */
    default HttpJsonEventSubscriberPlugin subscribe(
        final String topic,
        final Collection<SystemDescription> providers,
        final EventSubscriptionHandler handler)
    {
        return subscribe(topic, null, providers, handler);
    }

    /**
     * Registers new default event subscription.
     * <p>
     * The default event subscriptions added to this plugin are added to each
     * system it is {@link #attachTo(ArSystem, Map) attached} to. For this
     * reason, these subscriptions cannot be cancelled in any other way than
     * {@link ArSystem#shutdown() shutting down} the systems this plugin is
     * attached to. If wanting to create subscriptions that are more readily
     * cancellable, use the {@link ArSystem#pluginFacadeOf(Class)} method and
     * cast its successful result to {@link ArEventSubscriberPluginFacade},
     * like so:
     * <pre>
     *     var system = new System.Builder()
     *         (...)
     *         .plugins(new HttpJsonEventSubscriberPlugin())
     *         .build();
     *     system.pluginFacadeOf(HttpJsonEventSubscriberPlugin.class)
     *         .map(facade -&gt; (ArEventSubscriberPluginFacade) facade)
     *         .ifPresent(facade -&gt; {...});
     * </pre>
     *
     * @param topic     Topic, or "eventType", that must be matched by
     *                  received events. Topics are case insensitive and
     *                  can only be subscribed to once.
     * @param metadata  Metadata pairs that must be matched by received
     *                  events.
     * @param providers Event providers to receive events from.
     * @param handler   Handler to receive matching events.
     * @return Future completed when subscription is registered.
     */
    default HttpJsonEventSubscriberPlugin subscribe(
        final String topic,
        final Map<String, String> metadata,
        final Collection<SystemDescription> providers,
        final EventSubscriptionHandler handler)
    {
        return subscribe(new EventSubscription()
            .topic(topic)
            .metadata(metadata)
            .providers(providers)
            .handler(handler));
    }

    /**
     * Registers new default event subscription.
     * <p>
     * The default event subscriptions added to this plugin are added to each
     * system it is {@link #attachTo(ArSystem, Map) attached} to. For this
     * reason, these subscriptions cannot be cancelled in any other way than
     * {@link ArSystem#shutdown() shutting down} the systems this plugin is
     * attached to. If wanting to create subscriptions that are more readily
     * cancellable, use the {@link ArSystem#pluginFacadeOf(Class)} method and
     * cast its successful result to {@link ArEventSubscriberPluginFacade},
     * like so:
     * <pre>
     *     var system = new System.Builder()
     *         (...)
     *         .plugins(new HttpJsonEventSubscriberPlugin())
     *         .build();
     *     system.pluginFacadeOf(HttpJsonEventSubscriberPlugin.class)
     *         .map(facade -&gt; (ArEventSubscriberPluginFacade) facade)
     *         .ifPresent(facade -&gt; {...});
     * </pre>
     *
     * @param subscription Subscription to register.
     * @return This builder.
     */
    HttpJsonEventSubscriberPlugin subscribe(final EventSubscription subscription);
}
