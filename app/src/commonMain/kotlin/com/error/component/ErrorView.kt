package com.error.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.error.ErrorMessagesFactory
import model.AppError

/**
 * Компонент для отображения ошибки
 *
 * @param error Ошибка для отображения
 * @param onRetry Обработчик повторной попытки (опционально)
 * @param showDetails Показывать ли технические детали ошибки
 */
@Composable
fun ErrorView(
    error: AppError,
    onRetry: (() -> Unit)? = null,
    showDetails: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Иконка ошибки
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colors.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Сообщение об ошибке
            Text(
                text = ErrorMessagesFactory.getErrorMessage(error, showDetails),
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.error
            )

            // Кнопка повторной попытки (если предоставлен обработчик)
            if (onRetry != null) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Повторить")
                }
            }
        }
    }
}

/**
 * Упрощенный компонент для отображения ошибки без дополнительного оформления
 */
@Composable
fun SimpleErrorText(
    error: AppError,
    showDetails: Boolean = false,
    modifier: Modifier = Modifier
) {
    Text(
        text = ErrorMessagesFactory.getErrorMessage(error, showDetails),
        color = MaterialTheme.colors.error,
        style = MaterialTheme.typography.caption,
        modifier = modifier
    )
}

/**
 * Компонент для отображения ошибки в виде полосы вверху экрана
 */
@Composable
fun ErrorBanner(
    error: AppError,
    showDetails: Boolean = false,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.error.copy(alpha = 0.8f))
            .padding(8.dp)
    ) {
        Text(
            text = ErrorMessagesFactory.getErrorMessage(error, showDetails),
            color = MaterialTheme.colors.onError,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.align(Alignment.Center)
        )

        // Крестик для закрытия
        Text(
            text = "✕",
            color = MaterialTheme.colors.onError,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .padding(4.dp)
                .clickable { onDismiss() }
        )
    }
}
