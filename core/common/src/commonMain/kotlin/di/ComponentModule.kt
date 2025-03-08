package di

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import component.app.main.DefaultMainComponent
import component.app.main.store.MainStoreFactory
import component.app.settings.DefaultSettingsComponent
import component.app.settings.store.SettingsStoreFactory
import component.root.DefaultRootComponent
import component.root.store.RootStoreFactory
import org.kodein.di.DI
import org.kodein.di.bindFactory
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

/**
 * Модуль компонентов, который предоставляет все необходимые зависимости для UI компонентов
 */
@OptIn(ExperimentalDecomposeApi::class)
val componentModule = DI.Module("component") {
    // Store Factory
    bindSingleton { DefaultStoreFactory() }

    // Store Factories
    bindProvider { RootStoreFactory(storeFactory = instance(), di = di) }
    bindProvider { MainStoreFactory(storeFactory = instance(), di = di) }
    bindProvider { SettingsStoreFactory(storeFactory = instance(), di = di) }

    // Фабрики компонентов с использованием data class для параметров
    bindFactory<MainComponentParams, DefaultMainComponent> { params ->
        DefaultMainComponent(
            componentContext = params.componentContext,
            onSettingsClicked = params.onSettingsClicked,
            di = di
        )
    }

    bindFactory<SettingsComponentParams, DefaultSettingsComponent> { params ->
        DefaultSettingsComponent(
            componentContext = params.componentContext,
            onBack = params.onBack,
            di = di
        )
    }

    bindFactory<RootComponentParams, DefaultRootComponent> { params ->
        DefaultRootComponent(
            componentContext = params.componentContext,
            webHistoryController = params.webHistoryController,
            deepLink = params.deepLink ?: DefaultRootComponent.DeepLink.None,
            storeFactory = instance(),
            di = di
        )
    }
}
