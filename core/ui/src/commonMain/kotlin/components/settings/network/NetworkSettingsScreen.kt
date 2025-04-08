package components.settings.network

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import components.settings.NetworkSettingsContentWithState
import domain.MultiplatformSettings

/**
 * Главный экран настроек сети
 *
 * @param settings Настройки приложения
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun NetworkSettingsScreen(
    settings: MultiplatformSettings,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            SmallTopAppBar(
                title = { Text("Настройки сети") }
            )
        }
    ) { paddingValues ->
        NetworkSettingsContentWithState(
            settings = settings,
            modifier = Modifier.padding(paddingValues)
        )
    }
}
