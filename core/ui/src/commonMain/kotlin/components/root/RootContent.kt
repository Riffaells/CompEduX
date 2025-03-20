package components.root

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import dev.chrisbanes.haze.hazeSource
import settings.AppearanceSettings
import ui.theme.AppTheme
import utils.getScreenWidth
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * Корневой композабл, который отображает текущий дочерний компонент
 */
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

    val screenWidth = getScreenWidth()
    val isLargeScreen = screenWidth > 840.dp

    // Анимируемые значения для навигации
    val navigationState = rememberNavigationState(isLargeScreen)

    // Определяем тему
    val isDarkTheme = when (theme) {
        AppearanceSettings.ThemeOption.THEME_SYSTEM -> null
        AppearanceSettings.ThemeOption.THEME_DARK -> true
        AppearanceSettings.ThemeOption.THEME_LIGHT -> false
        else -> null
    }

    // Инициализация
    LaunchedEffect(Unit) {
        component.onEvent(RootStore.Intent.Init)
    }

    val hazeState = remember { HazeState() }
    val blurType = BlurType.ACRYLIC

    // Состояние для управления drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Конфигурация навигации
    val navigationConfig = rememberNavigationConfig(component)

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

    // Padding для контента
    val contentPadding = remember(isLargeScreen) {
        PaddingValues()
    }

    // Модификатор безопасной области
    val safeAreaModifier = remember(isLargeScreen) {
        Modifier.padding(
            start = if (isLargeScreen) 100.dp else 0.dp,
            end = 16.dp
        )
    }

    AppTheme(
        isDarkTheme = isDarkTheme,
        useBlackBackground = blackBackground
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (desktopTopContent != null) {
                    desktopTopContent(childStack.active.instance, false)
                }

                Box(modifier = Modifier.weight(1f)) {
                    ModalNavigationDrawer(
                        modifier = Modifier.fillMaxSize(),
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Дополнительные настройки",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                HorizontalDivider()

                                // Анимированные элементы drawer
                                val drawerItems = listOf(
                                    "Профиль пользователя" to { scope.launch { drawerState.close() } },
                                    "Уведомления" to { scope.launch { drawerState.close() } },
                                    "Конфиденциальность" to { scope.launch { drawerState.close() } }
                                )

                                drawerItems.forEachIndexed { index, (text, onClick) ->
                                    var showItem by remember { mutableStateOf(false) }

                                    // Запускаем анимацию с задержкой для каждого элемента
                                    LaunchedEffect(drawerState.isOpen) {
                                        if (drawerState.isOpen) {
                                            delay(100L * index)
                                            showItem = true
                                        } else {
                                            showItem = false
                                        }
                                    }

                                    AnimatedVisibility(
                                        visible = showItem,
                                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                                    ) {
                                        NavigationDrawerItem(
                                            label = { Text(text) },
                                            selected = false,
                                            onClick = { onClick() }
                                        )
                                    }
                                }
                            }
                        }
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Основной контент
                            ContentPadding(isLargeScreen = isLargeScreen) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .hazeSource(state = hazeState)
                                    ) {
                                        RenderContent(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(contentPadding),
                                            component = component,
                                            contentPadding = contentPadding,
                                            safeAreaModifier = safeAreaModifier
                                        )
                                    }
                                }
                            }

                            // Боковая навигация
                            if (navigationState.sideNavAlpha > 0.01f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .zIndex(100f)
                                        .graphicsLayer(
                                            alpha = navigationState.sideNavAlpha,
                                            translationX = navigationState.sideNavOffsetX.value,
                                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                                        )
                                ) {
                                    FloatingNavigationRail(
                                        hazeState = hazeState,
                                        blurType = blurType,
                                        backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
                                        useProgressiveBlur = true,
                                        animationProgress = navigationState.sideNavAlpha
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

                            // Нижняя навигация
                            if (navigationState.bottomNavAlpha > 0.01f) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .zIndex(100f)
                                        .graphicsLayer(
                                            alpha = navigationState.bottomNavAlpha,
                                            translationY = navigationState.bottomNavOffsetY.value,
                                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                                        )
                                ) {
                                    FloatingNavigationBar(
                                        modifier = Modifier,
                                        hazeState = hazeState,
                                        blurType = blurType,
                                        backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
                                        useProgressiveBlur = true,
                                        animationProgress = navigationState.bottomNavAlpha
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
    }
}

@Composable
private fun RenderContent(
    modifier: Modifier,
    component: RootComponent,
    contentPadding: PaddingValues,
    safeAreaModifier: Modifier
) {
    Children(
        stack = component.childStack,
        animation = stackAnimation(
            fade(animationSpec = tween(300)) +
                    scale(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                    slide(animationSpec = tween(400, easing = FastOutSlowInEasing))
        ),
        modifier = Modifier.fillMaxSize()
    ) { child ->

        when (val instance = child.instance) {
            is MainChild -> MainContent(modifier, instance.component)
            is SettingsChild -> SettingsContent(modifier, instance.component)
            is SkikoChild -> SkikoContent(modifier, instance.component)
            is AuthChild -> AuthContent(instance.component)
            is RoomChild -> RoomContent(modifier, instance.component)
        }

    }
}

@Composable
private fun ContentPadding(
    isLargeScreen: Boolean,
    content: @Composable () -> Unit
) {
    val sideNavAlpha by animateFloatAsState(
        targetValue = if (isLargeScreen) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "SideNavAlpha"
    )

    val sideNavStartPadding by animateDpAsState(
        targetValue = if (isLargeScreen) 100.dp else 0.dp,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "SideNavStartPadding"
    )

    val sideNavEndPadding by animateDpAsState(
        targetValue = 16.dp,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "SideNavEndPadding"
    )

    val contentPadding = remember(sideNavStartPadding, sideNavEndPadding) {
        PaddingValues(
            start = sideNavStartPadding,
            end = sideNavEndPadding
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        content()
    }
}

@Composable
private fun rememberNavigationState(isLargeScreen: Boolean): NavigationState {
    val sideNavAlpha by animateFloatAsState(
        targetValue = if (isLargeScreen) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "SideNavAlpha"
    )

    val bottomNavAlpha by animateFloatAsState(
        targetValue = if (!isLargeScreen) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "BottomNavAlpha"
    )

    val sideNavOffsetX by animateDpAsState(
        targetValue = if (isLargeScreen) 0.dp else (-80).dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "SideNavOffsetX"
    )

    val bottomNavOffsetY by animateDpAsState(
        targetValue = if (!isLargeScreen) 0.dp else 80.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BottomNavOffsetY"
    )

    val sideNavStartPadding by animateDpAsState(
        targetValue = if (isLargeScreen) 16.dp else 0.dp,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "SideNavStartPadding"
    )

    val sideNavEndPadding by animateDpAsState(
        targetValue = if (isLargeScreen) 8.dp else 0.dp,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "SideNavEndPadding"
    )

    val sideNavPadding = remember(sideNavStartPadding, sideNavEndPadding) {
        PaddingValues(start = sideNavStartPadding, end = sideNavEndPadding)
    }

    return remember(sideNavAlpha, bottomNavAlpha, sideNavOffsetX, bottomNavOffsetY, sideNavPadding) {
        NavigationState(
            sideNavAlpha = sideNavAlpha,
            bottomNavAlpha = bottomNavAlpha,
            sideNavOffsetX = sideNavOffsetX,
            bottomNavOffsetY = bottomNavOffsetY,
            sideNavPadding = sideNavPadding
        )
    }
}

@Composable
private fun rememberNavigationConfig(component: RootComponent): NavigationConfig {
    return remember {
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
}
