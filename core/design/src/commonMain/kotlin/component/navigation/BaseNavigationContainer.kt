package component.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.*
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock

/**
 * Базовый контейнер для навигационных компонентов с эффектом "парения" и размытием фона.
 * Используется как основа для FloatingNavigationBar и FloatingNavigationRail.
 *
 * @param modifier Модификатор для настройки внешнего вида компонента
 * @param backgroundColor Цвет фона навигационной панели
 * @param contentColor Цвет содержимого навигационной панели
 * @param elevation Высота тени для эффекта "парения"
 * @param cornerRadius Радиус скругления углов
 * @param blurType Тип эффекта размытия
 * @param hazeState Состояние эффекта размытия, должно быть общим с источником размытия
 * @param useProgressiveBlur Использовать ли прогрессивное размытие (градиент)
 * @param progressiveBlurCreator Функция для создания прогрессивного размытия
 * @param contentAlignment Выравнивание содержимого внутри контейнера
 * @param content Содержимое навигационной панели
 */
@Composable
fun BaseNavigationContainer(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Float = 8f,
    cornerRadius: Float = 24f,
    blurType: BlurType = BlurType.FROSTED,
    hazeState: HazeState,
    useProgressiveBlur: Boolean = true,
    progressiveBlurCreator: () -> HazeProgressive? = { null },
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit
) {
    // Логируем создание контейнера
    Napier.d("BaseNavigationContainer: Создание контейнера с тенью и скруглением")
    val boxStartTime = Clock.System.now().toEpochMilliseconds()

    // Создаем стиль размытия на основе типа и цвета фона
    val hazeStyle = when (blurType) {
        BlurType.GLASS -> HazeStyle(
            backgroundColor = backgroundColor,
            tint = HazeTint(backgroundColor),
            blurRadius = 20.dp,
            noiseFactor = 0.05f
        )
        BlurType.FROSTED -> HazeStyle(
            backgroundColor = backgroundColor,
            tint = HazeTint(backgroundColor),
            blurRadius = 15.dp,
            noiseFactor = 0.02f
        )
        BlurType.ACRYLIC -> HazeStyle(
            backgroundColor = backgroundColor,
            tint = HazeTint(backgroundColor),
            blurRadius = 30.dp,
            noiseFactor = 0.1f
        )
        BlurType.MICA -> HazeStyle(
            backgroundColor = backgroundColor,
            tint = HazeTint(backgroundColor),
            blurRadius = 10.dp,
            noiseFactor = 0.01f
        )
        BlurType.NONE -> HazeStyle(
            backgroundColor = backgroundColor.copy(alpha = 0.95f),
            tint = null,
            blurRadius = 0.dp,
            noiseFactor = 0.0f
        )
    }

    Napier.d("BaseNavigationContainer: Создан стиль размытия для типа $blurType")

    // Определяем модификатор для контейнера
    val containerModifier = if (blurType == BlurType.NONE) {
        // Если размытие отключено, используем обычный фон
        Napier.d("BaseNavigationContainer: Применяется обычный фон без размытия")
        modifier
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(cornerRadius.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(
                backgroundColor.copy(alpha = 0.95f),
                shape = RoundedCornerShape(cornerRadius.dp)
            )
    } else {
        // Если размытие включено, применяем эффект размытия
        Napier.d("BaseNavigationContainer: Применяется эффект размытия типа $blurType")
        modifier
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(cornerRadius.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(cornerRadius.dp))
            .hazeEffect(
                state = hazeState,
                style = hazeStyle
            )
    }

    // Контейнер с тенью и скруглением
    Box(
        modifier = containerModifier,
        contentAlignment = contentAlignment,
        content = content
    )

    // Логируем завершение создания контейнера
    Napier.d("BaseNavigationContainer: Контейнер создан за ${Clock.System.now().toEpochMilliseconds() - boxStartTime}ms")
}
