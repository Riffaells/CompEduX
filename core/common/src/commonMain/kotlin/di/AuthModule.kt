package di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import component.app.auth.store.LoginStore
import component.app.auth.store.LoginStoreFactory
import component.app.auth.store.ProfileStore
import component.app.auth.store.ProfileStoreFactory
import component.app.auth.store.RegisterStore
import component.app.auth.store.RegisterStoreFactory
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

// Модуль для компонентов аутентификации
val authModule = DI.Module("auth") {
    // Фабрики для Store компонентов аутентификации
    bindProvider<LoginStoreFactory> {
        LoginStoreFactory(
            storeFactory = instance<StoreFactory>(),
            di = di
        )
    }

    bindProvider<RegisterStoreFactory> {
        RegisterStoreFactory(
            storeFactory = instance<StoreFactory>(),
            di = di
        )
    }

    bindProvider<ProfileStoreFactory> {
        ProfileStoreFactory(
            storeFactory = instance<StoreFactory>(),
            di = di
        )
    }
}
