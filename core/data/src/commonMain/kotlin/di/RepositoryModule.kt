package di

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import repository.auth.AuthRepository
import repository.auth.DefaultAuthRepository
import repository.mapper.ErrorMapper
import repository.mapper.ErrorMapper as ErrorMapperInterface

/**
 * Устаревший модуль для предоставления репозиториев.
 * @deprecated Используйте dataModule вместо этого модуля
 */
@Deprecated("Используйте dataModule вместо этого модуля")
val dataRepositoryModule = DI.Module("dataRepositoryModule") {
    // Этот модуль оставлен для обратной совместимости
    // и будет удален в будущих версиях.
}
