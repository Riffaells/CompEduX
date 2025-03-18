package components.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compedux.core.ui.generated.resources.*
import component.app.settings.SettingsComponent
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
                        Icon(RIcons.ArrowBack, contentDescription = stringResource(Res.string.settings_back))
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
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
