package di

import component.app.room.DefaultRoomComponent
import component.app.room.RoomComponent
import component.app.room.RoomComponentParams
import component.app.room.achievement.AchievementComponentParams
import component.app.room.achievement.DefaultAchievementComponent
import component.app.room.achievement.store.AchievementStoreFactory
import component.app.room.detail.DefaultRoomDetailComponent
import component.app.room.detail.RoomDetailComponentParams
import component.app.room.detail.store.RoomDetailStoreFactory
import component.app.room.diagram.DefaultDiagramComponent
import component.app.room.diagram.DiagramComponentParams
import component.app.room.diagram.store.DiagramStoreFactory
import component.app.room.list.DefaultRoomListComponent
import component.app.room.list.RoomListComponentParams
import component.app.room.list.store.RoomListStoreFactory
import component.app.room.store.RoomStoreFactory
import org.kodein.di.*

val roomComponentModule = DI.Module("roomComponentModule") {
    // Store factories
    bind { provider { RoomStoreFactory(instance(), di) } }
    bind { provider { RoomListStoreFactory(instance(), di) } }
    bind { provider { RoomDetailStoreFactory(instance(), di) } }
    bind { provider { DiagramStoreFactory(instance(), di) } }
    bind { provider { AchievementStoreFactory(instance(), di) } }

    // Main room component
    bindFactory { params: RoomComponentParams ->
        DefaultRoomComponent(
            componentContext = params.componentContext,
            storeFactory = instance(),
            onBack = params.onBack,
            di = di
        )
    }

    // Room list component
    bindFactory { params: RoomListComponentParams ->
        DefaultRoomListComponent(
            params = params,
            di = di
        )
    }

    // Room detail component
    bindFactory { params: RoomDetailComponentParams ->
        DefaultRoomDetailComponent(
            params = params,
            di = di
        )
    }

    // Diagram component
    bindFactory { params: DiagramComponentParams ->
        DefaultDiagramComponent(
            componentContext = params.componentContext,
            di = di
        )
    }

    // Achievement component
    bindFactory { params: AchievementComponentParams ->
        DefaultAchievementComponent(
            componentContext = params.componentContext,
            di = di
        )
    }
} 