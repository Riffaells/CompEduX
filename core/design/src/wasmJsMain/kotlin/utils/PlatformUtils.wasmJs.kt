package utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.browser.window
import org.w3c.dom.Window
import org.w3c.dom.events.Event

// В Kotlin/Wasm мы не можем использовать js() внутри функций
// Поэтому используем фиксированное значение для ширины экрана
@Composable
actual fun getScreenWidth(): Dp {
    // Создаем состояние для хранения ширины окна
    var windowWidth by remember { mutableStateOf(window.innerWidth) }

    // Добавляем обработчик изменения размера окна
    DisposableEffect(Unit) {
        val resizeListener: (Event) -> Unit = {
            windowWidth = window.innerWidth
        }

        // Добавляем слушатель события resize
        window.addEventListener("resize", resizeListener)

        // Удаляем слушатель при уничтожении компонента
        onDispose {
            window.removeEventListener("resize", resizeListener)
        }
    }

    // Конвертируем пиксели в Dp (примерно)
    // В WASM мы не имеем доступа к плотности экрана, поэтому используем приблизительное соотношение
    return (windowWidth / 1.5).dp
}
