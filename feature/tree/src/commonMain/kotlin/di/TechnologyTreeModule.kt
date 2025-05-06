package di

import component.TechnologyTreeStore
import component.TechnologyTreeStoreFactory
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

val technologyTreeModule = DI.Module("TechnologyTreeModule") {
    // Bind для SkikoStore
    bindProvider<TechnologyTreeStore> {
        TechnologyTreeStoreFactory(
            storeFactory = instance(),
            di = di
        ).create()
    }

    // Bind для SkikoStoreFactory
    bindProvider<TechnologyTreeStoreFactory> {
        TechnologyTreeStoreFactory(
            storeFactory = instance(),
            di = di
        )
    }
}