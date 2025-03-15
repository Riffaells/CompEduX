package components.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import component.app.settings.store.SettingsStore
import settings.AppearanceSettings
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import compedux.core.ui.generated.resources.Res
import compedux.core.ui.generated.resources.*

@OptIn(ExperimentalResourceApi::class)
@Composable
fun AppearanceSettingsContent(
    state: SettingsStore.State,
    onAction: (SettingsStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок
        Text(
            text = stringResource(Res.string.appearance_settings_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Выбор темы
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(Res.string.appearance_theme),
                    style = MaterialTheme.typography.titleMedium
                )

                // Радио-кнопки для выбора темы
                val themeOptions = listOf(
                    stringResource(Res.string.appearance_theme_system) to AppearanceSettings.ThemeOption.THEME_SYSTEM,
                    stringResource(Res.string.appearance_theme_light) to AppearanceSettings.ThemeOption.THEME_LIGHT,
                    stringResource(Res.string.appearance_theme_dark) to AppearanceSettings.ThemeOption.THEME_DARK
                )

                themeOptions.forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.theme == value,
                            onClick = { onAction(SettingsStore.Intent.UpdateTheme(value)) }
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        // Переключатель для черного фона
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(Res.string.appearance_black_bg),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(Res.string.appearance_black_bg_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = state.blackBackground,
                    onCheckedChange = { onAction(SettingsStore.Intent.UpdateBlackBackground(it)) },
                    enabled = state.theme != AppearanceSettings.ThemeOption.THEME_LIGHT
                )
            }
        }

        // Переключатель для звездного неба
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(Res.string.appearance_starry_sky),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(Res.string.appearance_starry_sky_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = state.starrySky,
                    onCheckedChange = { onAction(SettingsStore.Intent.UpdateStarrySky(it)) }
                )
            }
        }

        // Выбор языка
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(Res.string.appearance_language),
                    style = MaterialTheme.typography.titleMedium
                )

                // Радио-кнопки для выбора языка
                val languageOptions = listOf(
                    stringResource(Res.string.appearance_language_ru) to "ru",
                    stringResource(Res.string.appearance_language_en) to "en"
                )

                languageOptions.forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.language == value,
                            onClick = { onAction(SettingsStore.Intent.UpdateLanguage(value)) }
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
