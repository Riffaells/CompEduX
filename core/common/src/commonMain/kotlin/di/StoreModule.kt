package di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import component.app.auth.store.AuthStore
import component.app.auth.store.AuthStoreFactory
import component.app.auth.store.LoginStore
import component.app.auth.store.LoginStoreFactory
import component.app.auth.store.RegisterStore
import component.app.auth.store.RegisterStoreFactory
import component.app.auth.store.ProfileStore
import component.app.auth.store.ProfileStoreFactory
import component.root.store.RootStore
import component.root.store.RootStoreFactory
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

/**
 * Модуль для регистрации всех Store-компонентов приложения
 */
val storeModule = DI.Module("storeModule") {
    // Регистрация AuthStore как синглтон (используется во всем приложении)
    bindSingleton<AuthStore> {
        AuthStoreFactory(
            storeFactory = instance<StoreFactory>(),
            di = di
        ).create()
    }

    // Регистрация LoginStore как провайдер (создается для каждого экрана входа)
    bindProvider<LoginStore> {
        LoginStoreFactory(
            storeFactory = instance<StoreFactory>(),
            di = di
        ).create()
    }

    // Регистрация RegisterStore как провайдер (создается для каждого экрана регистрации)
    bindProvider<RegisterStore> {
        RegisterStoreFactory(
            storeFactory = instance<StoreFactory>(),
            di = di
        ).create()
    }

    // Регистрация ProfileStore как провайдер (создается для каждого экрана профиля)
    bindProvider<ProfileStore> {
        ProfileStoreFactory(
            storeFactory = instance<StoreFactory>(),
            di = di
        ).create()
    }

    bindProvider<RootStore> {
        RootStoreFactory(
            storeFactory = instance(),
            di= di
        ).create()
    }
}
