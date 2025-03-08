package com.riffaells.compedux.di

import di.componentModule
import di.settingsModule
import org.kodein.di.DI

// Основной DI контейнер приложения
val appDI = DI {
    // Общие зависимости

    import(settingsModule)
    import(componentModule)

}
