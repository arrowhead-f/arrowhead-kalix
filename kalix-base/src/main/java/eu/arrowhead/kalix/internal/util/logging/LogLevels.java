package eu.arrowhead.kalix.internal.util.logging;

import eu.arrowhead.kalix.util.logging.LogLevel;

public class LogLevels {
    private LogLevels() {}

    public static io.netty.handler.logging.LogLevel toNettyLogLevel(final LogLevel logLevel) {
        switch (logLevel) {
        case TRACE: return io.netty.handler.logging.LogLevel.TRACE;
        case DEBUG: return io.netty.handler.logging.LogLevel.DEBUG;
        case INFO: return io.netty.handler.logging.LogLevel.INFO;
        case WARN: return io.netty.handler.logging.LogLevel.WARN;
        case ERROR: return io.netty.handler.logging.LogLevel.ERROR;
        }
        throw new IllegalStateException("LogLevel not convertible: " + logLevel);
    }
}
