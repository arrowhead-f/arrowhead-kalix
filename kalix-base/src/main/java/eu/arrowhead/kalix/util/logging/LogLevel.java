package eu.arrowhead.kalix.util.logging;

/**
 * Describes what kinds of internal Arrowhead system events should be logged.
 */
public enum LogLevel {
    /**
     * Information useful while a developer debugs an application.
     */
    TRACE,

    /**
     * Information useful while a systems operator debugs an application.
     */
    DEBUG,

    /**
     * Significant application events that are not errors.
     */
    INFO,

    /**
     * Recoverable errors, such as network failures or receiving faulty data.
     */
    WARN,

    /**
     * Irrecoverable errors, in the sense that they prevent the completion of
     * significant application tasks, such as failing to connect to an
     * application database or access a log file.
     */
    ERROR,
}
