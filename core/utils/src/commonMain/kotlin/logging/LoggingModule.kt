package logging

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.Napier
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

/**
 * Provides application logging components for DI container
 */
val loggingModule = DI.Module("loggingModule") {
    // Инициализируем логирование через централизованную систему
    initializeLogging()

    // Привязываем глобальный экземпляр Antilog
    bind<Antilog>() with singleton { LoggingInitializer.getLoggingProvider().antilog }

    // Привязываем глобальный LoggingProvider
    bind<LoggingProvider>() with singleton { LoggingInitializer.getLoggingProvider() }

    // Привязываем дефолтный логгер без тега
    bind<Logger>() with singleton { LoggingInitializer.getLogger() }
}
