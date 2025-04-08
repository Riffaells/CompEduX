package components.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import compedux.core.ui.generated.resources.*
import component.app.settings.SettingsComponent
import component.app.settings.store.SettingsStore
import component.settings.CategoryBlock
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import ui.icon.RIcons

/**
 * Экран с конкретной категорией настроек
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun SettingsCategoryScreen(
    modifier: Modifier = Modifier,
    component: SettingsComponent,
    category: SettingsComponent.SettingsCategory
) {
    val state by component.state.collectAsState()

    val categoryTitle = when (category) {
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = categoryTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { component.onBackFromCategory() }) {
                        Icon(
                            imageVector = RIcons.ArrowBack,
                            contentDescription = stringResource(Res.string.settings_back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = category,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
                },
                modifier = Modifier.animateContentSize()
            ) { currentCategory ->
                when (currentCategory) {
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

                    SettingsComponent.SettingsCategory.LANGUAGE -> LanguageSettingsContent(
                        state = state,
                        onAction = component::onAction,
                        modifier = Modifier.fillMaxSize()
                    )

                    SettingsComponent.SettingsCategory.NOTIFICATIONS -> NotificationsSettingsContent(
                        state = state,
                        onAction = component::onAction,
                        modifier = Modifier.fillMaxSize()
                    )

                    SettingsComponent.SettingsCategory.EXPERIMENTAL -> ExperimentalSettingsContent(
                        state = state,
                        onAction = component::onAction,
                        modifier = Modifier.fillMaxSize()
                    )

                    SettingsComponent.SettingsCategory.STORAGE -> StorageSettingsContent(
                        state = state,
                        onAction = component::onAction,
                        modifier = Modifier.fillMaxSize()
                    )

                    SettingsComponent.SettingsCategory.SYSTEM -> SystemSettingsContent(
                        state = state,
                        onAction = component::onAction,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * Временная заглушка для настроек хранилища
 */
@Composable
private fun StorageSettingsContent(
    state: SettingsStore.State,
    onAction: (SettingsStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    CategoryBlock(
        modifier = modifier.padding(16.dp),
        title = "Настройки хранилища",
        icon = Icons.Filled.Storage,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Text(
            text = "Настройки хранилища пока недоступны. Они будут добавлены в следующих версиях.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Временная заглушка для системных настроек
 */
@Composable
private fun SystemSettingsContent(
    state: SettingsStore.State,
    onAction: (SettingsStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    CategoryBlock(
        title = "Системные настройки",
        icon = Icons.Filled.Settings,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = "Системные настройки пока недоступны. Они будут добавлены в следующих версиях.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp)
        )
    }
}
