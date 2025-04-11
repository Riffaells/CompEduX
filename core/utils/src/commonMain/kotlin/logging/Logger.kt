package logging

/**
 * Interface for application logging
 */
interface Logger {
    /**
     * Logs a debug message
     * @param message the message to log
     * @param throwable optional throwable to log
     * @param tag optional tag for the log message
     */
    fun d(message: String, throwable: Throwable? = null, tag: String? = null)

    /**
     * Logs an info message
     * @param message the message to log
     * @param throwable optional throwable to log
     * @param tag optional tag for the log message
     */
    fun i(message: String, throwable: Throwable? = null, tag: String? = null)

    /**
     * Logs a warning message
     * @param message the message to log
     * @param throwable optional throwable to log
     * @param tag optional tag for the log message
     */
    fun w(message: String, throwable: Throwable? = null, tag: String? = null)

    /**
     * Logs an error message
     * @param message the message to log
     * @param throwable optional throwable to log
     * @param tag optional tag for the log message
     */
    fun e(message: String, throwable: Throwable? = null, tag: String? = null)

    /**
     * Logs a verbose message
     * @param message the message to log
     * @param throwable optional throwable to log
     * @param tag optional tag for the log message
     */
    fun v(message: String, throwable: Throwable? = null, tag: String? = null)

    /**
     * Creates a new logger instance with the specified tag
     * @param tag the tag to use for the new logger
     * @return a new logger with the specified tag
     */
    fun withTag(tag: String): Logger
}

/**
 * Functional extensions for Logger
 */
fun Logger.debug(message: () -> String, throwable: Throwable? = null, tag: String? = null) {
    d(message(), throwable, tag)
}

fun Logger.info(message: () -> String, throwable: Throwable? = null, tag: String? = null) {
    i(message(), throwable, tag)
}

fun Logger.warn(message: () -> String, throwable: Throwable? = null, tag: String? = null) {
    w(message(), throwable, tag)
}

fun Logger.error(message: () -> String, throwable: Throwable? = null, tag: String? = null) {
    e(message(), throwable, tag)
}

fun Logger.verbose(message: () -> String, throwable: Throwable? = null, tag: String? = null) {
    v(message(), throwable, tag)
}
