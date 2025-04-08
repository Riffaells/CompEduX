package components.settings.network

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import component.settings.CategoryBlock
import compedux.core.ui.generated.resources.Res
import compedux.core.ui.generated.resources.network_timeouts_title
import compedux.core.ui.generated.resources.network_custom_timeouts
import compedux.core.ui.generated.resources.network_custom_timeouts_desc
import compedux.core.ui.generated.resources.network_connection_timeout
import compedux.core.ui.generated.resources.network_connection_timeout_desc
import compedux.core.ui.generated.resources.network_read_timeout
import compedux.core.ui.generated.resources.network_read_timeout_desc
import compedux.core.ui.generated.resources.network_seconds_format
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

/**
 * A component for configuring network timeout settings
 *
 * @param isCustomTimeoutsEnabled Whether custom timeouts are enabled
 * @param onCustomTimeoutsEnabledChanged Handler for changing the custom timeouts status
 * @param connectionTimeoutSeconds Connection timeout value in seconds
 * @param onConnectionTimeoutChanged Handler for changing the connection timeout value
 * @param readTimeoutSeconds Read timeout value in seconds
 * @param onReadTimeoutChanged Handler for changing the read timeout value
 * @param modifier Modifier for configuring appearance
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun TimeoutsSection(
    isCustomTimeoutsEnabled: Boolean,
    onCustomTimeoutsEnabledChanged: (Boolean) -> Unit,
    connectionTimeoutSeconds: Int,
    onConnectionTimeoutChanged: (Int) -> Unit,
    readTimeoutSeconds: Int,
    onReadTimeoutChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    CategoryBlock(
        title = stringResource(Res.string.network_timeouts_title),
        icon = Icons.Default.Timer,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Toggle for custom timeouts
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.network_custom_timeouts),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(Res.string.network_custom_timeouts_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = isCustomTimeoutsEnabled,
                    onCheckedChange = onCustomTimeoutsEnabledChanged,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            // Timeout settings visible when custom timeouts are enabled
            AnimatedVisibility(
                visible = isCustomTimeoutsEnabled,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Connection timeout slider
                    TimeoutSlider(
                        value = connectionTimeoutSeconds,
                        onValueChange = onConnectionTimeoutChanged,
                        title = stringResource(Res.string.network_connection_timeout),
                        description = stringResource(Res.string.network_connection_timeout_desc)
                    )

                    // Read timeout slider
                    TimeoutSlider(
                        value = readTimeoutSeconds,
                        onValueChange = onReadTimeoutChanged,
                        title = stringResource(Res.string.network_read_timeout),
                        description = stringResource(Res.string.network_read_timeout_desc)
                    )
                }
            }
        }
    }
}

/**
 * A reusable timeout slider component
 *
 * @param value Current timeout value in seconds
 * @param onValueChange Handler for timeout value changes
 * @param title Title for the timeout setting
 * @param description Description of the timeout setting
 * @param modifier Modifier for configuring appearance
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
private fun TimeoutSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Title and value display
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = stringResource(Res.string.network_seconds_format, value),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Slider for timeout adjustment
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 5f..120f,
            steps = 23,  // (120-5)/5 - 1 = 23 steps for 5-second increments
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        // Labels for the slider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "5 ${stringResource(Res.string.network_seconds_format, 5).drop(2)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Text(
                text = "60 ${stringResource(Res.string.network_seconds_format, 60).drop(3)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Text(
                text = "120 ${stringResource(Res.string.network_seconds_format, 120).drop(4)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
