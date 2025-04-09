package component.settings.alert

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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
 * Современный компонент предупреждения с улучшенным дизайном и плавной анимацией
 *
 * @param title Заголовок предупреждения
 * @param message Текст предупреждения
 * @param icon Иконка предупреждения
 * @param isVisible Флаг видимости предупреждения
 * @param onDismiss Функция-обработчик закрытия предупреждения
 * @param severity Уровень серьезности предупреждения
 * @param modifier Модификатор для стилизации компонента
 */
@Composable
fun WarningAlert(
    title: String,
    message: String,
    icon: ImageVector? = null,
    isVisible: Boolean = true,
    onDismiss: () -> Unit = {},
    severity: AlertSeverity = AlertSeverity.INFO,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    var isShown by remember { mutableStateOf(isVisible) }

    val alertIcon = icon ?: when(severity) {
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
        ElevatedCard(
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
            colors = CardDefaults.elevatedCardColors(
                containerColor = colors.backgroundColor,
                contentColor = colors.contentColor
            ),
//            border = BorderStroke(1.dp, colors.borderColor),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Заголовок с действиями
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
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
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.contentColor
                        )
                    }

                    // Кнопки действий
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.isNotEmpty()) {
                            IconButton(
                                onClick = { isExpanded = !isExpanded },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isExpanded)
                                        Icons.Outlined.VisibilityOff
                                    else
                                        Icons.Outlined.Visibility,
                                    contentDescription = if (isExpanded) "Скрыть содержимое" else "Показать содержимое",
                                    tint = colors.iconColor.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { handleDismiss() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Закрыть",
                                tint = colors.iconColor.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Содержимое алерта (если есть сообщение и оно развернуто)
                if (message.isNotEmpty()) {
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically(
                            animationSpec = tween(durationMillis = 300, easing = EaseOutCubic)
                        ) + fadeIn(
                            animationSpec = tween(durationMillis = 200)
                        ),
                        exit = shrinkVertically(
                            animationSpec = tween(durationMillis = 300, easing = EaseInCubic)
                        ) + fadeOut(
                            animationSpec = tween(durationMillis = 200)
                        )
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.messageBackgroundColor,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.contentColor,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Цветовая схема для предупреждения
 */
data class AlertColors(
    val backgroundColor: Color,
    val messageBackgroundColor: Color,
    val contentColor: Color,
    val iconColor: Color,
    val borderColor: Color
)

/**
 * Уровни серьезности предупреждения с улучшенными цветами
 */
enum class AlertSeverity {
    WARNING,
    ERROR,
    INFO;

    @Composable
    fun getColors(): AlertColors {
        return when (this) {
            WARNING -> AlertColors(
                backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                messageBackgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                iconColor = MaterialTheme.colorScheme.error,
                borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            )
            ERROR -> AlertColors(
                backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                messageBackgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                iconColor = MaterialTheme.colorScheme.error,
                borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            )
            INFO -> AlertColors(
                backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                messageBackgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                iconColor = MaterialTheme.colorScheme.primary,
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        }
    }
}
