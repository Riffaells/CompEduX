package di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import component.app.skiko.store.SkikoStore
import component.app.skiko.store.SkikoStoreFactory
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

// Модуль для Skiko компонентов
val skikoModule = DI.Module("skiko") {
    // Фабрика для Store Skiko компонента
    bindProvider<SkikoStoreFactory> {
        SkikoStoreFactory(
            storeFactory = instance<StoreFactory>(),
            di = di
        )
    }
}
