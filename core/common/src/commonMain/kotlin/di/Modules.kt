package di

import org.kodein.di.DI

val allModules = DI.Module("allModules") {
    importAll(
        authModule,
        settingsModule,
        componentModule
    )


}
