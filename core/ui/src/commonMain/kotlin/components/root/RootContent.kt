package components.root

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.unit.Dp
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

    // Анимация позиции для боковой навигации
    val sideNavOffsetX by animateDpAsState(
        targetValue = if (isLargeScreen) 0.dp else (-80).dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "SideNavOffsetX"
    )

    // Анимация позиции для нижней навигации
    val bottomNavOffsetY by animateDpAsState(
        targetValue = if (!isLargeScreen) 0.dp else 80.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BottomNavOffsetY"
    )

    // Анимация масштаба для обеих навигаций
    val sideNavScale by animateFloatAsState(
        targetValue = if (isLargeScreen) 1f else 0.8f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "SideNavScale"
    )

    val bottomNavScale by animateFloatAsState(
        targetValue = if (!isLargeScreen) 1f else 0.8f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "BottomNavScale"
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

    // Создаем модификатор безопасной области для контента, который учитывает навигацию
    val safeAreaModifier = remember(isLargeScreen) {
        Napier.d("RootContent: Создание модификатора безопасной области")
        Modifier.padding(
            // Добавляем отступ слева для боковой навигации на больших экранах
            start = if (isLargeScreen) 100.dp else 0.dp,
            // Добавляем отступ снизу для нижней навигации на маленьких экранах
            bottom = if (!isLargeScreen) 100.dp else 0.dp,
            // Добавляем дополнительный отступ для возможности прокрутки дальше
            end = 16.dp
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
                                contentPadding = contentPadding,
                                safeAreaModifier = safeAreaModifier
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
                                .graphicsLayer(
                                    alpha = sideNavAlpha,
                                    translationX = sideNavOffsetX.value,
                                    scaleX = sideNavScale,
                                    scaleY = sideNavScale,
                                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                                )
                        ) {
                            FloatingNavigationRail(
                                hazeState = hazeState,
                                blurType = blurType,
                                backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
                                useProgressiveBlur = true,
                                animationProgress = sideNavAlpha
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))

                                navigationConfig.items.forEachIndexed { index, item ->
                                    val isSelected = item.id == selectedItemId

                                    // Анимация для каждого элемента навигации
                                    val animState = rememberNavigationAnimationState(
                                        isVisible = isLargeScreen,
                                        itemIndex = index
                                    )

                                    Box(
                                        modifier = Modifier
                                            .graphicsLayer(
                                                alpha = animState.alpha,
                                                scaleX = animState.scale,
                                                scaleY = animState.scale,
                                                translationX = animState.offset.value
                                            )
                                    ) {
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
                                .graphicsLayer(
                                    alpha = bottomNavAlpha,
                                    translationY = bottomNavOffsetY.value,
                                    scaleX = bottomNavScale,
                                    scaleY = bottomNavScale,
                                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                                )
                        ) {
                            FloatingNavigationBar(
                                modifier = Modifier,
                                hazeState = hazeState,
                                blurType = blurType,
                                backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
                                useProgressiveBlur = true,
                                animationProgress = bottomNavAlpha
                            ) {
                                navigationConfig.items.forEachIndexed { index, item ->
                                    val isSelected = item.id == selectedItemId

                                    // Анимация для каждого элемента нижней навигации
                                    val animState = rememberNavigationAnimationState(
                                        isVisible = !isLargeScreen,
                                        itemIndex = index
                                    )

                                    Box(
                                        modifier = Modifier
                                            .graphicsLayer(
                                                alpha = animState.alpha,
                                                scaleX = animState.scale,
                                                scaleY = animState.scale,
                                                translationY = -animState.offset.value
                                            )
                                    ) {
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
    contentPadding: PaddingValues = PaddingValues(0.dp),
    safeAreaModifier: Modifier = Modifier
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
        modifier = Modifier
            .fillMaxSize()
    ) { child ->
        Napier.d("RenderContent: Рендеринг компонента ${child.instance::class.simpleName}")
        val instanceStartTime = Clock.System.now().toEpochMilliseconds()

        when (val instance = child.instance) {
            is MainChild -> MainContent(modifier, instance.component)
            is SettingsChild -> SettingsContent(modifier, instance.component)
            is SkikoChild -> SkikoContent(modifier, instance.component)
            is AuthChild -> AuthContent(modifier, instance.component)
            is RoomChild -> RoomContent(modifier, instance.component)
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

/**
 * Обертка для контента, которая добавляет невидимые элементы для прокрутки,
 * чтобы пользователь мог прокрутить контент за пределы навигации
 */


// Дополнительно анимируем состояние для навигационных элементов
// Создадим класс для анимации появления/исчезновения элементов навигации
@Composable
private fun rememberNavigationAnimationState(
    isVisible: Boolean,
    baseDelay: Long = 0,
    itemIndex: Int = 0
): NavigationItemAnimationState {
    // Анимация видимости (alpha) с задержкой для каскадного эффекта
    val delay = baseDelay + (itemIndex * 50L)
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = delay.toInt(),
            easing = FastOutSlowInEasing
        ),
        label = "NavItemAlpha"
    )

    // Анимация позиции (смещение)
    val offset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 20.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
            visibilityThreshold = 1.dp
        ),
        label = "NavItemOffset"
    )

    // Анимация масштаба
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = delay.toInt(),
            easing = FastOutSlowInEasing
        ),
        label = "NavItemScale"
    )

    return remember(alpha, offset, scale) {
        NavigationItemAnimationState(alpha, offset, scale)
    }
}

// Класс для хранения состояния анимации элемента навигации
data class NavigationItemAnimationState(
    val alpha: Float,
    val offset: Dp,
    val scale: Float
)
