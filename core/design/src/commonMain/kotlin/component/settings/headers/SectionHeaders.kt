package component.settings.headers

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Section header with icon and title.
 *
 * This component provides a visually prominent header for a settings section,
 * featuring an icon and title with a divider below it.
 *
 * @param title The text to display as the header title
 * @param icon The vector icon to display next to the title
 * @param modifier Optional modifier for customizing the layout
 * @param accentColor Accent color for the icon and text (defaults to primary color)
 */
@Composable
fun SectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = accentColor.copy(alpha = 0.15f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = accentColor,
            modifier = Modifier.padding(start = 12.dp)
        )
    }

    Divider(
        color = accentColor.copy(alpha = 0.1f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    )
}

/**
 * Simple text header for subsections.
 *
 * A lightweight header for smaller subsections within settings,
 * with optional badge support.
 *
 * @param title The text to display as the header
 * @param badge Optional composable function to display a badge beside the header
 * @param modifier Optional modifier for customizing the layout
 */
@Composable
fun SimpleHeader(
    title: String,
    badge: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        if (badge != null) {
            Spacer(modifier = Modifier.width(8.dp))
            badge()
        }
    }
}
