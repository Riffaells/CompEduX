package components.settings.experimental

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import component.app.settings.store.SettingsStore
import component.settings.section.CategoryBlock

@Composable
fun ExperimentalSettingsContent(
    state: SettingsStore.State,
    onAction: (SettingsStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    // Временная заглушка для экспериментальных настроек
    CategoryBlock(
        modifier = modifier.padding(16.dp),
        title = "Экспериментальные функции",
        icon = Icons.Filled.Science,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Text(
            text = "Экспериментальные функции в разработке. Они будут доступны в следующих версиях.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp)
        )
    }
}
