package components.settings.experimental

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import compedux.core.ui.generated.resources.Res
import compedux.core.ui.generated.resources.*
import component.app.settings.store.SettingsStore
import component.settings.CategoryBlock
import component.settings.ExperimentalBadge
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import ui.icon.RIcons
import components.settings.experimental.components.ExperimentalFeatureItem

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ExperimentalSettingsContent(
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
        // Заголовок с предупреждением
        Text(
            text = stringResource(Res.string.settings_category_experimental),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )

        // Предупреждение
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(Res.string.experimental_warning),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Экспериментальные функции
        CategoryBlock(
            title = stringResource(Res.string.experimental_features),
            icon = RIcons.ExperimentLab,
            isExperimental = true
        ) {
            // Список экспериментальных функций
            val experimentalFeatures = listOf(
                Triple(
                    stringResource(Res.string.experimental_new_ui),
                    stringResource(Res.string.experimental_new_ui_desc),
                    state.useExperimentalUI
                ),
                Triple(
                    stringResource(Res.string.experimental_new_parser),
                    stringResource(Res.string.experimental_new_parser_desc),
                    state.useExperimentalParser
                ),
                Triple(
                    stringResource(Res.string.experimental_beta_api),
                    stringResource(Res.string.experimental_beta_api_desc),
                    state.useExperimentalAPI
                )
            )

            experimentalFeatures.forEachIndexed { index, (title, description, isEnabled) ->
                ExperimentalFeatureItem(
                    title = title,
                    description = description,
                    isEnabled = isEnabled,
                    onToggleChange = {
                        when (index) {
                            0 -> onAction(SettingsStore.Intent.UpdateExperimentalUI(it))
                            1 -> onAction(SettingsStore.Intent.UpdateExperimentalParser(it))
                            2 -> onAction(SettingsStore.Intent.UpdateExperimentalAPI(it))
                        }
                    }
                )

                if (index < experimentalFeatures.size - 1) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // Дополнительные экспериментальные параметры
        CategoryBlock(
            title = stringResource(Res.string.experimental_advanced),
            icon = RIcons.ExperimentAdvanced,
            isExperimental = true
        ) {
            var useMultithreading by remember { mutableStateOf(state.useMultithreading) }

            // Multithreading
            ExperimentalFeatureItem(
                title = stringResource(Res.string.experimental_multithreading),
                description = stringResource(Res.string.experimental_multithreading_desc),
                isEnabled = useMultithreading,
                onToggleChange = {
                    useMultithreading = it
                    onAction(SettingsStore.Intent.UpdateMultithreading(it))
                }
            )

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Поле для настройки количества потоков
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(Res.string.experimental_thread_count),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = stringResource(Res.string.experimental_thread_count_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                var sliderPosition by remember { mutableFloatStateOf(state.threadCount.toFloat()) }

                Slider(
                    value = sliderPosition,
                    onValueChange = {
                        sliderPosition = it
                        onAction(SettingsStore.Intent.UpdateThreadCount(it.toInt()))
                    },
                    valueRange = 1f..16f,
                    steps = 14,
                    enabled = useMultithreading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "1",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = sliderPosition.toInt().toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "16",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Секция для сброса экспериментальных настроек
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.experimental_reset_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.experimental_reset_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onAction(SettingsStore.Intent.ResetExperimentalSettings) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.experimental_reset_button))
                }
            }
        }
    }
}
