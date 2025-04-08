package component.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Компонент для отображения стилизованного блока категории в настройках
 *
 * @param title Заголовок блока категории
 * @param icon Иконка категории
 * @param isExperimental Флаг, указывающий, является ли категория экспериментальной
 * @param accentColor Акцентный цвет категории (если null, используется primary из темы)
 * @param containerColor Цвет контейнера (если null, используется surface из темы)
 * @param elevation Высота тени (по умолчанию 2dp)
 * @param content Содержимое блока категории
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBlock(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    isExperimental: Boolean = false,
    accentColor: Color? = null,
    containerColor: Color? = null,
    elevation: Int = 2,
    content: @Composable ColumnScope.() -> Unit
) {
    val primaryColor = accentColor ?: MaterialTheme.colorScheme.primary
    val bgColor = containerColor ?: MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = bgColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Иконка с градиентным фоном
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.15f),
                                    primaryColor.copy(alpha = 0.05f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                if (isExperimental) {
                    Spacer(modifier = Modifier.width(8.dp))
                    ExperimentalBadge()
                }
            }

            // Тонкая декоративная линия с градиентом
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(vertical = 4.dp, horizontal = 16.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.3f),
                                primaryColor.copy(alpha = 0.05f),
                                primaryColor.copy(alpha = 0.3f)
                            )
                        )
                    )
            )

            // Содержимое блока
            content()
        }
    }
}

/**
 * Версия CategoryBlock с предварительно настроенным вторичным стилем (для дополнительных категорий)
 */
@Composable
fun SecondaryCategoryBlock(
    title: String,
    icon: ImageVector,
    isExperimental: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    CategoryBlock(
        title = title,
        icon = icon,
        isExperimental = isExperimental,
        accentColor = MaterialTheme.colorScheme.secondary,
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        elevation = 1,
        content = content
    )
}
