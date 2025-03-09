package com.riffaells.compedux.di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import component.app.auth.store.LoginStoreFactory
import component.app.auth.store.ProfileStoreFactory
import component.app.auth.store.RegisterStoreFactory
import di.componentModule
import di.settingsModule
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

// Основной DI контейнер приложения
val appDI = DI {
    // Общие зависимости для всех платформ
    bindSingleton<StoreFactory> { DefaultStoreFactory() }

    // Импортируем основные модули компонентов
    import(settingsModule)
    import(componentModule)

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

    // Платформенно-специфичные зависимости добавляются в соответствующих платформенных модулях
}
