package logging

import io.github.aakira.napier.Antilog
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

/**
 * Provides application logging components for DI container
 */
val loggingModule = DI.Module("loggingModule") {
    // Configure logging backend
    bind<Antilog>() with singleton { CompEduXAntilog() }

    // Bind logging provider
    bind<LoggingProvider>() with singleton { LoggingProvider(instance()) }

    // Bind default logger (without tag) for convenience
    bind<Logger>() with singleton { instance<LoggingProvider>().withTag() }
}
