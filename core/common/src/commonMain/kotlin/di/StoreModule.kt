package di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import component.app.auth.store.*
import component.app.room.diagram.store.DiagramStore
import component.app.room.diagram.store.DiagramStoreFactory
import component.app.room.list.store.RoomListStore
import component.app.room.list.store.RoomListStoreFactory
import component.app.room.store.RoomStore
import component.app.room.store.RoomStoreFactory
import component.root.store.RootStore
import component.root.store.RootStoreFactory
import org.kodein.di.*

/**
 * Модуль для регистрации всех Store-компонентов приложения
 */
val storeModule = DI.Module("storeModule") {
    // Регистрация AuthStore как синглтон (используется во всем приложении)
    bindSingleton<AuthStore> {
        AuthStoreFactory(
            storeFactory = instance<StoreFactory>(),
            di = di
        ).create()
    }

    // Регистрация LoginStore как провайдер (создается для каждого экрана входа)
    bindProvider<LoginStore> {
        LoginStoreFactory(
            storeFactory = instance<StoreFactory>(),
            di = di
        ).create()
    }

    // Регистрация RegisterStore как провайдер (создается для каждого экрана регистрации)
    bindProvider<RegisterStore> {
        RegisterStoreFactory(
            storeFactory = instance<StoreFactory>(),
            di = di
        ).create()
    }

    // Регистрация ProfileStore как провайдер (создается для каждого экрана профиля)
    bindProvider<ProfileStore> {
        ProfileStoreFactory(
            storeFactory = instance<StoreFactory>(),
            di = di
        ).create()
    }

    bindProvider<RootStore> {
        RootStoreFactory(
            storeFactory = instance(),
            di = di
        ).create()
    }
    
    // Room stores
    bindProvider<RoomStore> {
        RoomStoreFactory(
            storeFactory = instance(),
            di = di
        ).create()
    }
    
    bindProvider<RoomListStore> {
        RoomListStoreFactory(
            storeFactory = instance(),
            di = di
        ).create()
    }
}
