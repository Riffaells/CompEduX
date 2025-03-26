package di

import api.auth.AuthApi
import api.auth.AuthApiImpl
import client.HttpClientFactory
import config.NetworkConfig
import io.ktor.client.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import model.AppError
import model.ErrorCode
import org.kodein.di.*

/**
 * Модуль зависимостей для сетевых компонентов
 */
val networkModule = DI.Module("networkModule") {
    // JSON сериализатор
    bindSingleton<Json> {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            prettyPrint = true
            coerceInputValues = true
        }
    }

    // Примечание: NetworkConfig получается из SettingsModule
    // и не должен быть определен здесь, чтобы избежать конфликта

    // Примечание: ErrorMapper получается из DataModule
    // и не должен быть определен здесь, чтобы избежать конфликта

    // Фабрика HTTP клиента
    bindSingleton<HttpClientFactory> {
        HttpClientFactory(
            json = instance(),
            errorMapper = instance(),
            networkConfig = instance()
        )
    }

    // HTTP клиент
    bindSingleton<HttpClient> {
        instance<HttpClientFactory>().create()
    }

    // API аутентификации
    bindSingleton<AuthApi> {
        AuthApiImpl(
            client = instance(),
            networkConfig = instance()
        )
    }
}
