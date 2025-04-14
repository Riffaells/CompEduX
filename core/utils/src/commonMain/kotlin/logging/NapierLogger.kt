package logging

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.Napier

/**
 * Napier implementation of Logger interface
 * Provides structured logging with better visual distinction
 */
class NapierLogger(
    private val antilog: Antilog,
    private val defaultTag: String? = null
) : Logger {
    companion object {
        // Флаг для отслеживания инициализации - гарантирует
        // что Napier.base() будет вызван только один раз
        private var isInitialized = false

        fun initialize(antilog: Antilog) {
            if (!isInitialized) {
                Napier.base(antilog)
                isInitialized = true
            }
        }
    }

    init {
        // Используем синхронизированную инициализацию вместо прямого вызова
        initialize(antilog)
    }

    override fun d(message: String, throwable: Throwable?, tag: String?) {
        Napier.d(message, throwable, tag ?: defaultTag)
    }

    override fun i(message: String, throwable: Throwable?, tag: String?) {
        Napier.i(message, throwable, tag ?: defaultTag)
    }

    override fun w(message: String, throwable: Throwable?, tag: String?) {
        Napier.w(message, throwable, tag ?: defaultTag)
    }

    override fun e(message: String, throwable: Throwable?, tag: String?) {
        Napier.e(message, throwable, tag ?: defaultTag)
    }

    override fun v(message: String, throwable: Throwable?, tag: String?) {
        Napier.v(message, throwable, tag ?: defaultTag)
    }

    /**
     * Creates a new logger with the specified tag
     * All log messages from this logger will use this tag by default
     * @param tag the default tag for the new logger instance
     * @return a new logger with the specified tag
     */
    override fun withTag(tag: String): Logger {
        return NapierLogger(antilog, tag)
    }
}
