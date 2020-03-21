package se.arkalix.internal.net.http.client;

import se.arkalix.net.http.client.HttpClientConnection;
import se.arkalix.util.Result;
import se.arkalix.util.concurrent.Future;

import java.util.concurrent.CancellationException;
import java.util.function.Consumer;

public class FutureHttpClientConnection implements Future<HttpClientConnection> {
    private Consumer<Result<HttpClientConnection>> consumer = null;
    private Result<HttpClientConnection> pendingResult = null;

    private boolean isCancelled = false;

    @Override
    public void onResult(final Consumer<Result<HttpClientConnection>> consumer) {
        if (pendingResult != null) {
            consumer.accept(pendingResult);
            pendingResult = null;
            return;
        }
        this.consumer = consumer;
    }

    @Override
    public void cancel(final boolean mayInterruptIfRunning) {
        isCancelled = true;
    }

    public boolean failIfCancelled() {
        if (isCancelled) {
            setResult(Result.failure(new CancellationException()));
            return true;
        }
        return false;
    }

    public void setResult(final Result<HttpClientConnection> result) {
        if (consumer != null) {
            consumer.accept(result);
            consumer = null;
            return;
        }
        if (pendingResult != null) {
            throw new IllegalStateException("Result already set");
        }
        pendingResult = result;
    }
}
