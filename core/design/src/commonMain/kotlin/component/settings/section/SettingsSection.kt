package component.settings.section

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Basic settings section container.
 *
 * This component provides a standardized container for settings content
 * with consistent padding, spacing, and layout organization.
 *
 * For more specialized section components, see SettingsSections.kt
 *
 * @see SettingsSection
 * @see AnimatedSettingsSection
 */
@Composable
fun SettingsSection(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}
