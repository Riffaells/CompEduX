package di

import org.kodein.di.DI

/**
 * Все модули приложения, собранные в один для импорта
 */
val allModules = DI.Module("allModules") {
    // Компоненты UI и Store
    import(mvikotlinModule)
    import(componentModule)

    // Репозитории и данные

    // Настройки и утилиты
    import(serializationModule)

    importAll(
        settingsModule,
        componentModule
    )

}
