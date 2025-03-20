package di

import component.app.auth.store.AuthStore
import component.app.auth.store.AuthStoreFactory
import component.app.auth.store.LoginStore
import component.app.auth.store.LoginStoreFactory
import component.app.auth.store.ProfileStore
import component.app.auth.store.ProfileStoreFactory
import component.app.auth.store.RegisterStore
import component.app.auth.store.RegisterStoreFactory
import com.arkivanov.mvikotlin.core.store.StoreFactory
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.factory
import org.kodein.di.instance
import org.kodein.di.provider
import org.kodein.di.singleton

// Модуль для компонентов аутентификации
val authModule = DI.Module("authModule") {
    // Основной AuthStore - синглтон, чтобы сохранять состояние аутентификации глобально
    bind<AuthStore>() with singleton {
        val authStoreFactory = AuthStoreFactory(instance())
        authStoreFactory.create()
    }

}
