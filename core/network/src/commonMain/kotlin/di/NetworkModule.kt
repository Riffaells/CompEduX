package di

import api.AuthApi
import api.impl.AuthApiImpl
import client.HttpClientFactory
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import repository.mapper.ErrorMapper

/**
 * Модуль зависимостей для сетевых компонентов
 */
val networkModule = DI.Module("networkModule") {
    // Базовый URL API (устаревший подход, теперь берём из настроек)
    bind<String>(tag = "baseUrl") with singleton { "https://api.default.com" }

    // HTTP клиент
    bind<HttpClient>() with singleton {
        HttpClientFactory(instance(), instance()).create()
    }

    // API аутентификации
    bind<AuthApi>() with singleton {
        AuthApiImpl(
            client = instance(),
            baseUrl = instance(tag = "baseUrl"),
            errorMapper = instance(),
            networkConfig = instance()
        )
    }

    // JSON сериализатор
    bind<Json>() with singleton {
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
    }
}
