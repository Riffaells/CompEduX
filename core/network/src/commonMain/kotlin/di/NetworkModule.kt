package di

import api.NetworkAuthApiImpl
import api.auth.NetworkAuthApi
import api.course.NetworkCourseApi
import api.course.NetworkCourseApiImpl
import api.room.NetworkRoomApi
import api.room.NetworkRoomApiImpl
import api.tree.NetworkTreeApi
import api.tree.NetworkTreeApiImpl
import client.HttpClientFactory
import client.InMemoryTokenStorage
import client.TokenStorage
import io.ktor.client.*
import kotlinx.serialization.json.Json
import logging.LoggingProvider
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

    // API для курсов
    bind<NetworkCourseApi>() with singleton {
        NetworkCourseApiImpl(
            client = instance(),
            networkConfig = instance(),
            logger = instance<LoggingProvider>().withTag("NetworkCourseApi")
        )
    }

    // API для технологического дерева
    bind<NetworkTreeApi>() with singleton {
        NetworkTreeApiImpl(
            client = instance(),
            networkConfig = instance(),
            logger = instance<LoggingProvider>().withTag("TreeApi")
        )
    }

    // API для комнат
    bind<NetworkRoomApi>() with singleton {
        NetworkRoomApiImpl(
            client = instance(),
            networkConfig = instance(),
            logger = instance<LoggingProvider>().withTag("NetworkRoomApi")
        )
    }
}
