package components.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import compedux.core.ui.generated.resources.Res
import compedux.core.ui.generated.resources.*
import component.app.settings.SettingsComponent
import component.app.settings.store.SettingsStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

/**
 * Композабл для отображения экрана настроек
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    component: SettingsComponent
) {
    val childStack by component.childStack.subscribeAsState()

    Children(
        stack = childStack,
        animation = stackAnimation(fade() + slide()),
    ) { child ->
        when (val instance = child.instance) {
            is SettingsComponent.Child.MainChild -> SettingsCategoriesScreen(modifier, component)
            is SettingsComponent.Child.CategoryChild -> SettingsCategoryScreen(
                modifier = modifier,
                component = component,
                category = instance.category
            )
        }
    }
}

/**
 * Экран с категориями настроек
 */
@Composable
fun SettingsCategoriesScreen(
    modifier: Modifier = Modifier,
    component: SettingsComponent
) {
    // Получаем состояние из компонента
    val state by component.state.collectAsState()

    // Состояние для управления drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Состояние для анимации элементов
    var showContent by remember { mutableStateOf(false) }

    // Запускаем анимацию появления контента с небольшой задержкой
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    // Создаем drawer с дополнительными опциями
    ModalNavigationDrawer(
        modifier = modifier,
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
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Настройки") },
                    navigationIcon = {
                        IconButton(onClick = { component.onAction(SettingsStore.Intent.Back) }) {
                            Icon(Icons.Rounded.ArrowBack, contentDescription = "Назад")
                        }
                    },
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Меню")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Анимация заголовка
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(animationSpec = tween(500)) +
                                slideInHorizontally(
                                    initialOffsetX = { -it / 2 },
                                    animationSpec = tween(500)
                                ),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = "Настройки приложения",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    // Список категорий настроек
                    val categories = listOf(
                        SettingCategory(
                            title = stringResource(Res.string.settings_category_profile),
                            description = stringResource(Res.string.settings_category_profile_desc),
                            icon = Icons.Default.AccountCircle,
                            category = SettingsComponent.SettingsCategory.PROFILE
                        ),
                        SettingCategory(
                            title = stringResource(Res.string.settings_category_appearance),
                            description = stringResource(Res.string.settings_category_appearance_desc),
                            icon = Icons.Default.Brush,
                            category = SettingsComponent.SettingsCategory.APPEARANCE
                        ),
                        SettingCategory(
                            title = stringResource(Res.string.settings_category_language),
                            description = stringResource(Res.string.settings_category_language_desc),
                            icon = Icons.Default.Language,
                            category = SettingsComponent.SettingsCategory.LANGUAGE
                        ),
                        SettingCategory(
                            title = stringResource(Res.string.settings_category_network),
                            description = stringResource(Res.string.settings_category_network_desc),
                            icon = Icons.Default.Web,
                            category = SettingsComponent.SettingsCategory.NETWORK
                        ),
                        SettingCategory(
                            title = stringResource(Res.string.settings_category_security),
                            description = stringResource(Res.string.settings_category_security_desc),
                            icon = Icons.Default.Security,
                            category = SettingsComponent.SettingsCategory.SECURITY
                        ),
                        SettingCategory(
                            title = stringResource(Res.string.settings_category_experimental),
                            description = stringResource(Res.string.settings_category_experimental_desc),
                            icon = Icons.Default.Science,
                            category = SettingsComponent.SettingsCategory.EXPERIMENTAL
                        )
                    )

                    categories.forEachIndexed { index, category ->
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(animationSpec = tween(500)) +
                                    slideInHorizontally(
                                        initialOffsetX = { it / 2 },
                                        animationSpec = tween(500 + index * 100)
                                    )
                        ) {
                            SettingCategoryItem(
                                category = category,
                                onClick = { component.onCategorySelected(category.category) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Экран с конкретной категорией настроек
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun SettingsCategoryScreen(
    component: SettingsComponent,
    category: SettingsComponent.SettingsCategory
) {
    val state by component.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (category) {
                            SettingsComponent.SettingsCategory.APPEARANCE -> stringResource(Res.string.settings_category_appearance)
                            SettingsComponent.SettingsCategory.LANGUAGE -> stringResource(Res.string.settings_category_language)
                            SettingsComponent.SettingsCategory.NETWORK -> stringResource(Res.string.settings_category_network)
                            SettingsComponent.SettingsCategory.SECURITY -> stringResource(Res.string.settings_category_security)
                            SettingsComponent.SettingsCategory.NOTIFICATIONS -> stringResource(Res.string.settings_category_notifications)
                            SettingsComponent.SettingsCategory.STORAGE -> stringResource(Res.string.settings_category_storage)
                            SettingsComponent.SettingsCategory.EXPERIMENTAL -> stringResource(Res.string.settings_category_experimental)
                            SettingsComponent.SettingsCategory.SYSTEM -> stringResource(Res.string.settings_category_system)
                            SettingsComponent.SettingsCategory.PROFILE -> stringResource(Res.string.settings_category_profile)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { component.onBackFromCategory() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = stringResource(Res.string.settings_back))
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (category) {
                SettingsComponent.SettingsCategory.APPEARANCE -> AppearanceSettingsContent(
                    state = state,
                    onAction = component::onAction,
                    modifier = Modifier.fillMaxSize()
                )

                SettingsComponent.SettingsCategory.NETWORK -> NetworkSettingsContent(
                    state = state,
                    onAction = component::onAction,
                    modifier = Modifier.fillMaxSize()
                )

                SettingsComponent.SettingsCategory.SECURITY -> SecuritySettingsContent(
                    state = state,
                    onAction = component::onAction,
                    modifier = Modifier.fillMaxSize()
                )

                SettingsComponent.SettingsCategory.PROFILE -> ProfileSettingsContent(
                    state = state,
                    onAction = component::onAction,
                    modifier = Modifier.fillMaxSize()
                )

                SettingsComponent.SettingsCategory.LANGUAGE -> TODO()
                SettingsComponent.SettingsCategory.NOTIFICATIONS -> TODO()
                SettingsComponent.SettingsCategory.STORAGE -> TODO()
                SettingsComponent.SettingsCategory.EXPERIMENTAL -> TODO()
                SettingsComponent.SettingsCategory.SYSTEM -> TODO()
            }
        }
    }
}

/**
 * Модель категории настроек
 */
data class SettingCategory(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val category: SettingsComponent.SettingsCategory
)

/**
 * Элемент категории настроек
 */
@Composable
fun SettingCategoryItem(
    category: SettingCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Перейти",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
