package com.riffaells.compedux.di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import di.componentModule
import di.settingsModule
import org.kodein.di.DI
import org.kodein.di.bindSingleton

// Основной DI контейнер приложения
val appDI = DI {
    // Общие зависимости для всех платформ
    bindSingleton<StoreFactory> { DefaultStoreFactory() }

    // Импортируем основные модули компонентов
    import(settingsModule)
    import(componentModule)

    // Платформенно-специфичные зависимости добавляются в соответствующих платформенных модулях
}
