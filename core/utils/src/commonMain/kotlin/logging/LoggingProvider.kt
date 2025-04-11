package logging

import io.github.aakira.napier.Antilog
import org.kodein.di.DIAware
import org.kodein.di.instance

/**
 * Provider for obtaining loggers without extra abstractions
 */
class LoggingProvider(private val antilog: Antilog) {
    /**
     * Gets a logger with the specified tag
     * @param tag optional tag for the logger
     * @return a logger instance with the tag
     */
    fun getLogger(tag: String? = null): Logger {
        return NapierLogger(antilog, tag)
    }

    /**
     * Gets a logger with the specified tag
     * Alternative to getLogger with more expressive name
     * @param tag tag for the logger
     * @return a logger instance with the tag
     */
    fun withTag(tag: String = ""): Logger {
        return getLogger(tag)
    }
}

/**
 * Extensions for DIAware classes to easily obtain loggers
 */
inline fun <reified T : Any> DIAware.logger(): Logger {
    val provider by instance<LoggingProvider>()
    return provider.getLogger(T::class.simpleName)
}

fun DIAware.logger(tag: String): Logger {
    val provider by instance<LoggingProvider>()
    return provider.getLogger(tag)
}
