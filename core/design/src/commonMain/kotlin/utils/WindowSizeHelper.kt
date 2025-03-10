package utils

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Интерфейс для получения информации о размере окна
 */
interface WindowSizeHelper {
    /**
     * Получить ширину экрана
     */
    fun getScreenWidth(): Dp

    /**
     * Проверить, является ли экран большим
     */
    fun isLargeScreen(): Boolean = getScreenWidth() > 840.dp
}

/**
 * CompositionLocal для доступа к WindowSizeHelper
 */
val LocalWindowSizeHelper = compositionLocalOf<WindowSizeHelper> {
    // Реализация по умолчанию
    object : WindowSizeHelper {
        override fun getScreenWidth(): Dp = 1024.dp
    }
}
