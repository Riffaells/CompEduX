package di

import api.AuthApi as DomainAuthApi
import api.auth.AuthApi as NetworkAuthApi
import api.adapter.AuthApiAdapter
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import repository.auth.AuthRepository
import repository.auth.DefaultAuthRepository
import repository.mapper.AuthMapper
import repository.mapper.ErrorMapper
import settings.MultiplatformSettings

/**
 * Модуль зависимостей для слоя данных
 */
val dataModule = DI.Module("dataModule") {
    // API адаптер для преобразования между доменным API и сетевым API
    bind<DomainAuthApi>() with singleton {
        AuthApiAdapter(
            networkAuthApi = instance<NetworkAuthApi>(),
            settings = instance<MultiplatformSettings>()
        )
    }

    // Репозиторий аутентификации
    bind<AuthRepository>() with singleton {
        DefaultAuthRepository(
            authApi = instance<DomainAuthApi>(),
            settings = instance<MultiplatformSettings>()
        )
    }

    // Маппер ошибок
    bind<ErrorMapper>() with singleton { ErrorMapper }

    // Маппер аутентификации
    bind<AuthMapper>() with singleton { AuthMapper }
}
