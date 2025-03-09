package components.root

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
import components.main.MainContent
import components.settings.SettingsContent
import component.root.RootComponent
import component.root.RootComponent.Child
import theme.AppTheme

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

    val settings = component.settings
    val theme by settings.themeFlow.collectAsState()

    // Применяем тему на уровне всего контента, чтобы избежать мигания при переходах
    AppTheme(theme = theme) {
        // Отображаем текущий дочерний компонент с анимацией
        Surface(modifier = modifier.background(Color.Transparent).fillMaxSize()) {
            Children(
                stack = component.childStack,
                animation = stackAnimation(
                    scale() + fade()
                ),
                modifier = modifier.fillMaxSize()
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
}
