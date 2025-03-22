package di

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import repository.AuthRepository
import repository.impl.DefaultAuthRepository
import repository.mapper.ErrorMapper
import repository.mapper.ErrorMapper as ErrorMapperInterface

/**
 * Модуль для предоставления репозиториев
 */
val dataRepositoryModule = DI.Module("dataRepositoryModule") {
    // Маппер ошибок
    bindSingleton<ErrorMapperInterface> { ErrorMapper }

    // Репозиторий авторизации
    bindSingleton<AuthRepository> {
        DefaultAuthRepository(di)
    }
}
