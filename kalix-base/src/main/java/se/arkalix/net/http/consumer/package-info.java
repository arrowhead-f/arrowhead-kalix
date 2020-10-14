/**
 * <h1>HTTP Consumer Utilities</h1>
 * This package most significantly contains the {@link
 * se.arkalix.net.http.consumer.HttpConsumer HttpConsumer} class, which is
 * useful for sending HTTP requests to remote Arrowhead {@link se.arkalix
 * services}.
 * <p>
 * This package distinguishing itself form the {@link
 * se.arkalix.net.http.client HTTP Client Utilities} package by relying heavily
 * on the availability of relevant {@link
 * se.arkalix.description.ServiceDescription service descriptions}. Such can
 * most adequately be retrieved via the {@link
 * se.arkalix.query.ServiceQuery#using(se.arkalix.ArConsumerFactory) using()}
 * method of  the {@link se.arkalix.query.ServiceQuery ServiceQuery} class, an
 * instance of which is returned by the {@link se.arkalix.ArSystem#consume()
 * ArSystem#consume()} method. It effectively allows calls as the following
 * naive example:
 * <pre>
 *     system.consume()
 *         .name("the-service-name")
 *         .encoding(EncodingDescriptor.JSON)
 *         .using(HttpConsumer.factory())
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