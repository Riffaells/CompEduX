package components.settings.network.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import component.settings.ExperimentalBadge
import component.settings.FuturisticProgressBar
import component.settings.SectionHeader
import compedux.core.ui.generated.resources.Res
import compedux.core.ui.generated.resources.development_status
import compedux.core.ui.generated.resources.development_status_tooltip
import compedux.core.ui.generated.resources.in_development
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import ui.icon.RIcons

/**
 * Компонент для отображения статуса разработки
 *
 * @param progress Прогресс разработки (от 0.0 до 1.0)
 * @param modifier Модификатор для настройки внешнего вида
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun DevelopmentStatus(
    progress: Float = 0.3f,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        SectionHeader(
            title = stringResource(Res.string.development_status),
            badge = {
                ExperimentalBadge(
                    tooltipText = stringResource(Res.string.development_status_tooltip),
                    titleText = stringResource(Res.string.in_development),
                    icon = RIcons.ExperimentWIP
                )
            }
        )

        // Улучшенный футуристический прогресс-бар
        FuturisticProgressBar(progress = progress)
    }
}
