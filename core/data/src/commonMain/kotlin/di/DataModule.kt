package di

import api.auth.AuthApi
import api.auth.DataAuthApiAdapter
import api.auth.NetworkAuthApi
import api.course.CourseApi
import api.course.DataCourseApiAdapter
import api.course.NetworkCourseApi
import config.DataNetworkConfig
import config.NetworkConfig
import logging.LoggingProvider
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import repository.auth.AuthRepository
import repository.auth.AuthRepositoryImpl
import repository.auth.TokenRepository
import repository.auth.TokenRepositoryImpl
import repository.course.CourseRepository
import repository.course.CourseRepositoryImpl
import settings.MultiplatformSettings

/**
 * DI-модуль для data-слоя
 * Содержит репозитории и адаптеры для работы с данными
 */
val dataModule = DI.Module("dataModule") {
    // Конфигурация сети
    bind<NetworkConfig>() with singleton {
        DataNetworkConfig(di)
    }

    // Репозитории и адаптеры для аутентификации
    bind<TokenRepository>() with singleton {
        val multiplatformSettings = instance<MultiplatformSettings>()
        val logger = instance<LoggingProvider>().getLogger("TokenRepository")
        TokenRepositoryImpl(multiplatformSettings.security, logger)
    }

    bind<AuthRepository>() with singleton {
        val networkAuthApi = instance<NetworkAuthApi>()
        val tokenRepository = instance<TokenRepository>()
        val logger = instance<LoggingProvider>().getLogger("AuthRepository")
        AuthRepositoryImpl(networkAuthApi, tokenRepository, logger)
    }

    bind<AuthApi>() with singleton {
        val networkAuthApi = instance<NetworkAuthApi>()
        val tokenRepository = instance<TokenRepository>()
        val logger = instance<LoggingProvider>().getLogger("AuthApi")
        DataAuthApiAdapter(networkAuthApi, tokenRepository, logger)
    }

    // Репозитории и адаптеры для курсов
    bind<CourseRepository>() with singleton {
        val networkCourseApi = instance<NetworkCourseApi>()
        val tokenRepository = instance<TokenRepository>()
        val logger = instance<LoggingProvider>().getLogger("CourseRepository")
        CourseRepositoryImpl(networkCourseApi, tokenRepository, logger)
    }

    bind<CourseApi>() with singleton {
        val networkCourseApi = instance<NetworkCourseApi>()
        val tokenRepository = instance<TokenRepository>()
        val logger = instance<LoggingProvider>().getLogger("CourseApi")
        DataCourseApiAdapter(networkCourseApi, tokenRepository, logger)
    }
}
