package di

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import component.app.auth.*
import component.app.auth.login.DefaultLoginComponent
import component.app.auth.login.LoginComponent
import component.app.auth.register.DefaultRegisterComponent
import component.app.auth.register.RegisterComponent
import component.app.main.DefaultMainComponent
import component.app.main.MainComponentParams
import component.app.main.store.MainStoreFactory
import component.app.room.DefaultRoomComponent
import component.app.room.RoomComponentParams
import component.app.room.achievement.AchievementComponentParams
import component.app.room.achievement.DefaultAchievementComponent
import component.app.room.achievement.store.AchievementStoreFactory
import component.app.room.diagram.DefaultDiagramComponent
import component.app.room.diagram.DiagramComponentParams
import component.app.room.diagram.store.DiagramStoreFactory
import component.app.room.store.RoomStoreFactory
import component.app.settings.DefaultSettingsComponent
import component.app.settings.SettingsComponentParams
import component.app.skiko.DefaultSkikoComponent
import component.app.skiko.SkikoComponentParams
import component.root.DefaultRootComponent
import component.root.RootComponentParams
import component.root.store.RootStoreFactory
import repository.auth.AuthRepository
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
    bindProvider { RoomStoreFactory(storeFactory = instance(), di = di) }
    bindProvider { DiagramStoreFactory(storeFactory = instance(), di = di) }
    bindProvider { AchievementStoreFactory(storeFactory = instance(), di = di) }

    // Navigation callbacks for auth flow
    bindConstant(tag = "onLoginSuccess") { { println("Login success") } } // This will be overridden by actual implementation
    bindConstant(tag = "onRegisterSuccess") { { println("Register success") } } // This will be overridden by actual implementation
    bindConstant(tag = "onLogout") { { println("Logout") } } // This will be overridden by actual implementation
    bindConstant(tag = "onRegister") { { println("Navigate to Register") } } // This will be overridden by actual implementation
    bindConstant(tag = "onLogin") { { println("Navigate to Login") } } // This will be overridden by actual implementation

    // Фабрики компонентов с использованием data class для параметров
    bindFactory { params: MainComponentParams ->
        DefaultMainComponent(
            componentContext = params.componentContext,
            onSettings = params.onSettingsClicked,
            onDevelopmentMap = params.onDevelopmentMapClicked,
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

    bindFactory { params: SkikoComponentParams ->
        DefaultSkikoComponent(
            componentContext = params.componentContext,
            onBack = params.onBack,
            di = di
        )
    }

    bindFactory { params: RoomComponentParams ->
        DefaultRoomComponent(
            componentContext = params.componentContext,
            storeFactory = instance(),
            onBack = params.onBack,
            di = di
        )
    }

    bindFactory { params: DiagramComponentParams ->
        DefaultDiagramComponent(
            componentContext = params.componentContext,
            di = di
        )
    }

    bindFactory { params: AchievementComponentParams ->
        DefaultAchievementComponent(
            componentContext = params.componentContext,
            di = di
        )
    }

    // Авторизация
    bindFactory<AuthComponentParams, DefaultAuthComponent> { params ->
        DefaultAuthComponent(
            di = di,
            componentContext = params.componentContext,
            onBack = params.onBack,
            storeFactory = instance(),
            authUseCases = instance()
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
            onLogout = params.onLogout,
            onBackClicked = params.onBackClicked
        )
    }
}
