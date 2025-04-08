package di

import api.adapter.AuthApiAdapter
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import repository.mapper.DataAuthMapper
import repository.mapper.DataErrorMapper
import repository.mapper.ErrorMapper
import api.AuthApi as DomainAuthApi

/**
 * Модуль для слоя данных
 */
val dataModule = DI.Module("dataModule") {
    // Импортируем репозитории
    import(repositoryModule)

    // API адаптер для преобразования между доменным API и NetworkAuthApi
    bind<DomainAuthApi>() with singleton {
        AuthApiAdapter(
            networkAuthApi = instance(),
            settings = instance()
        )
    }

    // Мапперы
    bind<ErrorMapper>() with singleton { DataErrorMapper() }
    bind<DataAuthMapper>() with singleton { DataAuthMapper }
}
