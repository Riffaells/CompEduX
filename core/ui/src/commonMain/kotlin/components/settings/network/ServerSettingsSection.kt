package components.settings.network

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import component.settings.CategoryBlock
import component.settings.SecondaryCategoryBlock
import compedux.core.ui.generated.resources.Res
import compedux.core.ui.generated.resources.clear
import compedux.core.ui.generated.resources.experimental_api
import compedux.core.ui.generated.resources.hide_bandwidth_settings
import compedux.core.ui.generated.resources.server_settings
import compedux.core.ui.generated.resources.server_url
import compedux.core.ui.generated.resources.show_bandwidth_settings
import compedux.core.ui.generated.resources.bandwidth_settings
import compedux.core.ui.generated.resources.enable_bandwidth_limit
import compedux.core.ui.generated.resources.bandwidth_limit
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ServerSettingsSection(
    serverUrl: String,
    onServerUrlChange: (String) -> Unit,
    useExperimentalApi: Boolean,
    onExperimentalApiChange: (Boolean) -> Unit,
    enableBandwidthLimit: Boolean,
    onEnableBandwidthLimitChange: (Boolean) -> Unit,
    bandwidthLimitKbps: Float,
    onBandwidthLimitChange: (Float) -> Unit,
    isExpandedBandwidth: Boolean,
    onExpandedBandwidthChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    CategoryBlock(
        title = stringResource(Res.string.server_settings),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Server URL input
            OutlinedTextField(
                value = serverUrl,
                onValueChange = onServerUrlChange,
                label = { Text(stringResource(Res.string.server_url)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (serverUrl.isNotEmpty()) {
                        IconButton(onClick = { onServerUrlChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(Res.string.clear)
                            )
                        }
                    }
                }
            )

            // Experimental API toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(Res.string.experimental_api))
                Switch(
                    checked = useExperimentalApi,
                    onCheckedChange = onExperimentalApiChange
                )
            }

            // Bandwidth settings
            SecondaryCategoryBlock(
                title = stringResource(Res.string.bandwidth_settings),
                onClick = { onExpandedBandwidthChange(!isExpandedBandwidth) },
                expanded = isExpandedBandwidth
            ) {
                BandwidthSettings(
                    enableBandwidthLimit = enableBandwidthLimit,
                    onEnableBandwidthLimitChange = onEnableBandwidthLimitChange,
                    bandwidthLimitKbps = bandwidthLimitKbps,
                    onBandwidthLimitChange = onBandwidthLimitChange
                )
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun BandwidthSettings(
    enableBandwidthLimit: Boolean,
    onEnableBandwidthLimitChange: (Boolean) -> Unit,
    bandwidthLimitKbps: Float,
    onBandwidthLimitChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Enable bandwidth limiting toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(Res.string.enable_bandwidth_limit))
            Switch(
                checked = enableBandwidthLimit,
                onCheckedChange = onEnableBandwidthLimitChange
            )
        }

        // Bandwidth limit slider
        AnimatedVisibility(
            visible = enableBandwidthLimit,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                Text(
                    text = "${stringResource(Res.string.bandwidth_limit)}: ${bandwidthLimitKbps.toInt()} kbps",
                    style = MaterialTheme.typography.bodyMedium
                )

                Slider(
                    value = bandwidthLimitKbps,
                    onValueChange = onBandwidthLimitChange,
                    valueRange = 50f..5000f,
                    steps = 49,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }
    }
}
