package components.settings.network.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import component.settings.FuturisticFeatureCard
import component.settings.SectionHeader
import compedux.core.ui.generated.resources.Res
import compedux.core.ui.generated.resources.planned_features
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import ui.icon.RIcons

/**
 * Компонент для отображения планируемых функций
 *
 * @param modifier Модификатор для настройки внешнего вида
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun PlannedFeatures(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = stringResource(Res.string.planned_features)
        )

        // Список планируемых функций
        FuturisticFeatureCard(
            text = "Поддержка прокси-серверов",
            icon = RIcons.Proxy
        )

        FuturisticFeatureCard(
            text = "Настройка тайм-аутов подключения",
            icon = Icons.Default.Timer
        )

        FuturisticFeatureCard(
            text = "Мониторинг сетевой активности",
            icon = RIcons.NetworkCheck
        )

        FuturisticFeatureCard(
            text = "Продвинутое кэширование запросов",
            icon = RIcons.Cache
        )

        FuturisticFeatureCard(
            text = "Настройка DNS серверов",
            icon = RIcons.Dns
        )
    }
}
