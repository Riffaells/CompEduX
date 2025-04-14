package components.settings.appearance

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import component.app.settings.store.SettingsStore
import component.settings.animation.pulsatingGradient
import component.settings.animation.glowingPulse
import component.settings.badge.NewFeatureBadge
import component.settings.badge.PlanningBadge
import component.settings.badge.ExperimentalBadge
import component.settings.base.FuturisticSettingItem
import component.settings.base.FuturisticSlider
import component.settings.headers.FuturisticSectionHeader
import component.settings.headers.SectionHeader
import component.settings.headers.SimpleHeader
import component.settings.input.*
import component.settings.section.*
import component.settings.alert.WarningAlert
import component.settings.alert.AlertSeverity
import components.settings.base.SettingsScaffold
import compedux.core.ui.generated.resources.Res
import compedux.core.ui.generated.resources.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import settings.AppearanceSettings

@OptIn(ExperimentalResourceApi::class)
@Composable
fun AppearanceSettingsContent(
    state: SettingsStore.State,
    onAction: (SettingsStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    SettingsScaffold(
        modifier = modifier,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Header with section icon
                SectionHeader(
                    title = stringResource(Res.string.appearance_settings_title),
                    icon = Icons.Outlined.Palette
                )

                SettingsSection {
                    // Черный фон настройка
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(Res.string.appearance_black_bg),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
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

                    // Звездное небо настройка
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(Res.string.appearance_starry_sky),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Тема приложения
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(Res.string.appearance_theme),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Системная
                        Surface(
                            onClick = { onAction(SettingsStore.Intent.UpdateTheme(AppearanceSettings.ThemeOption.THEME_SYSTEM)) },
                            color = Color.Transparent,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = state.theme == AppearanceSettings.ThemeOption.THEME_SYSTEM,
                                    onClick = { onAction(SettingsStore.Intent.UpdateTheme(AppearanceSettings.ThemeOption.THEME_SYSTEM)) }
                                )
                                Text(
                                    text = stringResource(Res.string.appearance_theme_system),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }

                        // Светлая
                        Surface(
                            onClick = { onAction(SettingsStore.Intent.UpdateTheme(AppearanceSettings.ThemeOption.THEME_LIGHT)) },
                            color = if (state.theme == AppearanceSettings.ThemeOption.THEME_LIGHT)
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            else
                                Color.Transparent,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = state.theme == AppearanceSettings.ThemeOption.THEME_LIGHT,
                                        onClick = { onAction(SettingsStore.Intent.UpdateTheme(AppearanceSettings.ThemeOption.THEME_LIGHT)) }
                                    )
                                    Text(
                                        text = stringResource(Res.string.appearance_theme_light),
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.LightMode,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            }
                        }

                        // Темная
                        Surface(
                            onClick = { onAction(SettingsStore.Intent.UpdateTheme(AppearanceSettings.ThemeOption.THEME_DARK)) },
                            color = if (state.theme == AppearanceSettings.ThemeOption.THEME_DARK)
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            else
                                Color.Transparent,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = state.theme == AppearanceSettings.ThemeOption.THEME_DARK,
                                        onClick = { onAction(SettingsStore.Intent.UpdateTheme(AppearanceSettings.ThemeOption.THEME_DARK)) }
                                    )
                                    Text(
                                        text = stringResource(Res.string.appearance_theme_dark),
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun AppearanceSettingsContentPreview() {
    MaterialTheme {
        Surface {
            // Создаем пример состояния для предпросмотра
            val state = SettingsStore.State(
                theme = AppearanceSettings.ThemeOption.THEME_SYSTEM,
                blackBackground = true,
                starrySky = true,
                language = "ru"
            )

            AppearanceSettingsContent(
                state = state,
                onAction = { }
            )
        }
    }
}

// Вспомогательные компоненты для радио-опций
@Composable
private fun RadioOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null
) {
    Surface(
        onClick = onClick,
        color = if (selected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.surface.copy(alpha = 0f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )

            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
