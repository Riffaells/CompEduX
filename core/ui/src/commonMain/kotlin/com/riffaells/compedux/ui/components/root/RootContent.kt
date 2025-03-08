package com.riffaells.compedux.ui.components.root

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.riffaells.compedux.ui.components.main.MainContent
import com.riffaells.compedux.ui.components.settings.SettingsContent
import component.root.RootComponent
import component.root.RootComponent.Child

/**
 * Корневой композабл, который отображает текущий дочерний компонент
 */
@Composable
fun RootContent(
    modifier: Modifier = Modifier,
    component: RootComponent,
    desktopContent: @Composable ((component: Any, isSpace: Boolean) -> Unit)? = null
) {
    // Получаем состояние из компонента
    val state by component.state.collectAsState()

    // Отображаем текущий дочерний компонент с анимацией
    Children(
        stack = component.childStack,
        animation = stackAnimation(fade() + scale()),
        modifier = modifier
    ) { child ->
        val instance = child.instance

        if (desktopContent != null) {
            // Для десктопа используем кастомный контент
            Column() {
                desktopContent(instance, false)
            when (instance) {
                is Child.MainChild -> MainContent(instance.component)
                is Child.SettingsChild -> SettingsContent(instance.component)
            }
            }
        } else {
            // Для других платформ используем стандартный контент
            when (instance) {
                is Child.MainChild -> MainContent(instance.component)
                is Child.SettingsChild -> SettingsContent(instance.component)
            }
        }
    }
}
