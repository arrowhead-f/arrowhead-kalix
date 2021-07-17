/**
 * <h1>Kalix Concurrency</h1>
 * Kalix concurrency centers around two primary primitives {@link
 * se.arkalix.util.concurrent.Future futures} and {@link
 * se.arkalix.util.concurrent.Scheduler schedulers}. Futures allow the {@link
 * se.arkalix.util.concurrent.Result results} of asynchronous operations be awaited
 * without blocking the current thread, while schedulers are what make the
 * non-blocking property of futures possible. To make the use of scheduling as
 * seamless as possible for users of the Kalix library, there are two default
 * {@link se.arkalix.util.concurrent.Schedulers schedulers} that are
 * instantiated and used automatically by Kalix classes that rely on
 * scheduling.
 *
 * @see se.arkalix.util.concurrent.Future Future
 * @see se.arkalix.util.concurrent.Scheduler Scheduler
 * @see se.arkalix.util.concurrent.Schedulers Schedulers
 */
package se.arkalix.util.concurrent;