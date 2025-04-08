package components.settings.network.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import component.settings.FuturisticFilterChip
import component.settings.SectionHeader
import components.settings.NetworkProtocol
import ui.icon.RIcons
import compedux.core.ui.generated.resources.Res
import compedux.core.ui.generated.resources.protocol_description
import compedux.core.ui.generated.resources.protocol_selection
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

/**
 * Компонент выбора протокола подключения
 *
 * @param modifier Модификатор для настройки внешнего вида
 */
@OptIn(ExperimentalResourceApi::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ProtocolSelector(
    modifier: Modifier = Modifier
) {
    // Состояние для выбранного протокола
    var selectedProtocol by remember { mutableStateOf("HTTPS") }
    val protocols = listOf("HTTP", "HTTPS", "FTP", "SFTP")

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        SectionHeader(
            title = stringResource(Res.string.protocol_selection)
        )

        Text(
            text = stringResource(Res.string.protocol_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 4
        ) {
            protocols.forEach { protocol ->
                FuturisticOptionChip(
                    text = protocol,
                    selected = selectedProtocol == protocol,
                    onClick = {
                        selectedProtocol = protocol
                    }
                )
            }
        }
    }
}
