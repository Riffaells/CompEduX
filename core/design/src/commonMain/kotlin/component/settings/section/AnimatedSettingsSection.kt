package component.settings.section

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Animated settings section with fade-in and expand animations.
 *
 * This component provides an animated container for settings that appear
 * with a smooth animation. Useful for progressive disclosure or delayed loading.
 *
 * @param title The text to display as section title
 * @param showContent Flag indicating whether to show the content
 * @param animationDelay Delay before starting the animation (in milliseconds)
 * @param showTopDivider Whether to show a divider above the section
 * @param showBottomDivider Whether to show a divider below the section
 * @param content The settings content to display inside the section
 *
 * Example usage:
 * ```
 * AnimatedSettingsSection(
 *     title = "Advanced Settings",
 *     showContent = showAdvancedSettings,
 *     animationDelay = 300
 * ) {
 *     // Advanced settings content
 * }
 * ```
 */
@Composable
fun AnimatedSettingsSection(
    title: String,
    showContent: Boolean,
    animationDelay: Int = 0,
    showTopDivider: Boolean = true,
    showBottomDivider: Boolean = true,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(showContent) {
        if (showContent) {
            delay(animationDelay.toLong())
            visible = true
        } else {
            visible = false
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) +
                expandVertically(animationSpec = tween(500)),
        exit = fadeOut()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top divider (if needed)
            if (showTopDivider) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }

            // Section title
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Section content
            content()

            // Bottom divider (if needed)
            if (showBottomDivider) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
