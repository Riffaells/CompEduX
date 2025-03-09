package di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

// Модуль для Skiko компонентов
val skikoModule = DI.Module("skiko") {
    // Фабрика для Store Skiko компонента
    // Примечание: Этот модуль будет использоваться, когда компоненты Skiko будут реализованы
    // Пока оставляем его пустым
}
