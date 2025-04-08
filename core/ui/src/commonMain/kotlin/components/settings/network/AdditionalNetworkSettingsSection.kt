package components.settings.network

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import component.settings.SecondaryCategoryBlock
import components.settings.network.components.ProtocolSelector
import components.settings.network.components.DevelopmentStatus
import components.settings.network.components.PlannedFeatures
import compedux.core.ui.generated.resources.Res
import compedux.core.ui.generated.resources.network_additional_settings
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import ui.icon.RIcons

/**
 * Секция дополнительных настроек сети
 *
 * @param modifier Модификатор для настройки внешнего вида
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun AdditionalNetworkSettingsSection(
    modifier: Modifier = Modifier
) {
    SecondaryCategoryBlock(
        title = stringResource(Res.string.network_additional_settings),
        icon = RIcons.Settings,
        isExperimental = false,
        modifier = modifier
    ) {
        // Выбор протокола подключения
        ProtocolSelector(
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Информация о статусе разработки
        DevelopmentStatus(
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Планируемые функции
        PlannedFeatures()
    }
}
