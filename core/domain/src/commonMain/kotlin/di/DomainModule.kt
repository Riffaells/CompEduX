package di

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import usecase.auth.*

/**
 * DI-модуль для домена
 * Содержит все Use Cases и другие компоненты доменного слоя
 */
val domainModule = DI.Module("domainModule") {
    // Auth Use Cases
    bind<LoginUseCase>() with singleton { LoginUseCase(instance()) }
    bind<RegisterUseCase>() with singleton { RegisterUseCase(instance()) }
    bind<GetCurrentUserUseCase>() with singleton { GetCurrentUserUseCase(instance()) }
    bind<LogoutUseCase>() with singleton { LogoutUseCase(instance()) }
    bind<CheckServerStatusUseCase>() with singleton { CheckServerStatusUseCase(instance()) }
    bind<IsAuthenticatedUseCase>() with singleton { IsAuthenticatedUseCase(instance()) }

    // Контейнер для всех use cases аутентификации
    bind<AuthUseCases>() with singleton {
        AuthUseCases(
            login = instance(),
            register = instance(),
            logout = instance(),
            getCurrentUser = instance(),
            isAuthenticated = instance(),
            checkServerStatus = instance()
        )
    }
}
