package components.settings.network

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import component.settings.CategoryBlock
import compedux.core.ui.generated.resources.Res
import compedux.core.ui.generated.resources.network_protocols
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ProtocolsSection(
    selectedProtocol: NetworkProtocol,
    onProtocolSelected: (NetworkProtocol) -> Unit,
    modifier: Modifier = Modifier
) {
    CategoryBlock(
        title = stringResource(Res.string.network_protocols),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NetworkProtocol.values().forEach { protocol ->
                ProtocolItem(
                    protocol = protocol,
                    selected = protocol == selectedProtocol,
                    onClick = { onProtocolSelected(protocol) }
                )
            }
        }
    }
}

@Composable
private fun ProtocolItem(
    protocol: NetworkProtocol,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = protocol.displayName,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = protocol.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

enum class NetworkProtocol(val displayName: String, val description: String) {
    HTTP("HTTP", "Standard protocol for web communication"),
    HTTPS("HTTPS", "Secure HTTP protocol with encryption"),
    HTTP2("HTTP/2", "Improved HTTP protocol with multiplexing"),
    HTTP3("HTTP/3", "Latest HTTP protocol with QUIC transport")
}
