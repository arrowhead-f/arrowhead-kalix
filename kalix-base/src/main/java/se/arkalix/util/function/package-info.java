/**
 * <h1>Kalix Functional Primitives</h1>
 * The classes in this package exist as a complement to their counter-parts in
 * the {@link java.util.function} library. In particular, the variants
 * available in this package are more permissive when it comes to exceptions,
 * as they are intended to be used in contexts where exceptions will always be
 * caught and packed into failing {@link se.arkalix.util.concurrent.Future
 * Future} or {@link se.arkalix.util.Result Result} instances.
 */
package se.arkalix.util.function;