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
import components.settings.base.SettingCategory
import components.settings.base.SettingCategoryItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import ui.icon.RIcons

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

    // Состояние для анимации элементов
    var showContent by remember { mutableStateOf(false) }

    // Запускаем анимацию появления контента с небольшой задержкой
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = { component.onAction(SettingsStore.Intent.Back) }) {
                        Icon(RIcons.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { component.onDrawerButtonClicked() }) {
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
