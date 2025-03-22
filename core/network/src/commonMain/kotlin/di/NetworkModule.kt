package di

import api.ApiClient
import api.auth.AuthApi
import api.auth.AuthApiImpl
import config.NetworkConfig
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

/**
 * Модуль зависимостей для сетевых компонентов
 */
val networkModule = DI.Module("networkModule") {
    // API клиент
    bind<ApiClient>() with singleton {
        ApiClient(instance())
    }

    // HTTP клиент
    bind<HttpClient>() with singleton {
        instance<ApiClient>().createHttpClient()
    }

    // API аутентификации
    bind<AuthApi>() with singleton {
        AuthApiImpl(instance())
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
