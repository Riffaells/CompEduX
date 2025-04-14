package logging

import io.github.aakira.napier.Napier

/**
 * Утилитарный класс для инициализации логирования в приложении.
 * Помогает предотвратить дублирование логов.
 */
object LoggingInitializer {

    private var isInitialized = false

    // Объект для синхронизации
    private val initLock = Any()

    // Глобальный экземпляр Antilog для всего приложения
    private val globalAntilog by lazy { CompEduXAntilog() }

    // Блокируем создание дополнительных экземпляров LoggingProvider
    private val globalLoggingProvider by lazy { LoggingProvider(globalAntilog) }

    /**
     * Инициализирует систему логирования. Должен вызываться один раз при запуске приложения.
     * Предотвращает дублирование логов путем очистки старых логгеров.
     */
    fun initialize() {
        synchronized(initLock) {
            if (isInitialized) return

            // Полностью очищаем все существующие логгеры
            Napier.takeLogarithm()

            // Устанавливаем наш единственный Antilog
            Napier.base(globalAntilog)

            // Установка флага инициализации
            isInitialized = true

            // Логируем только один раз при инициализации
            getLogger("LoggingSystem").i("Logging system initialized - version 1.0")
        }
    }

    /**
     * Получить глобальный экземпляр LoggingProvider
     * Все логгеры должны создаваться только через эту функцию
     */
    fun getLoggingProvider(): LoggingProvider {
        if (!isInitialized) {
            initialize()
        }
        return globalLoggingProvider
    }

    /**
     * Получить логгер по имени тега
     * Удобный метод для быстрого доступа к логгеру
     */
    fun getLogger(tag: String = ""): Logger {
        return getLoggingProvider().withTag(tag)
    }

    /**
     * Очищает все логгеры и сбрасывает состояние.
     * Полезно в случаях сильного дублирования логов.
     */
    fun reset() {
        synchronized(initLock) {
            // Удаление всех установленных логгеров
            Napier.takeLogarithm()

            // Переустанавливаем логгер
            Napier.base(globalAntilog)

            // Сброс флага инициализации
            isInitialized = false

            // Заново инициализируем
            initialize()
        }
    }
}

/**
 * Вызывайте эту функцию из точки входа вашего приложения,
 * чтобы гарантировать правильную инициализацию логирования
 */
fun initializeLogging() {
    LoggingInitializer.initialize()
}

/**
 * Глобальная функция для быстрого доступа к логгеру
 */
fun getLogger(tag: String = ""): Logger {
    return LoggingInitializer.getLogger(tag)
}
