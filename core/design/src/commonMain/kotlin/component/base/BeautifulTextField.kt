package component.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Красивый компонент OutlinedTextField с единым стилем для всего приложения
 *
 * @param value Текущее значение текстового поля
 * @param onValueChange Обработчик изменения значения
 * @param label Текст метки (заголовок поля)
 * @param icon Иконка, отображаемая слева от поля ввода
 * @param modifier Модификатор для поля ввода
 * @param supportingText Вспомогательный текст под полем ввода (опционально)
 * @param placeholder Текст-подсказка внутри поля (опционально)
 * @param isError Флаг ошибки
 * @param singleLine Флаг для однострочного ввода
 * @param maxLength Максимальная длина текста (опционально)
 * @param trailingIcon Содержимое для отображения справа от поля ввода (опционально)
 * @param visualTransformation Трансформация для отображения текста (например, для пароля)
 * @param enabled Включено ли поле ввода
 */
@Composable
fun BeautifulTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    supportingText: (@Composable () -> Unit)? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    maxLength: Int = Int.MAX_VALUE,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.length <= maxLength) {
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        trailingIcon = trailingIcon,
        placeholder = placeholder?.let { { Text(it) } },
        isError = isError,
        visualTransformation = visualTransformation,
        supportingText = supportingText,
        singleLine = singleLine,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        )
    )
}
