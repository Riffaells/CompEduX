package com.riffaells.compedux

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.riffaells.compedux.di.appDI
import components.root.RootContent
import theme.AppTheme
import component.root.DefaultRootComponent
import org.jetbrains.skiko.wasm.onWasmReady
import org.kodein.di.compose.withDI
import org.kodein.di.direct
import org.kodein.di.factory

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val lifecycle = LifecycleRegistry()

    // Создаем корневой компонент
    val rootComponentFactory: (com.arkivanov.decompose.ComponentContext, Any?, Any?) -> DefaultRootComponent = appDI.direct.factory()
    val rootComponent = rootComponentFactory(DefaultComponentContext(lifecycle), null, null)

    lifecycle.resume() // Активируем жизненный цикл

    onWasmReady {
        CanvasBasedWindow("CompEduX") {
            withDI(appDI) {
                AppTheme {
                    RootContent(component = rootComponent)
                }
            }
        }
    }
}
