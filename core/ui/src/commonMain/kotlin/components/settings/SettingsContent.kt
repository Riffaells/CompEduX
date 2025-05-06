package components.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import compedux.core.ui.generated.resources.*
import component.app.settings.SettingsComponent
import components.settings.base.SettingCategory
import components.settings.base.SettingCategoryItem
import components.settings.base.SettingsScaffold
import components.settings.model.CategoryState
import kotlinx.coroutines.delay
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
            is SettingsComponent.Child.CategoryChild -> {
                // Создаем соответствующий объект CategoryState на основе типа категории
                val categoryState = when (instance.category) {
                    SettingsComponent.SettingsCategory.APPEARANCE -> CategoryState.Appearance(
                        component = component,
                        title = stringResource(Res.string.settings_category_appearance)
                    )

                    SettingsComponent.SettingsCategory.LANGUAGE -> CategoryState.Language(
                        component = component,
                        title = stringResource(Res.string.settings_category_language)
                    )

                    SettingsComponent.SettingsCategory.NETWORK -> CategoryState.Network(
                        component = component,
                        title = stringResource(Res.string.settings_category_network)
                    )

                    SettingsComponent.SettingsCategory.SECURITY -> CategoryState.Security(
                        component = component,
                        title = stringResource(Res.string.settings_category_security)
                    )

                    SettingsComponent.SettingsCategory.NOTIFICATIONS -> CategoryState.Notifications(
                        component = component,
                        title = stringResource(Res.string.settings_category_notifications)
                    )

                    SettingsComponent.SettingsCategory.STORAGE -> CategoryState.Storage(
                        title = stringResource(Res.string.settings_category_storage)
                    )

                    SettingsComponent.SettingsCategory.EXPERIMENTAL -> CategoryState.Experimental(
                        component = component,
                        title = stringResource(Res.string.settings_category_experimental)
                    )

                    SettingsComponent.SettingsCategory.SYSTEM -> CategoryState.System(
                        title = stringResource(Res.string.settings_category_system)
                    )

                    SettingsComponent.SettingsCategory.PROFILE -> CategoryState.Profile(
                        component = component,
                        title = stringResource(Res.string.settings_category_profile)
                    )
                }

                SettingsCategoryScreen(
                    modifier = modifier,
                    categoryState = categoryState,
                    onBack = component::onBackFromCategory
                )
            }
        }
    }
}

/**
 * Экран с категориями настроек
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
private fun SettingsCategoriesScreen(
    modifier: Modifier = Modifier,
    component: SettingsComponent
) {
    // Получаем состояние из компонента
    val state by component.state.collectAsState()
    val scrollState = rememberScrollState()

    // Состояние для анимации элементов
    var showContent by remember { mutableStateOf(false) }

    // Запускаем анимацию появления контента с небольшой задержкой
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    SettingsScaffold(
        modifier = modifier,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 16.dp)
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(Res.string.settings_title),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )

                // Title header with animation
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
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Settings categories list
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
                            onClick = { component.onCategorySelected(category.category) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Add bottom padding for better spacing
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    )
}
