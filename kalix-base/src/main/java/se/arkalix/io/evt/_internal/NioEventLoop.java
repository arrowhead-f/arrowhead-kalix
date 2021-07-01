package se.arkalix.io.evt._internal;

import se.arkalix.io.evt.EventLoop;
import se.arkalix.util.concurrent.Future;

public class NioEventLoop implements EventLoop {
    @Override
    public Future<?> shutdown() {
        return null;
    }
}
