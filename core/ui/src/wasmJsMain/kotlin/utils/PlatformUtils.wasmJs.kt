package utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// В Kotlin/Wasm мы не можем использовать js() внутри функций
// Поэтому используем фиксированное значение для ширины экрана
@Composable
actual fun getScreenWidth(): Dp {
    // Возвращаем фиксированное значение для ширины экрана
    // В реальном приложении можно было бы использовать более сложную логику
    return 1024.dp
}
