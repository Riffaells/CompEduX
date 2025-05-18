package di

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import component.DefaultTechnologyTreeComponent
import component.TechnologyTreeComponentParams
import component.app.auth.*
import component.app.auth.login.DefaultLoginComponent
import component.app.auth.register.DefaultRegisterComponent
import component.app.main.DefaultMainComponent
import component.app.main.MainComponentParams
import component.app.main.store.MainStoreFactory
import component.app.settings.DefaultSettingsComponent
import component.app.settings.SettingsComponentParams
import component.root.DefaultRootComponent
import component.root.RootComponentParams
import component.root.store.RootStoreFactory
import org.kodein.di.*

/**
 * Модуль компонентов приложения
 */
@OptIn(ExperimentalDecomposeApi::class)
val componentModule = DI.Module("componentModule") {
    // Store Factory
    bindSingleton<StoreFactory> {
        DefaultStoreFactory()
    }

    // Store Factories
    bindProvider { RootStoreFactory(storeFactory = instance(), di = di) }
    bindProvider { MainStoreFactory(storeFactory = instance(), di = di) }

    // Импортируем модуль компонентов комнат
    importOnce(roomComponentModule)

    // Фабрики компонентов с использованием data class для параметров
    bindFactory { params: MainComponentParams ->
        DefaultMainComponent(
            componentContext = params.componentContext,
            onSettings = params.onSettingsClicked,
            onDevelopmentMap = params.onDevelopmentMapClicked,
            onTree = params.onTreeClicked,
            onRoom = params.onRoomClicked,
            di = di
        )
    }

    bindFactory { params: SettingsComponentParams ->
        DefaultSettingsComponent(
            componentContext = params.componentContext,
            onBack = params.onBack,
            di = di
        )
    }

    bindFactory { params: RootComponentParams ->
        DefaultRootComponent(
            componentContext = params.componentContext,
            webHistoryController = params.webHistoryController,
            deepLink = params.deepLink ?: DefaultRootComponent.DeepLink.None,
            storeFactory = instance(),
            di = di
        )
    }

    bindFactory { params: TechnologyTreeComponentParams ->
        DefaultTechnologyTreeComponent(
            componentContext = params.componentContext,
            onBack = params.onBack,
            di = di
        )
    }

    // Авторизация
    bindFactory<AuthComponentParams, DefaultAuthComponent> { params ->
        DefaultAuthComponent(
            di = di,
            componentContext = params.componentContext,
            onBack = params.onBack,
        )
    }

    bindFactory<LoginComponentParams, DefaultLoginComponent> { params ->
        DefaultLoginComponent(
            di = di,
            componentContext = params.componentContext,
            onBack = params.onBack,
            onRegister = params.onRegister,
            onLoginSuccess = params.onLoginSuccess
        )
    }

    bindFactory<RegisterComponentParams, DefaultRegisterComponent> { params ->
        DefaultRegisterComponent(
            di = di,
            componentContext = params.componentContext,
            onBack = params.onBack,
            onLogin = params.onLogin,
            onRegisterSuccess = params.onRegisterSuccess
        )
    }

    bindFactory<ProfileComponentParams, DefaultProfileComponent> { params ->
        DefaultProfileComponent(
            di = di,
            componentContext = params.componentContext,
            onLogoutClicked = params.onLogout,
        )
    }


}
