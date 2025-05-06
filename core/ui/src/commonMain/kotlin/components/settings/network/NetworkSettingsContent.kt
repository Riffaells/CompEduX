package components.settings.network

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Http
import androidx.compose.material.icons.outlined.NetworkWifi
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import component.app.settings.store.SettingsStore
import component.settings.badge.ExperimentalBadge
import component.settings.base.FuturisticSettingItem
import component.settings.base.FuturisticSlider
import component.settings.headers.SectionHeader
import component.settings.input.SettingTextField
import component.settings.input.SettingToggle
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NetworkSettingsContent(
    state: SettingsStore.State,
    onAction: (SettingsStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var showConnectionDialog by remember { mutableStateOf(false) }
    var showTimeoutDialog by remember { mutableStateOf(false) }
    var serverUrlState by remember { mutableStateOf(TextFieldValue(state.serverUrl)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with network icon
        SectionHeader(
            title = "Network Settings",
            icon = Icons.Outlined.NetworkWifi
        )

        // Server URL
        SettingTextField(
            title = "Server URL",
            description = "API endpoint for the application",
            value = serverUrlState,
            onValueChange = {
                serverUrlState = it
                onAction(SettingsStore.Intent.UpdateServerUrl(it.text))
            },
            trailingIcon = Icons.Outlined.Http
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // API Mode
        FuturisticSettingItem(
            title = "Experimental API",
            description = "Use experimental API features for testing",
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ExperimentalBadge(tooltipText = "This feature is experimental")
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = state.useExperimentalApi,
                        onCheckedChange = { onAction(SettingsStore.Intent.UpdateUseExperimentalApi(it)) }
                    )
                }
            }
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Bandwidth Limiting Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Speed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bandwidth Control",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Enable bandwidth limit
                SettingToggle(
                    title = "Enable Bandwidth Limit",
                    description = "Limit download/upload speeds",
                    isChecked = state.enableBandwidthLimit,
                    onCheckedChange = { onAction(SettingsStore.Intent.UpdateEnableBandwidthLimit(it)) }
                )

                // Bandwidth limit slider
                if (state.enableBandwidthLimit) {
                    Text(
                        text = "Bandwidth Limit: ${state.bandwidthLimitKbps} Kbps",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    FuturisticSlider(
                        value = state.bandwidthLimitKbps.toFloat() / 10000f, // Scale to 0-1 range
                        onValueChange = {
                            val newValue = (it * 10000).toInt().coerceIn(100, 10000)
                            onAction(SettingsStore.Intent.UpdateBandwidthLimitKbps(newValue))
                        },
                        valueText = "${state.bandwidthLimitKbps} Kbps",
                        showLabels = true,
                        labels = listOf("100 Kbps", "5 Mbps", "10 Mbps")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Connection Timeouts Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Connection Timeouts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Enable custom timeouts
                SettingToggle(
                    title = "Custom Timeouts",
                    description = "Override default connection timeouts",
                    isChecked = state.useCustomTimeouts,
                    onCheckedChange = { onAction(SettingsStore.Intent.UpdateUseCustomTimeouts(it)) }
                )

                if (state.useCustomTimeouts) {
                    // Connection timeout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Connection Timeout",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${state.connectionTimeoutSeconds} seconds",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { showConnectionDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit connection timeout")
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Read timeout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Read Timeout",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${state.readTimeoutSeconds} seconds",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { showTimeoutDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit read timeout")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Advanced options
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Advanced Network Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { /* Test connection */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.NetworkCheck, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Connection")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { /* Reset network settings */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset to Defaults")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Connection timeout dialog
    if (showConnectionDialog) {
        var timeoutValue by remember { mutableStateOf(state.connectionTimeoutSeconds.toString()) }

        AlertDialog(
            onDismissRequest = { showConnectionDialog = false },
            title = { Text("Connection Timeout") },
            text = {
                OutlinedTextField(
                    value = timeoutValue,
                    onValueChange = {
                        // Only allow numbers
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            timeoutValue = it
                        }
                    },
                    label = { Text("Seconds") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        timeoutValue.toIntOrNull()?.let { value ->
                            if (value in 1..300) { // Reasonable range check
                                onAction(SettingsStore.Intent.UpdateConnectionTimeoutSeconds(value))
                                showConnectionDialog = false
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConnectionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Read timeout dialog
    if (showTimeoutDialog) {
        var timeoutValue by remember { mutableStateOf(state.readTimeoutSeconds.toString()) }

        AlertDialog(
            onDismissRequest = { showTimeoutDialog = false },
            title = { Text("Read Timeout") },
            text = {
                OutlinedTextField(
                    value = timeoutValue,
                    onValueChange = {
                        // Only allow numbers
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            timeoutValue = it
                        }
                    },
                    label = { Text("Seconds") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        timeoutValue.toIntOrNull()?.let { value ->
                            if (value in 1..300) { // Reasonable range check
                                onAction(SettingsStore.Intent.UpdateReadTimeoutSeconds(value))
                                showTimeoutDialog = false
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimeoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
