package components.settings.network.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import compedux.core.ui.generated.resources.Res
import compedux.core.ui.generated.resources.bandwidth_limit
import compedux.core.ui.generated.resources.bandwidth_limit_description
import compedux.core.ui.generated.resources.bandwidth_limit_value
import compedux.core.ui.generated.resources.speed_high
import compedux.core.ui.generated.resources.speed_low
import compedux.core.ui.generated.resources.speed_medium
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import settings.MultiplatformSettings
import ui.icon.RIcons

/**
 * Компонент настроек ограничения пропускной способности
 *
 * @param settings Настройки приложения
 * @param modifier Модификатор для настройки внешнего вида
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun BandwidthLimiterSettings(
    settings: MultiplatformSettings,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // Состояние для экспериментальных опций (инициализированное из настроек)
    var enableBandwidthLimiter by remember(settings.network.enableBandwidthLimitFlow.collectAsState().value) {
        mutableStateOf(settings.network.enableBandwidthLimitFlow.collectAsState().value)
    }
    var bandwidthLimitValue by remember(settings.network.bandwidthLimitKbpsFlow.collectAsState().value) {
        mutableFloatStateOf(settings.network.bandwidthLimitKbpsFlow.collectAsState().value / 1000f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Переключатель ограничения пропускной способности
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.bandwidth_limit),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Text(
                        text = stringResource(Res.string.bandwidth_limit_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = enableBandwidthLimiter,
                    onCheckedChange = {
                        enableBandwidthLimiter = it
                        coroutineScope.launch {
                            settings.network.saveEnableBandwidthLimit(it)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        // Слайдер для установки лимита скорости
        AnimatedVisibility(visible = enableBandwidthLimiter) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = RIcons.Bolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = stringResource(Res.string.bandwidth_limit_value, bandwidthLimitValue.toInt()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    // Слайдер с настраиваемым стилем
                    Slider(
                        value = bandwidthLimitValue,
                        onValueChange = {
                            bandwidthLimitValue = it
                            coroutineScope.launch {
                                settings.network.saveBandwidthLimitKbps((it * 1000).toInt())
                            }
                        },
                        valueRange = 1f..20f,
                        steps = 19,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                            activeTickColor = MaterialTheme.colorScheme.primaryContainer,
                            inactiveTickColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.shadow(2.dp)
                    )
                }

                // Метки скорости
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
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
            }
        }
    }
}
