package utils

import androidx.compose.runtime.Composable
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
    @Composable
    fun getScreenWidth(): Dp

    /**
     * Проверить, является ли экран большим
     */
    @Composable
    fun isLargeScreen(): Boolean = getScreenWidth() > 840.dp
}

/**
 * CompositionLocal для доступа к WindowSizeHelper
 */
val LocalWindowSizeHelper = compositionLocalOf<WindowSizeHelper> {
    // Реализация по умолчанию
    object : WindowSizeHelper {
        @Composable
        override fun getScreenWidth(): Dp = 1024.dp
    }
}
