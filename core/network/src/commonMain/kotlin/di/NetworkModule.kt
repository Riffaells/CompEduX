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
import repository.mapper.ErrorMapper

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

    // Фабрика HTTP клиента использует зависимости, импортированные из других модулей
    // Примечание: NetworkConfig и ErrorMapper должны быть предоставлены другими модулями
    bindSingleton<HttpClientFactory> {
        HttpClientFactory(
            json = instance(),
            errorMapper = instance<ErrorMapper>(),
            networkConfig = instance<NetworkConfig>()
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
