package components.root

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.*
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import component.navigation.*
import component.root.RootComponent
import component.root.RootComponent.Child.*
import component.root.store.RootStore
import components.auth.AuthContent
import components.main.MainContent
import components.room.RoomContent
import components.settings.SettingsContent
import components.skiko.SkikoContent
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock
import settings.AppearanceSettings
import ui.theme.AppTheme
import utils.getScreenWidth

/**
 * Корневой композабл, который отображает текущий дочерний компонент
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
@Composable
fun RootContent(
    modifier: Modifier = Modifier,
    component: RootComponent,
    desktopTopContent: @Composable ((component: Any, isSpace: Boolean) -> Unit)? = null
) {
    // Логируем начало композиции RootContent
    Napier.d("RootContent: Начало композиции")
    val startTime = Clock.System.now().toEpochMilliseconds()

    val childStack by component.childStack.subscribeAsState()
    val state by component.state.collectAsState()

    val settings = component.settings
    val appearance = settings.appearance
    val theme by appearance.themeFlow.collectAsState()
    val blackBackground by appearance.blackBackgroundFlow.collectAsState()

    // Получаем ширину экрана напрямую
    val screenWidth = getScreenWidth()
    // Определяем большой ли экран, используя пороговое значение
    val isLargeScreen = screenWidth > 840.dp

    // Запоминаем предыдущее состояние для анимации
    var wasLargeScreen by remember { mutableStateOf(isLargeScreen) }
    val isChangingLayout = wasLargeScreen != isLargeScreen


    // Если размер экрана изменился, обновляем состояние
    LaunchedEffect(isLargeScreen) {
        // Небольшая задержка для анимации
        Napier.d("RootContent: Изменение размера экрана, задержка перед обновлением")
        kotlinx.coroutines.delay(50)
        wasLargeScreen = isLargeScreen
        Napier.d("RootContent: Размер экрана обновлен")
    }

    // Определяем, какую тему использовать на основе настроек
    val isDarkTheme = when (theme) {
        AppearanceSettings.ThemeOption.THEME_SYSTEM -> null // null означает использовать системную тему
        AppearanceSettings.ThemeOption.THEME_DARK -> true
        AppearanceSettings.ThemeOption.THEME_LIGHT -> false
        else -> null
    }

    // Инициализируем состояние при первом запуске
    LaunchedEffect(Unit) {
        // Отложенная инициализация для ускорения запуска
        Napier.d("RootContent: Отложенная инициализация, задержка 50ms")
        val initStartTime = Clock.System.now().toEpochMilliseconds()
        kotlinx.coroutines.delay(50) // Уменьшаем задержку до 50ms
        component.onEvent(RootStore.Intent.Init)
        Napier.d(
            "RootContent: Инициализация завершена за ${
                Clock.System.now().toEpochMilliseconds() - initStartTime
            }ms"
        )
    }

    val hazeState = remember {
        Napier.d("RootContent: Создание HazeState")
        HazeState()
    }

    // Устанавливаем тип размытия NONE (без размытия)
    val blurType = BlurType.ACRYLIC

    // Создаем конфигурацию навигации
    val navigationConfig = remember {
        Napier.d("RootContent: Создание конфигурации навигации")
        NavigationConfig().apply {
            addItem(
                id = "main",
                icon = Icons.Default.Home,
                label = "Главная",
                contentDescription = "Перейти на главную страницу",
                onClick = { component.onMainClicked() }
            )
            addItem(
                id = "settings",
                icon = Icons.Default.Settings,
                label = "Настройки",
                contentDescription = "Перейти в настройки",
                onClick = { component.onSettingsClicked() }
            )
            addItem(
                id = "map",
                icon = Icons.Default.Map,
                label = "Карта развития",
                contentDescription = "Перейти на карту развития",
                onClick = { component.onDevelopmentMapClicked() }
            )
            addItem(
                id = "profile",
                icon = Icons.Default.Person,
                label = "Профиль",
                contentDescription = "Перейти в профиль",
                onClick = { component.onAuthClicked() }
            )
        }
    }

    // Определяем выбранный элемент навигации
    val selectedItemId = remember(childStack.active.instance) {
        Napier.d("RootContent: Определение выбранного элемента навигации")
        when (childStack.active.instance) {
            is MainChild -> "main"
            is SettingsChild -> "settings"
            is SkikoChild -> "map"
            is AuthChild -> "profile"
            else -> "main"
        }
    }

    // Анимируемые значения для плавных переходов
    val sideNavAlpha by animateFloatAsState(
        targetValue = if (isLargeScreen) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "SideNavAlpha"
    )

    val bottomNavAlpha by animateFloatAsState(
        targetValue = if (!isLargeScreen) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "BottomNavAlpha"
    )

    // Создаем PaddingValues для прокручиваемого контента
    val contentPadding = remember(isLargeScreen) {
        Napier.d("RootContent: Создание PaddingValues для контента")
        PaddingValues(
            // Не добавляем отступы, так как навигация будет поверх контента
            // Но оставляем небольшой отступ снизу и сбоку для лучшей читаемости
            start = if (isLargeScreen) 16.dp else 0.dp,
            bottom = if (!isLargeScreen) 16.dp else 0.dp
        )
    }

    // Применяем тему на уровне всего контента
    Napier.d("RootContent: Применение темы AppTheme")
    val themeStartTime = Clock.System.now().toEpochMilliseconds()

    AppTheme(
        isDarkTheme = isDarkTheme,
        useBlackBackground = blackBackground
    ) {
        Napier.d("RootContent: Тема применена за ${Clock.System.now().toEpochMilliseconds() - themeStartTime}ms")

        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Основная структура с Column для правильного размещения элементов
            Column(modifier = Modifier.fillMaxSize()) {
                // Элементы управления окном (если есть) всегда сверху
                if (desktopTopContent != null) {
                    desktopTopContent(childStack.active.instance, false)
                }

                // Основной контейнер для контента и навигации
                Box(modifier = Modifier.weight(1f)) {
                    // Основной контент занимает всё пространство без источника размытия
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Передаем contentPadding в RenderContent для правильной прокрутки
                        Napier.d("RootContent: Рендеринг основного контента")
                        val renderStartTime = Clock.System.now().toEpochMilliseconds()

                        // Применяем источник размытия к основному контенту
                        Napier.d("RootContent: Применение источника размытия (hazeSource) к основному контенту")

                        // Создаем Box с hazeSource, который будет источником для размытия
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .hazeSource(state = hazeState)
                        ) {
                            // Рендерим основной контент внутри источника размытия
                            RenderContent(
                                modifier = Modifier.fillMaxSize(),
                                component = component,
                                contentPadding = contentPadding
                            )
                        }

                        Napier.d(
                            "RootContent: Основной контент отрендерен за ${
                                Clock.System.now().toEpochMilliseconds() - renderStartTime
                            }ms"
                        )
                    }

                    // Боковая навигация (для больших экранов)
                    if (sideNavAlpha > 0.01f) {
                        Napier.d("RootContent: Рендеринг боковой навигации с размытием типа $blurType")
                        val sideNavStartTime = Clock.System.now().toEpochMilliseconds()
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(top = 16.dp)
                                .zIndex(100f)
                                .graphicsLayer(alpha = sideNavAlpha)
                        ) {
                            FloatingNavigationRail(
                                hazeState = hazeState,
                                blurType = blurType,
                                backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f), // Уменьшаем непрозрачность для лучшего эффекта размытия
                                useProgressiveBlur = true // Включаем прогрессивное размытие
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))

                                navigationConfig.items.forEach { item ->

                                    val isSelected = item.id == selectedItemId
                                    FloatingNavigationRailItem(
                                        selected = isSelected,
                                        onClick = { item.onClick() },
                                        icon = { item.IconContent() },
                                        label = {
                                            Text(
                                                text = item.label,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    )

                                }
                            }
                        }
                        Napier.d(
                            "RootContent: Боковая навигация отрендерена за ${
                                Clock.System.now().toEpochMilliseconds() - sideNavStartTime
                            }ms"
                        )
                    }

                    // Нижняя навигация (для маленьких экранов)
                    if (bottomNavAlpha > 0.01f) {
                        Napier.d("RootContent: Рендеринг нижней навигации с размытием типа $blurType")
                        val bottomNavStartTime = Clock.System.now().toEpochMilliseconds()
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .zIndex(100f)
                                .graphicsLayer(alpha = bottomNavAlpha)
                        ) {
                            FloatingNavigationBar(
                                modifier = Modifier,
                                hazeState = hazeState,
                                blurType = blurType,
                                backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f), // Уменьшаем непрозрачность для лучшего эффекта размытия
                                useProgressiveBlur = true // Включаем прогрессивное размытие
                            ) {
                                navigationConfig.items.forEach { item ->
                                    val isSelected = item.id == selectedItemId
                                    FloatingNavigationBarItem(
                                        selected = isSelected,
                                        onClick = { item.onClick() },
                                        icon = { item.IconContent() },
                                        label = {
                                            Text(
                                                text = item.label,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    )

                                }
                            }
                        }
                        Napier.d(
                            "RootContent: Нижняя навигация отрендерена за ${
                                Clock.System.now().toEpochMilliseconds() - bottomNavStartTime
                            }ms"
                        )
                    }
                }
            }
        }
    }

    // Логируем завершение композиции RootContent
    Napier.d("RootContent: Композиция завершена за ${Clock.System.now().toEpochMilliseconds() - startTime}ms")
}

@Composable
private fun RenderContent(
    modifier: Modifier,
    component: RootComponent,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    // Логируем начало рендеринга контента
    Napier.d("RenderContent: Начало рендеринга дочерних компонентов")
    val startTime = Clock.System.now().toEpochMilliseconds()

    // Улучшенная анимация для переключения между экранами
    Children(
        stack = component.childStack,
        animation = stackAnimation(
            fade(animationSpec = tween(300)) +
                    scale(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                    slide(animationSpec = tween(400, easing = FastOutSlowInEasing))
        ),
        modifier = modifier.fillMaxSize()
    ) { child ->
        Napier.d("RenderContent: Рендеринг компонента ${child.instance::class.simpleName}")
        val instanceStartTime = Clock.System.now().toEpochMilliseconds()

        when (val instance = child.instance) {
            is MainChild -> MainContent(modifier, instance.component)
            is SettingsChild -> SettingsContent(instance.component)
            is SkikoChild -> SkikoContent(instance.component)
            is AuthChild -> AuthContent(instance.component)
            is RoomChild -> RoomContent(instance.component)
        }

        Napier.d(
            "RenderContent: Компонент ${child.instance::class.simpleName} отрендерен за ${
                Clock.System.now().toEpochMilliseconds() - instanceStartTime
            }ms"
        )
    }

    // Логируем завершение рендеринга контента
    Napier.d(
        "RenderContent: Рендеринг дочерних компонентов завершен за ${
            Clock.System.now().toEpochMilliseconds() - startTime
        }ms"
    )
}
