package com.riffaells.compedux.di

import di.allModules
import org.kodein.di.DI

// Основной DI контейнер приложения
val appDI = DI {
    // Импортируем все модули из common
    import(allModules)

    // Платформенно-специфичные зависимости добавляются в соответствующих платформенных модулях
}
