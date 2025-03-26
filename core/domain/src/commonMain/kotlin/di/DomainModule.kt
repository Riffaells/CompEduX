package di

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import usecase.auth.*

/**
 * Модуль для доменного слоя
 */
val domainModule = DI.Module("domainModule") {
    // Use cases для аутентификации
    bind<LoginUseCase>() with singleton {
        LoginUseCase(instance())
    }

    bind<RegisterUseCase>() with singleton {
        RegisterUseCase(instance())
    }

    bind<LogoutUseCase>() with singleton {
        LogoutUseCase(instance())
    }

    bind<GetCurrentUserUseCase>() with singleton {
        GetCurrentUserUseCase(instance())
    }

    bind<IsAuthenticatedUseCase>() with singleton {
        IsAuthenticatedUseCase(instance())
    }

    bind<UpdateProfileUseCase>() with singleton {
        UpdateProfileUseCase(instance())
    }

    bind<CheckServerStatusUseCase>() with singleton {
        CheckServerStatusUseCase(instance())
    }

    // Контейнер для всех use cases аутентификации
    bind<AuthUseCases>() with singleton {
        AuthUseCases(
            login = instance(),
            register = instance(),
            logout = instance(),
            getCurrentUser = instance(),
            isAuthenticated = instance(),
            updateProfile = instance(),
            checkServerStatus = instance()
        )
    }
}
