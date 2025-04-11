package di

import api.NetworkAuthApi
import api.NetworkAuthApiImpl
import client.HttpClientFactory
import client.InMemoryTokenStorage
import client.TokenStorage
import io.ktor.client.*
import kotlinx.serialization.json.Json
import logging.LoggingProvider
import logging.logger
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

/**
 * DI-модуль для network-слоя
 */
val networkModule = DI.Module("networkModule") {
    // JSON сериализатор
    bind<Json>() with singleton {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            prettyPrint = true
            coerceInputValues = true
        }
    }

    // Хранилище токенов
    bind<TokenStorage>() with singleton {
        InMemoryTokenStorage()
    }

    // Фабрика HTTP-клиента
    bind<HttpClientFactory>() with singleton {
        HttpClientFactory(
            json = instance(),
            tokenStorage = instance(),
            networkConfig = instance(),
            logger = instance<LoggingProvider>().withTag("HttpClient")
        )
    }

    // HTTP-клиент
    bind<HttpClient>() with singleton {
        instance<HttpClientFactory>().create()
    }

    // API для аутентификации
    bind<NetworkAuthApi>() with singleton {
        NetworkAuthApiImpl(
            client = instance(),
            networkConfig = instance(),
            logger = instance<LoggingProvider>().withTag("NetworkAuthApi")
        )
    }
}
