package components.settings.base

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import components.layout.ConstrainedWidthLayout

/**
 * Base layout component for all settings screens with width constraint
 * to prevent excessive stretching on large screens.
 *
 * The component doesn't apply scrolling automatically to avoid nested scrolling issues.
 * You should handle scrolling in your content composable if needed.
 *
 * @param content The settings content to display
 * @param maxWidth Maximum content width in dp
 * @param modifier Modifier for the container
 * @param verticalSpacing Spacing between items in the settings
 */
@Composable
fun SettingsScaffold(
    content: @Composable () -> Unit,
    maxWidth: Int = 640,
    modifier: Modifier = Modifier,
    verticalSpacing: Int = 16
) {
    ConstrainedWidthLayout(
        modifier = modifier,
        maxWidth = maxWidth,
        content = { contentModifier ->
            Column(
                modifier = contentModifier,
                verticalArrangement = Arrangement.spacedBy(verticalSpacing.dp)
            ) {
                content()
            }
        }
    )
}
