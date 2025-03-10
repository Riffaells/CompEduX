package di

import api.auth.AuthApi
import api.auth.TokenManager
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import repository.auth.AuthRepository
import repository.auth.AuthRepositoryImpl

/**
 * Модуль зависимостей для компонентов данных
 */
val dataModule = DI.Module("dataModule") {
    // Репозиторий аутентификации
    bind<AuthRepository>() with singleton {
        AuthRepositoryImpl(instance<AuthApi>(), instance<TokenManager>())
    }
}
