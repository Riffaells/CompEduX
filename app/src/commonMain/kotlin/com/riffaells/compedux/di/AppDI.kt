package com.riffaells.compedux.di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import di.allModules
import org.kodein.di.DI
import org.kodein.di.bindSingleton

// Основной DI контейнер приложения
val appDI = DI {
    // Общие зависимости для всех платформ
    bindSingleton<StoreFactory> { DefaultStoreFactory() }

    // Импортируем все модули
    import(allModules)

    // Платформенно-специфичные зависимости добавляются в соответствующих платформенных модулях
}
