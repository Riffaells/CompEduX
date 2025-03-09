package di

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import component.app.main.DefaultMainComponent
import component.app.main.MainComponentParams
import component.app.main.store.MainStoreFactory
import component.app.settings.DefaultSettingsComponent
import component.app.settings.SettingsComponentParams
import component.app.settings.store.SettingsStoreFactory
import component.root.DefaultRootComponent
import component.root.RootComponentParams
import component.root.store.RootStoreFactory
import component.app.skiko.DefaultSkikoComponent
import component.app.skiko.SkikoComponentParams
import component.app.skiko.store.SkikoStoreFactory
import org.kodein.di.DI
import org.kodein.di.bindFactory
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import component.app.auth.DefaultAuthComponent
import component.app.auth.AuthComponentParams
import component.app.auth.store.LoginStoreFactory
import component.app.auth.store.RegisterStoreFactory
import component.app.auth.store.ProfileStoreFactory

/**
 * Модуль компонентов, который предоставляет все необходимые зависимости для UI компонентов
 */
@OptIn(ExperimentalDecomposeApi::class)
val componentModule = DI.Module("componentModule") {
    // Store Factory
    bindSingleton { DefaultStoreFactory() }

    // Store Factories
    bindProvider { RootStoreFactory(storeFactory = instance(), di = di) }
    bindProvider { MainStoreFactory(storeFactory = instance(), di = di) }
    bindProvider { SettingsStoreFactory(storeFactory = instance(), di = di) }
    bindProvider { SkikoStoreFactory(storeFactory = instance(), di = di) }

    // Фабрики компонентов с использованием data class для параметров
    bindFactory<MainComponentParams, DefaultMainComponent> { params ->
        DefaultMainComponent(
            componentContext = params.componentContext,
            onSettingsClicked = params.onSettingsClicked,
            onDevelopmentMapClicked = params.onDevelopmentMapClicked,
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

    bindFactory<SkikoComponentParams, DefaultSkikoComponent> { params ->
        DefaultSkikoComponent(
            componentContext = params.componentContext,
            onBack = params.onBack,
            di = di
        )
    }

    bindFactory<AuthComponentParams, DefaultAuthComponent> { params ->
        DefaultAuthComponent(
            componentContext = params.componentContext,
            onBack = params.onBack,
            di = di
        )
    }

    // Auth Store Factories
    bindSingleton<component.app.auth.store.LoginStoreFactory.Factory> {
        LoginStoreFactory(storeFactory = instance(), di = di)
    }
    bindSingleton<component.app.auth.store.RegisterStoreFactory.Factory> {
        RegisterStoreFactory(storeFactory = instance(), di = di)
    }
    bindSingleton<component.app.auth.store.ProfileStoreFactory.Factory> {
        ProfileStoreFactory(storeFactory = instance(), di = di)
    }
}
