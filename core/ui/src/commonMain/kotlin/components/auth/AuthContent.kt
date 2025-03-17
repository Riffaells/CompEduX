package components.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import component.app.auth.AuthComponent
import component.app.auth.AuthComponent.Child.*

/**
 * Основной композабл для отображения компонентов аутентификации
 */
@Composable
fun AuthContent(modifier: Modifier, component: AuthComponent) {
    // Отображаем текущий дочерний компонент с анимацией
    Children(
        stack = component.childStack,
        animation = stackAnimation(
            fade() + scale()
        )
    ) { child ->
        when (val instance = child.instance) {
            is LoginChild -> LoginContent(instance.component)
            is RegisterChild -> RegisterContent(instance.component)
            is ProfileChild -> ProfileContent(instance.component)
        }
    }
}
