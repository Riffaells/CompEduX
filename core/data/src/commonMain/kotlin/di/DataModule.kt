package di

import api.NetworkAuthApi
import api.adapter.AuthApiAdapter
import api.adapter.NetworkAuthApiAdapter
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import repository.mapper.AuthMapper
import repository.mapper.DataErrorMapper
import repository.mapper.ErrorMapper
import settings.MultiplatformSettings
import api.AuthApi as DomainAuthApi

/**
 * Модуль для слоя данных
 */
val dataModule = DI.Module("dataModule") {
    // Импортируем репозитории
    import(repositoryModule)

    // NetworkAuthApi адаптер для преобразования между NetworkAuthApi и KtorAuthApi
    bind<NetworkAuthApi>() with singleton {
        NetworkAuthApiAdapter(
            ktorAuthApi = instance()
        )
    }

    // API адаптер для преобразования между доменным API и NetworkAuthApi
    bind<DomainAuthApi>() with singleton {
        AuthApiAdapter(
            networkAuthApi = instance<NetworkAuthApi>(),
            settings = instance<MultiplatformSettings>()
        )
    }

    // Мапперы
    bind<ErrorMapper>() with singleton { DataErrorMapper() }
    bind<AuthMapper>() with singleton { AuthMapper }
}
