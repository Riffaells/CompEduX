package di

import logging.loggingModule
import org.kodein.di.DI

/**
 * Все модули приложения, собранные в один для импорта
 */
val allModules = DI.Module("allModules") {
    // Импортируем все модули в правильном порядке (от нижнего уровня к верхнему)

    // 0. Логирование (самый базовый уровень)
    import(loggingModule)

    // 1. Настройки и сериализация (базовый уровень)
    import(settingsModule)

    // 2. Доменный слой (интерфейсы)
    import(domainModule)

    // 3. Сетевой слой
    import(networkModule)

    // 4. Слой данных (реализации репозиториев)
    import(dataModule)

    // 5. Компоненты приложения
    import(storeModule)
    import(componentModule)
    import(technologyTreeModule)
}
