package components.root

import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInQuart
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import component.root.RootComponent
import component.root.RootComponent.Child.*
import components.auth.AuthContent
import components.main.MainContent
import components.room.RoomContent
import components.settings.SettingsContent
import components.skiko.SkikoContent
import ui.theme.AppTheme
import utils.isLargeScreen
import MultiplatformSettings

/**
 * Корневой композабл, который отображает текущий дочерний компонент
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
@Composable
fun RootContent(
    modifier: Modifier = Modifier,
    component: RootComponent,
    desktopContent: @Composable ((component: Any, isSpace: Boolean) -> Unit)? = null
) {
    val childStack by component.childStack.subscribeAsState()
    val state by component.state.collectAsState()

    val settings = component.settings
    val theme by settings.themeFlow.collectAsState()

    // Используем платформо-независимую функцию
    val isLargeScreen = isLargeScreen()

    // Запоминаем предыдущее состояние для анимации
    var wasLargeScreen by remember { mutableStateOf(isLargeScreen) }
    val isChangingLayout = wasLargeScreen != isLargeScreen

    // Если размер экрана изменился, обновляем состояние
    LaunchedEffect(isLargeScreen) {
        // Небольшая задержка для анимации
        kotlinx.coroutines.delay(50)
        wasLargeScreen = isLargeScreen
    }

    // Определяем, какую тему использовать на основе настроек
    val isDarkTheme = when (theme) {
        MultiplatformSettings.ThemeOption.THEME_SYSTEM -> null // null означает использовать системную тему
        MultiplatformSettings.ThemeOption.THEME_DARK -> true
        MultiplatformSettings.ThemeOption.THEME_LIGHT -> false
        else -> null
    }

    // Применяем тему на уровне всего контента
    AppTheme(isDarkTheme = isDarkTheme) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Отображаем desktopContent сверху для всех типов экранов, если он есть
                if (desktopContent != null) {
                    desktopContent(childStack.active.instance, false)
                }

                // Используем AnimatedContent для плавного перехода между разными типами навигации
                AnimatedContent(
                    targetState = isLargeScreen,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(500, easing = EaseOutQuart)) with
                                fadeOut(animationSpec = tween(500, easing = EaseInQuart))
                    },
                    modifier = Modifier.weight(1f),
                    label = "NavigationTypeAnimation"
                ) { targetIsLargeScreen ->
                    if (targetIsLargeScreen) {
                        // Для больших экранов используем NavigationRail слева
                        Row(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Анимированный NavigationRail
                            NavigationRail {
                                NavigationRailItem(
                                    selected = childStack.active.instance is MainChild,
                                    onClick = { component.onMainClicked() },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Главная") },
                                    label = { Text("Главная") }
                                )
                                NavigationRailItem(
                                    selected = childStack.active.instance is SettingsChild,
                                    onClick = { component.onSettingsClicked() },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Настройки") },
                                    label = { Text("Настройки") }
                                )
                                NavigationRailItem(
                                    selected = childStack.active.instance is SkikoChild,
                                    onClick = { component.onDevelopmentMapClicked() },
                                    icon = { Icon(Icons.Default.Map, contentDescription = "Карта развития") },
                                    label = { Text("Карта развития") }
                                )
                                NavigationRailItem(
                                    selected = childStack.active.instance is AuthChild,
                                    onClick = { component.onAuthClicked() },
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Профиль") },
                                    label = { Text("Профиль") }
                                )
                            }

                            // Контент справа от NavigationRail
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.background
                            ) {
                                RenderContent(component, null)
                            }
                        }
                    } else {
                        // Для маленьких экранов используем NavigationBar внизу
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Контент над NavigationBar
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                color = MaterialTheme.colorScheme.background
                            ) {
                                RenderContent(component, null)
                            }

                            // Нижняя навигация
                            NavigationBar {
                                NavigationBarItem(
                                    selected = childStack.active.instance is MainChild,
                                    onClick = { component.onMainClicked() },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Главная") },
                                    label = { Text("Главная") }
                                )
                                NavigationBarItem(
                                    selected = childStack.active.instance is SettingsChild,
                                    onClick = { component.onSettingsClicked() },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Настройки") },
                                    label = { Text("Настройки") }
                                )
                                NavigationBarItem(
                                    selected = childStack.active.instance is SkikoChild,
                                    onClick = { component.onDevelopmentMapClicked() },
                                    icon = { Icon(Icons.Default.Map, contentDescription = "Карта развития") },
                                    label = { Text("Карта развития") }
                                )
                                NavigationBarItem(
                                    selected = childStack.active.instance is AuthChild,
                                    onClick = { component.onAuthClicked() },
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Профиль") },
                                    label = { Text("Профиль") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderContent(
    component: RootComponent,
    desktopContent: @Composable ((component: Any, isSpace: Boolean) -> Unit)?
) {
    // Улучшенная анимация для переключения между экранами
    Children(
        stack = component.childStack,
        animation = stackAnimation(
            fade(animationSpec = tween(300)) +
                    scale(animationSpec = tween(300, easing = FastOutSlowInEasing))
        ),
        modifier = Modifier.fillMaxSize()
    ) { child ->
        val instance = child.instance
        when (instance) {
            is MainChild -> MainContent(instance.component)
            is SettingsChild -> SettingsContent(instance.component)
            is SkikoChild -> SkikoContent(instance.component)
            is AuthChild -> AuthContent(instance.component)
            is RoomChild -> RoomContent(instance.component)
        }
    }
}
