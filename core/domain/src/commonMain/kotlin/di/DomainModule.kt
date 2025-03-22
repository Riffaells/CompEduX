package di

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import repository.auth.AuthRepository
import usecase.auth.*

/**
 * Модуль зависимостей для компонентов домена
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
        GetCurrentUserUseCase(instance<AuthRepository>())
    }

    bind<CheckServerStatusUseCase>() with singleton {
        CheckServerStatusUseCase(instance<AuthRepository>())
    }
}
