package eu.arrowhead.kalix;

public abstract class ArrowheadSystemTcp<S> extends ArrowheadSystem<S> {
    protected ArrowheadSystemTcp(final Builder<?, ? extends ArrowheadSystem<?>> builder) {
        super(builder);
    }

    public static abstract class Builder<B extends Builder<?, AS>, AS extends ArrowheadSystemTcp<?>>
        extends ArrowheadSystem.Builder<B, AS>
    {

    }
}
