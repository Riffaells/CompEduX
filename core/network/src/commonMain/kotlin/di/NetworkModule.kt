package di

import MultiplatformSettings
import api.ApiClient
import api.auth.AuthApi
import api.auth.AuthApiImpl
import api.auth.TokenManager
import com.russhwolf.settings.Settings
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

    // API аутентификации
    bind<AuthApi>() with singleton {
        AuthApiImpl(instance())
    }

    // Менеджер токенов
    bind<TokenManager>() with singleton {
        TokenManager(instance<Settings>(), instance())
    }
}
