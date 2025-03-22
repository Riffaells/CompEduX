package di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import org.kodein.di.DI
import org.kodein.di.bindSingleton

/**
 * Модуль для MVIKotlin, предоставляющий StoreFactory
 */
val mvikotlinModule = DI.Module("mvikotlinModule") {
    // Основная фабрика для создания Store
    bindSingleton<StoreFactory> {
        DefaultStoreFactory()
    }
}
