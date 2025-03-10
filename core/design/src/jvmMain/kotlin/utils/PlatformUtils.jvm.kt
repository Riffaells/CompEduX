package utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import java.awt.Dimension
import java.awt.Toolkit

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenWidth(): Dp {
    // Получаем информацию о текущем окне
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current

    // Создаем состояние для хранения ширины окна
    var windowWidth by remember { mutableStateOf(windowInfo.containerSize.width) }

    // Обновляем ширину окна при каждой перекомпозиции
    // Это позволит реагировать на изменение размера окна
    windowWidth = windowInfo.containerSize.width

    // Конвертируем пиксели в Dp
    return with(density) { windowWidth.toDp() }
}
