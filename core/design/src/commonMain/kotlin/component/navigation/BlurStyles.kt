package component.navigation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint

/**
 * Класс, содержащий различные стили размытия для использования с библиотекой Haze.
 */
object BlurStyles {
    /**
     * Создает стиль размытия "стекло" - прозрачный с минимальным шумом.
     * Подходит для создания эффекта стеклянной поверхности.
     *
     * @param backgroundColor Базовый цвет фона
     * @return HazeStyle с настройками для эффекта стекла
     */
    fun glass(backgroundColor: Color): HazeStyle {
        return HazeStyle(
            backgroundColor = backgroundColor,
            tint = HazeTint(backgroundColor.copy(alpha = 0.4f)),
            blurRadius = 10.dp,
            noiseFactor = 0.0f
        )
    }

    /**
     * Создает стиль размытия "матовое стекло" - с легким шумом и более сильным размытием.
     * Подходит для создания эффекта матового стекла.
     *
     * @param backgroundColor Базовый цвет фона
     * @return HazeStyle с настройками для эффекта матового стекла
     */
    fun frostedGlass(backgroundColor: Color): HazeStyle {
        return HazeStyle(
            backgroundColor = backgroundColor,
            tint = HazeTint(backgroundColor.copy(alpha = 0.5f)),
            blurRadius = 20.dp,
            noiseFactor = 0.05f
        )
    }

    /**
     * Создает стиль размытия "акрил" - с более сильным размытием и шумом.
     * Имитирует эффект акрилового материала из Windows.
     *
     * @param backgroundColor Базовый цвет фона
     * @return HazeStyle с настройками для эффекта акрила
     */
    fun acrylic(backgroundColor: Color): HazeStyle {
        return HazeStyle(
            backgroundColor = backgroundColor,
            tint = HazeTint(backgroundColor.copy(alpha = 0.6f)),
            blurRadius = 30.dp,
            noiseFactor = 0.1f
        )
    }

    /**
     * Создает стиль размытия "слюда" - с максимальным размытием и шумом.
     * Имитирует эффект материала Mica из Windows 11.
     *
     * @param backgroundColor Базовый цвет фона
     * @return HazeStyle с настройками для эффекта слюды
     */
    fun mica(backgroundColor: Color): HazeStyle {
        return HazeStyle(
            backgroundColor = backgroundColor,
            tint = HazeTint(backgroundColor.copy(alpha = 0.7f)),
            blurRadius = 40.dp,
            noiseFactor = 0.15f
        )
    }
}
