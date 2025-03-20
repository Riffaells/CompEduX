package di

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.arkivanov.mvikotlin.core.store.StoreFactory
import component.app.auth.DefaultAuthComponent
import component.app.main.DefaultMainComponent
import component.app.main.MainComponent
import component.app.main.MainComponentParams
import component.app.main.store.MainStoreFactory
import component.app.room.DefaultRoomComponent
import component.app.room.RoomComponent
import component.app.room.achievement.DefaultAchievementComponent
import component.app.room.achievement.store.AchievementStoreFactory
import component.app.room.diagram.DefaultDiagramComponent
import component.app.room.diagram.store.DiagramStoreFactory
import component.app.room.store.RoomStoreFactory
import component.app.settings.DefaultSettingsComponent
import component.app.settings.SettingsComponent
import component.app.skiko.DefaultSkikoComponent
import component.app.skiko.SkikoComponent
import component.root.DefaultRootComponent
import component.root.store.RootStoreFactory
import org.kodein.di.*

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
    bindProvider { RoomStoreFactory(storeFactory = instance(), di = di) }
    bindProvider { DiagramStoreFactory(storeFactory = instance(), di = di) }
    bindProvider { AchievementStoreFactory(storeFactory = instance(), di = di) }

    // Фабрики компонентов с использованием data class для параметров
    bindFactory { params: MainComponentParams ->
        DefaultMainComponent(
            componentContext = params.componentContext,
            onSettingsClicked = params.onSettingsClicked,
            onDevelopmentMapClicked = params.onDevelopmentMapClicked,
            onRoomClicked = params.onRoomClicked,
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

    // Auth компоненты
    bindFactory { params: AuthComponentParams ->
        DefaultAuthComponent(
            componentContext = params.componentContext,
            onBack = params.onBack,
            storeFactory = instance()
        )
    }
}
