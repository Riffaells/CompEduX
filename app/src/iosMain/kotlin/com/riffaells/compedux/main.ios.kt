package com.riffaells.compedux

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.riffaells.compedux.di.appDI
import component.root.DefaultRootComponent
import components.root.RootContent
import org.kodein.di.compose.withDI
import org.kodein.di.direct
import org.kodein.di.factory
import platform.UIKit.UIViewController
import ui.theme.AppTheme

fun MainViewController(): UIViewController {
    val lifecycle = LifecycleRegistry()

    // Создаем корневой компонент
    val rootComponentFactory: (com.arkivanov.decompose.ComponentContext, Any?, Any?) -> DefaultRootComponent =
        appDI.direct.factory()
    val rootComponent = rootComponentFactory(DefaultComponentContext(lifecycle), null, null)

    lifecycle.resume() // Активируем жизненный цикл

    return ComposeUIViewController {
        withDI(appDI) {
            AppTheme {
                RootContent(component = rootComponent)
            }
        }
    }
}
