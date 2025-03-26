package di

import api.AuthApi
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import repository.auth.AuthRepository
import repository.auth.AuthRepositoryImpl
import settings.MultiplatformSettings
import repository.mapper.ErrorMapper

/**
 * Модуль, регистрирующий все репозитории для DI
 */
val repositoryModule = DI.Module("repository_module") {
    // Репозиторий аутентификации
    bind<AuthRepository>() with singleton {
        AuthRepositoryImpl(instance(), instance())
    }

    // Здесь регистрируем другие репозитории
}
