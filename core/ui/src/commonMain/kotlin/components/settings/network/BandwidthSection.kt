package components.settings.network

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import component.settings.CategoryBlock
import compedux.core.ui.generated.resources.Res
import compedux.core.ui.generated.resources.bandwidth_limit
import compedux.core.ui.generated.resources.bandwidth_limit_description
import compedux.core.ui.generated.resources.bandwidth_limit_value
import compedux.core.ui.generated.resources.enable_bandwidth_limit
import compedux.core.ui.generated.resources.kbps_unit
import compedux.core.ui.generated.resources.network_bandwidth_settings
import compedux.core.ui.generated.resources.optimization_note
import compedux.core.ui.generated.resources.speed_high
import compedux.core.ui.generated.resources.speed_low
import compedux.core.ui.generated.resources.speed_medium
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

/**
 * Компонент настроек ограничения пропускной способности
 *
 * @param isBandwidthLimitEnabled Включено ли ограничение пропускной способности
 * @param onBandwidthLimitEnabledChanged Обработчик изменения статуса ограничения пропускной способности
 * @param bandwidthLimitKbps Значение ограничения пропускной способности в Кбит/с
 * @param onBandwidthLimitChanged Обработчик изменения значения ограничения пропускной способности
 * @param modifier Модификатор для настройки внешнего вида
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun BandwidthSection(
    isBandwidthLimitEnabled: Boolean,
    onBandwidthLimitEnabledChanged: (Boolean) -> Unit,
    bandwidthLimitKbps: Int,
    onBandwidthLimitChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    CategoryBlock(
        title = stringResource(Res.string.network_bandwidth_settings),
        icon = Icons.Default.Speed,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Переключатель ограничения пропускной способности
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.enable_bandwidth_limit),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(Res.string.bandwidth_limit_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = isBandwidthLimitEnabled,
                    onCheckedChange = onBandwidthLimitEnabledChanged,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            // Слайдер для ограничения скорости, если ограничение включено
            AnimatedVisibility(
                visible = isBandwidthLimitEnabled,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    // Отображение значения скорости
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(Res.string.bandwidth_limit_value, bandwidthLimitKbps),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Слайдер с улучшенным стилем
                    Slider(
                        value = bandwidthLimitKbps.toFloat(),
                        onValueChange = { onBandwidthLimitChanged(it.toInt()) },
                        valueRange = 50f..5000f,
                        steps = 49,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    // Метки скорости
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(Res.string.speed_low),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )

                        Text(
                            text = stringResource(Res.string.speed_medium),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )

                        Text(
                            text = stringResource(Res.string.speed_high),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Примечание по оптимизации пропускной способности
                    Text(
                        text = stringResource(Res.string.optimization_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
