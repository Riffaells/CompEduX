package components.root

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.*
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import component.root.RootComponent
import component.root.RootComponent.Child.*
import components.auth.AuthContent
import components.main.MainContent
import components.room.RoomContent
import components.settings.SettingsContent
import components.skiko.SkikoContent
import ui.theme.AppTheme
import utils.getScreenWidth
import settings.AppearanceSettings
import component.navigation.*
import component.root.store.RootStore
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

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
        kotlinx.coroutines.delay(50)
        wasLargeScreen = isLargeScreen
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
        component.onEvent(RootStore.Intent.Init)
    }

    val hazeState = remember { HazeState() }

    // Выбираем тип размытия в зависимости от темы
    val blurType = remember(isDarkTheme) {
        if (isDarkTheme == true) {
            // В темной теме используем более сильное размытие для лучшей видимости
            BlurType.ACRYLIC
        } else {
            // В светлой теме используем более легкое размытие
            BlurType.FROSTED
        }
    }

    // Создаем конфигурацию навигации
    val navigationConfig = remember {
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
        PaddingValues(
            // Не добавляем отступы, так как навигация будет поверх контента
            // Но оставляем небольшой отступ снизу и сбоку для лучшей читаемости
            start = if (isLargeScreen) 16.dp else 0.dp,
            bottom = if (!isLargeScreen) 16.dp else 0.dp
        )
    }

    // Применяем тему на уровне всего контента
    AppTheme(
        isDarkTheme = isDarkTheme,
        useBlackBackground = blackBackground
    ) {
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
                    // Основной контент занимает всё пространство
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState)
                    ) {
                        // Передаем contentPadding в RenderContent для правильной прокрутки
                        RenderContent(
                            component = component,
                            contentPadding = contentPadding
                        )
                    }

                    // Боковая навигация (для больших экранов)
                    if (sideNavAlpha > 0.01f) {
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
                                backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.75f),
                                useProgressiveBlur = true
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
                    }

                    // Нижняя навигация (для маленьких экранов)
                    if (bottomNavAlpha > 0.01f) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .zIndex(100f)
                                .graphicsLayer(alpha = bottomNavAlpha)
                        ) {
                            FloatingNavigationBar(
                                hazeState = hazeState,
                                blurType = blurType,
                                backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.75f),
                                useProgressiveBlur = true
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
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderContent(
    component: RootComponent,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    // Улучшенная анимация для переключения между экранами
    Children(
        stack = component.childStack,
        animation = stackAnimation(
            fade(animationSpec = tween(300)) +
                    scale(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                    slide(animationSpec = tween(400, easing = FastOutSlowInEasing))
        ),
        modifier = Modifier.fillMaxSize()
    ) { child ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            when (val instance = child.instance) {
                is MainChild -> MainContent(instance.component)
                is SettingsChild -> SettingsContent(instance.component)
                is SkikoChild -> SkikoContent(instance.component)
                is AuthChild -> AuthContent(instance.component)
                is RoomChild -> RoomContent(instance.component)
            }
        }
    }
}
