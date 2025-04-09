package component.settings.section

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import component.settings.badge.PlanningBadge

/**
 * Компонент для отображения списка будущих функций
 *
 * @param title Заголовок списка
 * @param features Список функций
 * @param icon Иконка для заголовка
 * @param initiallyExpanded Флаг начального состояния (развернуто/свернуто)
 * @param modifier Модификатор для стилизации компонента
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
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
                    .padding(horizontal = 12.dp, vertical = 10.dp),
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
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = if (isExpanded)
                        Icons.Outlined.VisibilityOff
                    else
                        Icons.Outlined.Visibility,
                    contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Список функций (только если развернуто)
            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    features.forEach { feature ->
                        FeatureItem(feature = feature)
                    }
                }
            }
        }
    }
}

/**
 * Элемент списка функций
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
        // Иконка статуса
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
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

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
        PlanningBadge(
            text = when (feature.status) {
                FeatureStatus.PLANNED -> "Планируется"
                FeatureStatus.IN_PROGRESS -> "В разработке"
                FeatureStatus.COMPLETED -> "Реализовано"
                FeatureStatus.FEATURED -> "Особенность"
            },
            planned = feature.status == FeatureStatus.PLANNED || feature.status == FeatureStatus.IN_PROGRESS
        )
    }
}

/**
 * Класс, представляющий будущую функцию
 */
data class FutureFeature(
    val title: String,
    val description: String = "",
    val status: FeatureStatus = FeatureStatus.PLANNED
)

/**
 * Статусы будущих функций
 */
enum class FeatureStatus {
    PLANNED,      // Запланирована на будущее
    IN_PROGRESS,  // В процессе разработки
    COMPLETED,    // Завершена, но еще не выпущена
    FEATURED      // Особая функция (приоритетная)
}
