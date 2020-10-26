/**
 * <h1>HTTP Consumer Utilities</h1>
 * This package most significantly contains the {@link
 * se.arkalix.net.http.consumer.HttpConsumer HttpConsumer} class, which is
 * useful for sending HTTP requests to remote Arrowhead {@link se.arkalix
 * services}.
 * <p>
 * This package distinguishing itself form the {@link
 * se.arkalix.net.http.client HTTP Client Utilities} package by making use of
 * available Arrowhead {@link
 * se.arkalix.ServiceRecord service descriptions}. Such can
 * most adequately be retrieved via the {@link se.arkalix.query.ServiceQuery
 * ServiceQuery} class, instances of which are returned by the {@link
 * se.arkalix.ArSystem#consume() ArSystem#consume()} method. It effectively
 * allows calls as the following naive example:
 * <pre>
 *     system.consume()
 *         .name("the-service-name")
 *         .encoding(EncodingDescriptor.JSON)
 *         .oneUsing(HttpConsumer.factory())
 *         .flatMap(consumer -&gt; consumer.send(new HttpConsumerRequest()
 *            {...})
 *         .flatMap(HttpConsumerResponse::bodyAsString)
 *         .ifSuccess(body -&gt; System.out.println(body))
 *         .onFailure(Throwable::printStackTrace);
 * </pre>
 * It should be noted that all functionality provided by this package is
 * <i>non-blocking</i> by virtue of all I/O operations being handled by the
 * {@link se.arkalix.util.concurrent.Schedulers Kalix schedulers}.
 */
package se.arkalix.net.http.consumer;