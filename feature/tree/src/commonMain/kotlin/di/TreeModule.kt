package di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import component.tree.DefaultTreeComponent
import component.tree.TreeComponentParams
import component.tree.store.TreeStore
import component.tree.store.TreeStoreFactory
import org.kodein.di.DI
import org.kodein.di.bindFactory
import org.kodein.di.bindProvider
import org.kodein.di.instance

/**
 * Модуль для регистрации компонентов дерева развития
 */
val treeModule = DI.Module("treeModule") {
    // Регистрация фабрики для TreeStore
    bindProvider<TreeStoreFactory> {
        TreeStoreFactory(
            storeFactory = instance(),
            di = di
        )
    }

    // Регистрация TreeStore
    bindProvider<TreeStore> {
        TreeStoreFactory(
            storeFactory = instance(),
            di = di
        ).create()
    }

    // Регистрация фабрики для TreeComponent
    bindFactory { params: TreeComponentParams ->
        DefaultTreeComponent(
            componentContext = params.componentContext,
            onBack = params.onBack,
            di = di
        )
    }
}
