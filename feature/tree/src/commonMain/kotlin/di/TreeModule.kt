package di

import api.tree.NetworkTreeApi
import component.DefaultTechnologyTreeComponent
import component.TechnologyTreeComponent
import component.TechnologyTreeComponentParams
import component.TechnologyTreeStoreFactory
import logging.LoggingProvider
import org.kodein.di.*
import repository.auth.TokenRepository
import repository.tree.TreeRepository
import repository.tree.TreeRepositoryImpl

/**
 * Модуль для предоставления зависимостей, связанных с деревом технологий
 */
val treeModule = DI.Module("tree") {
    // Репозиторий технологического дерева
    bind<TreeRepository>() with singleton {
        TreeRepositoryImpl(
            treeApi = instance<NetworkTreeApi>(),
            tokenRepository = instance<TokenRepository>(),
            logger = instance<LoggingProvider>().withTag("TechnologyTreeRepository")
        )
    }


    // Фабрика для стора технологического дерева
    bindProvider { 
        TechnologyTreeStoreFactory(
            storeFactory = instance(),
            di = di
        )
    }

    // Фабрика для создания компонента технологического дерева
    bindFactory<TechnologyTreeComponentParams, TechnologyTreeComponent> { params ->
        DefaultTechnologyTreeComponent(
            componentContext = params.componentContext,
            courseId = params.courseId,
            onBack = params.onBack,
            di = di
        )
    }
} 