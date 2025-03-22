package di

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import repository.auth.AuthRepository
import repository.auth.DefaultAuthRepository
import repository.mapper.ErrorMapper

/**
 * Модуль зависимостей для слоя данных
 */
val dataModule = DI.Module("dataModule") {
    // Репозиторий аутентификации
    bind<AuthRepository>() with singleton {
        DefaultAuthRepository(instance())
    }

    // Маппер ошибок
    bind<ErrorMapper>() with singleton { ErrorMapper }
}
