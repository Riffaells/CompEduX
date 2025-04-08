package components.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Translate
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
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import ui.icon.RIcons

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class,
       androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun LanguageSettingsContent(
    state: SettingsStore.State,
    onAction: (SettingsStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var showRestartDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок
        Text(
            text = stringResource(Res.string.settings_category_language),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Блок выбора языка с чипсами
        CategoryBlock(
            title = stringResource(Res.string.appearance_language),
            icon = RIcons.Translate,
            accentColor = MaterialTheme.colorScheme.tertiary
        ) {
            Text(
                text = "Выберите предпочитаемый язык интерфейса:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Список доступных языков
            val languageOptions = listOf(
                Triple(stringResource(Res.string.appearance_language_ru), "ru", "🇷🇺"),
                Triple(stringResource(Res.string.appearance_language_en), "en", "🇺🇸"),
                Triple("Deutsch", "de", "🇩🇪"),
                Triple("Français", "fr", "🇫🇷"),
                Triple("Español", "es", "🇪🇸"),
                Triple("中文", "zh", "🇨🇳"),
                Triple("日本語", "ja", "🇯🇵")
            )

            // FlowRow для отображения чипсов языков в несколько строк
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                languageOptions.forEach { (label, value, flag) ->
                    val isSelected = state.language == value

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (!isSelected) {
                                onAction(SettingsStore.Intent.UpdateLanguage(value))
                                showRestartDialog = true
                            }
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = flag, modifier = Modifier.padding(end = 8.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = RIcons.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    )
                }
            }

            // Примечание о неполных переводах
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = RIcons.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        Text(
                            text = "Примечание",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.tertiary
                        )

                        Text(
                            text = "Некоторые языки находятся в стадии перевода и могут содержать неполный перевод интерфейса.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Диалог о необходимости перезапуска приложения
    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text("Изменение языка") },
            text = { Text("Для применения выбранного языка требуется перезапустить приложение.") },
            confirmButton = {
                Button(
                    onClick = {
                        showRestartDialog = false
                        // Здесь будет логика для перезапуска приложения
                        scope.launch {
                            // TODO: implement app restart
                        }
                    }
                ) {
                    Text("Перезапустить сейчас")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRestartDialog = false }) {
                    Text("Позже")
                }
            }
        )
    }
}
