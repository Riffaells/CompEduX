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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import component.settings.section.CategoryBlock
import components.settings.appearance.AppearanceSettingsContent
import components.settings.base.SettingsScaffold
import components.settings.experimental.ExperimentalSettingsContent
import components.settings.language.LanguageSettingsContent
import components.settings.model.CategoryState
import components.settings.network.NetworkSettingsContent
import components.settings.notifications.NotificationsSettingsContent
import components.settings.profile.ProfileSettingsContent
import components.settings.security.SecuritySettingsContent
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import settings.NetworkSettings
import ui.icon.RIcons

/**
 * Screen with specific settings category
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun SettingsCategoryScreen(
    modifier: Modifier = Modifier,
    categoryState: CategoryState?,
    onBack: () -> Unit
) {
    SettingsScaffold(
        modifier = modifier,
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(categoryState?.title ?: "") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(RIcons.ArrowBack, contentDescription = "Назад")
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
                    when (categoryState) {
                        is CategoryState.Appearance -> {
                            AppearanceSettingsContent(
                                state = categoryState.component.state.collectAsState().value,
                                onAction = categoryState.component::onAction,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        is CategoryState.Language -> {
                            LanguageSettingsContent(
                                state = categoryState.component.state.collectAsState().value,
                                onAction = categoryState.component::onAction,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        is CategoryState.Storage -> {
                            StorageSettingsContent()
                        }
                        is CategoryState.System -> {
                            SystemSettingsContent()
                        }
                        is CategoryState.Network -> {
                            NetworkSettingsContent(
                                state = categoryState.component.state.collectAsState().value,
                                onAction = categoryState.component::onAction,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        is CategoryState.Security -> {
                            SecuritySettingsContent(
                                state = categoryState.component.state.collectAsState().value,
                                onAction = categoryState.component::onAction,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        is CategoryState.Notifications -> {
                            NotificationsSettingsContent(
                                state = categoryState.component.state.collectAsState().value,
                                onAction = categoryState.component::onAction,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        is CategoryState.Experimental -> {
                            ExperimentalSettingsContent(
                                state = categoryState.component.state.collectAsState().value,
                                onAction = categoryState.component::onAction,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        is CategoryState.Profile -> {
                            ProfileSettingsContent(
                                state = categoryState.component.state.collectAsState().value,
                                onAction = categoryState.component::onAction,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        null -> {
                            Text(text = "Категория настроек не найдена")
                        }
                    }
                }
            }
        }
    )
}

/**
 * Temporary placeholder for storage settings
 */
@Composable
private fun StorageSettingsContent(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    SettingsScaffold(
        modifier = modifier,
        content = {
            Box(modifier = Modifier.verticalScroll(scrollState)) {
                CategoryBlock(
                    title = "Настройки хранилища",
                    icon = Icons.Filled.Storage,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Настройки хранилища пока недоступны. Они будут добавлены в следующих версиях.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    )
}

/**
 * Temporary placeholder for system settings
 */
@Composable
private fun SystemSettingsContent(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    SettingsScaffold(
        modifier = modifier,
        content = {
            Box(modifier = Modifier.verticalScroll(scrollState)) {
                CategoryBlock(
                    title = "Системные настройки",
                    icon = Icons.Filled.Settings,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Системные настройки пока недоступны. Они будут добавлены в следующих версиях.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    )
}
