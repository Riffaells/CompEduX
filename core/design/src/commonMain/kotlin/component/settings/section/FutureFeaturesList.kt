package component.settings.section

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.Upcoming
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Component for displaying a list of upcoming features
 *
 * @param title List title
 * @param features List of features
 * @param icon Optional icon for the header
 * @param initiallyExpanded Initial expansion state
 * @param modifier Modifier for styling
 */
@Composable
fun FutureFeaturesList(
    title: String,
    features: List<FutureFeature>,
    icon: ImageVector = Icons.Outlined.Upcoming,
    initiallyExpanded: Boolean = true,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = LinearOutSlowInEasing
                    )
                )
        ) {
            // Заголовок с кнопкой развернуть/свернуть
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = if (isExpanded)
                        Icons.Outlined.VisibilityOff
                    else
                        Icons.Outlined.Visibility,
                    contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Список функций (только если развернуто)
            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    features.forEach { feature ->
                        FeatureItem(feature = feature)
                    }
                }

                // Добавляем отступ внизу
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Feature list item
 */
@Composable
private fun FeatureItem(
    feature: FutureFeature,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Иконка статуса с круглым фоном
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = when (feature.status) {
                        FeatureStatus.PLANNED -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        FeatureStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        FeatureStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        FeatureStatus.FEATURED -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (feature.status) {
                    FeatureStatus.PLANNED -> Icons.Default.Schedule
                    FeatureStatus.IN_PROGRESS -> Icons.Default.Update
                    FeatureStatus.COMPLETED -> Icons.Default.Check
                    FeatureStatus.FEATURED -> Icons.Default.Star
                },
                contentDescription = null,
                tint = when (feature.status) {
                    FeatureStatus.PLANNED -> MaterialTheme.colorScheme.outline
                    FeatureStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
                    FeatureStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
                    FeatureStatus.FEATURED -> MaterialTheme.colorScheme.secondary
                },
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Описание функции
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = feature.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (feature.description.isNotEmpty()) {
                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Бейдж статуса
        val statusColor = when (feature.status) {
            FeatureStatus.PLANNED -> MaterialTheme.colorScheme.outline
            FeatureStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
            FeatureStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
            FeatureStatus.FEATURED -> MaterialTheme.colorScheme.secondary
        }

        val backgroundColor = when (feature.status) {
            FeatureStatus.PLANNED -> MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            FeatureStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            FeatureStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
            FeatureStatus.FEATURED -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = backgroundColor,
            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
        ) {
            Text(
                text = when (feature.status) {
                    FeatureStatus.PLANNED -> "Планируется"
                    FeatureStatus.IN_PROGRESS -> "В разработке"
                    FeatureStatus.COMPLETED -> "Реализовано"
                    FeatureStatus.FEATURED -> "Особенность"
                },
                style = MaterialTheme.typography.labelMedium,
                color = statusColor,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

/**
 * Class representing a future feature
 */
data class FutureFeature(
    val title: String,
    val description: String = "",
    val status: FeatureStatus = FeatureStatus.PLANNED
)

/**
 * Status types for future features
 */
enum class FeatureStatus {
    PLANNED,      // Planned for future
    IN_PROGRESS,  // Currently in development
    COMPLETED,    // Completed but not yet released
    FEATURED      // Special priority feature
}
