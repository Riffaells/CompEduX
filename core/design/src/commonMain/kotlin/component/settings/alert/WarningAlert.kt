package component.settings.alert

import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Modern alert component with improved design and smooth animations
 *
 * @param title Alert title
 * @param message Alert message text
 * @param icon Optional custom icon for the alert
 * @param isVisible Visibility flag
 * @param onDismiss Callback function when alert is dismissed
 * @param severity Alert severity level
 * @param closeButtonContentDescription Accessibility description for close button
 * @param modifier Modifier for styling
 */
@Composable
fun WarningAlert(
    title: String,
    message: String,
    icon: ImageVector? = null,
    isVisible: Boolean = true,
    onDismiss: () -> Unit = {},
    severity: AlertSeverity = AlertSeverity.INFO,
    closeButtonContentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    var isShown by remember { mutableStateOf(isVisible) }

    val alertIcon = icon ?: when (severity) {
        AlertSeverity.WARNING, AlertSeverity.ERROR -> Icons.Default.Warning
        AlertSeverity.INFO -> Icons.Default.Info
    }

    val colors = severity.getColors()

    // Обработка начального состояния
    LaunchedEffect(isVisible) {
        if (isVisible) {
            isShown = true
        } else {
            // Задержка для анимации исчезновения
            delay(300)
            isShown = false
        }
    }

    // Обработка закрытия
    val handleDismiss = {
        // Запускаем анимацию исчезновения
        isShown = false
        // Запускаем таймер и только потом вызываем onDismiss
        MainScope().launch {
            delay(300) // Ждем завершения анимации
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = isShown,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)) +
                expandVertically(animationSpec = tween(durationMillis = 300, easing = EaseOutCubic)),
        exit = fadeOut(animationSpec = tween(durationMillis = 300)) +
                shrinkVertically(animationSpec = tween(durationMillis = 300, easing = EaseInCubic))
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .animateEnterExit(
                    enter = slideInVertically(
                        initialOffsetY = { -40 },
                        animationSpec = tween(durationMillis = 300, easing = EaseOutCubic)
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { -40 },
                        animationSpec = tween(durationMillis = 300, easing = EaseInCubic)
                    )
                ),
            shape = RoundedCornerShape(12.dp),
            color = colors.backgroundColor,
            border = BorderStroke(1.dp, colors.borderColor)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Заголовок с действиями
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Заголовок с иконкой
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = alertIcon,
                            contentDescription = null,
                            tint = colors.iconColor,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = colors.contentColor
                        )
                    }

                    // Кнопка закрытия
                    IconButton(
                        onClick = { handleDismiss() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = closeButtonContentDescription,
                            tint = colors.iconColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Содержимое алерта
                if (message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.contentColor.copy(alpha = 0.9f),
                        modifier = Modifier.padding(start = 32.dp, end = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Color scheme for alert types
 */
data class AlertColors(
    val backgroundColor: Color,
    val messageBackgroundColor: Color,
    val contentColor: Color,
    val iconColor: Color,
    val borderColor: Color
)

/**
 * Alert severity levels with improved color schemes
 */
enum class AlertSeverity {
    WARNING,
    ERROR,
    INFO;

    @Composable
    fun getColors(): AlertColors {
        return when (this) {
            WARNING -> AlertColors(
                backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
                messageBackgroundColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                iconColor = MaterialTheme.colorScheme.error,
                borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            )

            ERROR -> AlertColors(
                backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                messageBackgroundColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                iconColor = MaterialTheme.colorScheme.error,
                borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            )

            INFO -> AlertColors(
                backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                messageBackgroundColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                iconColor = MaterialTheme.colorScheme.primary,
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        }
    }
}
