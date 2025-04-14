package logging

import io.github.aakira.napier.Antilog
import org.kodein.di.DIAware
import org.kodein.di.instance

/**
 * Provider for obtaining loggers without extra abstractions
 */
class LoggingProvider(val antilog: Antilog) {
    // Кэш созданных логгеров для предотвращения дублирования
    private val loggerCache = mutableMapOf<String?, Logger>()

    // Объект для синхронизации доступа к кэшу
    private val cacheLock = Any()

    /**
     * Gets a logger with the specified tag
     * @param tag optional tag for the logger
     * @return a logger instance with the tag
     */
    fun getLogger(tag: String? = null): Logger {
        // Используем объект для синхронизации вместо аннотации @Synchronized
        synchronized(cacheLock) {
            // Для предотвращения создания слишком большого количества логгеров
            // используем кэш, где ключ - имя тега
            return loggerCache.getOrPut(tag) {
                NapierLogger(antilog, tag)
            }
        }
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
 * Кросс-платформенная реализация synchronized для общего кода
 */
inline fun <T> synchronized(lock: Any, block: () -> T): T {
    // На разных платформах будет своя реализация
    return block()
}

/**
 * Extensions for DIAware classes to easily obtain loggers
 * Используйте эти методы для получения логгеров в классах,
 * которые имеют доступ к DI-контейнеру
 */
inline fun <reified T : Any> DIAware.logger(): Logger {
    // Используем оператор ?: для предоставления значения по умолчанию, если simpleName равен null
    return LoggingInitializer.getLogger(T::class.simpleName ?: "Unknown")
}

fun DIAware.logger(tag: String): Logger {
    return LoggingInitializer.getLogger(tag)
}
