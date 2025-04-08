package components.settings.network.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import component.settings.FuturisticOptionChip
import component.settings.SectionHeader
import compedux.core.ui.generated.resources.Res
import compedux.core.ui.generated.resources.connection_type
import compedux.core.ui.generated.resources.connection_type_description
import compedux.core.ui.generated.resources.connection_type_tooltip
import compedux.core.ui.generated.resources.new_feature
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import ui.icon.RIcons

/**
 * Компонент выбора типа подключения
 */
@OptIn(ExperimentalResourceApi::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ConnectionTypeSelector(
    modifier: Modifier = Modifier
) {
    // Состояние для выбранного типа подключения
    var selectedConnectionType by remember { mutableStateOf("Автоматически") }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        SectionHeader(
            title = stringResource(Res.string.connection_type),
            badge = {
                NewFeatureBadge(
                    tooltipText = stringResource(Res.string.connection_type_tooltip),
                    titleText = stringResource(Res.string.new_feature)
                )
            }
        )

        Text(
            text = stringResource(Res.string.connection_type_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Список типов подключения с иконками
        val connectionTypes = listOf(
            Triple("Автоматически", RIcons.NetworkAuto, "Автоматический выбор сети"),
            Triple("Wi-Fi", RIcons.Wifi, "Использовать только Wi-Fi"),
            Triple("Мобильные данные", RIcons.Cellular, "Использовать только мобильные данные"),
            Triple("Ethernet", RIcons.Ethernet, "Использовать только проводное подключение")
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 2 // На маленьких экранах будет по 2 элемента в строке
        ) {
            connectionTypes.forEach { (type, icon, _) ->
                FuturisticFilterChip(
                    text = type,
                    selected = selectedConnectionType == type,
                    onClick = { selectedConnectionType = type },
                    icon = icon,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
