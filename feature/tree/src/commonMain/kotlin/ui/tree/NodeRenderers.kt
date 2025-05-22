package ui.tree

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import model.tree.TreeNodeDomain
import model.tree.TreeNodeTypeDomain
import kotlin.math.*

/**
 * Файл содержит функции для отрисовки различных элементов дерева технологий:
 * - узлы разных форм (круг, шестиугольник, квадрат)
 * - соединения между узлами
 */

// Функция для получения градиентных цветов для узла в зависимости от его типа и состояния
fun getNodeColorGradient(node: TreeNodeDomain, selectedNodeId: String?): List<Color> {
    val isSelected = node.id == selectedNodeId

    return when {
        isSelected -> listOf(
            Color(0xFFFF9800),
            Color(0xFFE65100)
        )

        node.requirements.isEmpty() -> listOf(
            Color(0xFF4CAF50),
            Color(0xFF2E7D32)
        )

        else -> when (node.type) {
            TreeNodeTypeDomain.TOPIC -> listOf(
                Color(0xFF2196F3),
                Color(0xFF0D47A1)
            )

            TreeNodeTypeDomain.SKILL -> listOf(
                Color(0xFFFFC107),
                Color(0xFFFF6F00)
            )

            TreeNodeTypeDomain.MODULE -> listOf(
                Color(0xFFE91E63),
                Color(0xFFC2185B)
            )

            TreeNodeTypeDomain.ARTICLE -> listOf(
                Color(0xFF9C27B0),
                Color(0xFF6A1B9A)
            )
        }
    }
}

// Функция для создания шестиугольника
fun createHexagonPath(centerX: Float, centerY: Float, radius: Float): Path {
    val path = Path()
    val sides = 6

    for (i in 0 until sides) {
        val angle = 2.0 * PI * i / sides - PI / 2
        val nextX = centerX + radius * cos(angle).toFloat()
        val nextY = centerY + radius * sin(angle).toFloat()
        if (i == 0) path.moveTo(nextX, nextY) else path.lineTo(nextX, nextY)
    }
    path.close()
    return path
}

// Функция для создания верхней части шестиугольника (для эффекта блика)
fun createTopHexagonPath(centerX: Float, centerY: Float, radius: Float): Path {
    val path = Path()
    val sides = 6

    // Создаем только верхнюю часть шестиугольника для эффекта блика
    for (i in 0 until 3) {
        val angle = 2.0 * PI * i / sides - PI / 2
        val nextX = centerX + radius * cos(angle).toFloat()
        val nextY = centerY + radius * sin(angle).toFloat()
        if (i == 0) path.moveTo(nextX, nextY) else path.lineTo(nextX, nextY)
    }
    path.lineTo(centerX, centerY)
    path.close()
    return path
}

// Функция для отрисовки узла круглой формы
fun DrawScope.drawCircularNode(
    node: TreeNodeDomain,
    x: Float,
    y: Float,
    isSelected: Boolean,
    nodeGradient: List<Color>,
    currentScale: Float = 1f
) {
    // Тень под узлом
    drawCircle(
        color = Color.Black.copy(alpha = 0.5f),
        radius = 34f * currentScale,
        center = Offset(x + 2f, y + 2f)
    )

    // Внешнее кольцо с эффектом свечения
    if (isSelected) {
        drawCircle(
            color = nodeGradient.first().copy(alpha = 0.7f),
            radius = 36f * currentScale,
            center = Offset(x, y)
        )
    }

    // Круглый узел
    drawCircle(
        brush = Brush.radialGradient(
            colors = nodeGradient,
            center = Offset(x, y),
            radius = 32f * currentScale
        ),
        radius = 32f * currentScale,
        center = Offset(x, y)
    )

    // Блик на узле
    drawCircle(
        color = Color.White.copy(alpha = 0.5f),
        radius = 12f * currentScale,
        center = Offset(x - 10f, y - 10f)
    )

    // Обводка
    drawCircle(
        color = if (isSelected) Color.White else Color(0xFF212121),
        radius = 32f * currentScale,
        center = Offset(x, y),
        style = Stroke(width = 2.5f)
    )
}

// Функция для отрисовки узла в форме шестиугольника
fun DrawScope.drawHexagonNode(
    node: TreeNodeDomain,
    x: Float,
    y: Float,
    isSelected: Boolean,
    nodeGradient: List<Color>,
    currentScale: Float = 1f
) {
    val nodePath = createHexagonPath(x, y, 35f * currentScale)
    val shadowPath = createHexagonPath(x + 2f, y + 2f, 35f * currentScale)

    // Тень
    drawPath(shadowPath, color = Color.Black.copy(alpha = 0.5f))

    // Внешний контур с эффектом свечения
    if (isSelected) {
        val glowPath = createHexagonPath(x, y, 40f * currentScale)
        drawPath(glowPath, color = nodeGradient.first().copy(alpha = 0.7f))
    }

    // Основная форма с градиентом
    drawPath(
        path = nodePath,
        brush = Brush.radialGradient(
            colors = nodeGradient,
            center = Offset(x, y),
            radius = 35f * currentScale
        )
    )

    // Добавление эффекта блика
    drawPath(
        path = createTopHexagonPath(x, y, 35f * currentScale),
        color = Color.White.copy(alpha = 0.5f)
    )

    // Обводка
    drawPath(
        path = nodePath,
        color = if (isSelected) Color.White else Color(0xFF212121),
        style = Stroke(width = 2.5f)
    )
}

// Функция для отрисовки узла в форме квадрата
fun DrawScope.drawSquareNode(
    node: TreeNodeDomain,
    x: Float,
    y: Float,
    isSelected: Boolean,
    nodeGradient: List<Color>,
    currentScale: Float = 1f
) {
    val size = 60f * currentScale

    // Тень
    drawRect(
        color = Color.Black.copy(alpha = 0.5f),
        topLeft = Offset(x - size / 2 + 2f, y - size / 2 + 2f),
        size = Size(size, size)
    )

    // Внешний контур с эффектом свечения
    if (isSelected) {
        drawRect(
            color = nodeGradient.first().copy(alpha = 0.7f),
            topLeft = Offset(x - size / 2 - 5f, y - size / 2 - 5f),
            size = Size(size + 10f, size + 10f)
        )
    }

    // Основная форма с градиентом
    drawRect(
        brush = Brush.radialGradient(
            colors = nodeGradient,
            center = Offset(x, y),
            radius = size
        ),
        topLeft = Offset(x - size / 2, y - size / 2),
        size = Size(size, size)
    )

    // Добавление эффекта блика
    drawRect(
        color = Color.White.copy(alpha = 0.5f),
        topLeft = Offset(x - size / 2, y - size / 2),
        size = Size(size, size / 2)
    )

    // Обводка
    drawRect(
        color = if (isSelected) Color.White else Color(0xFF212121),
        topLeft = Offset(x - size / 2, y - size / 2),
        size = Size(size, size),
        style = Stroke(width = 2.5f)
    )
}

// Функция для отрисовки соединения со стрелкой
fun DrawScope.drawArrowConnection(
    fromX: Float,
    fromY: Float,
    toX: Float,
    toY: Float,
    isHighlighted: Boolean,
    gradient: Brush
) {
    // Эффект свечения вокруг линии для выделенных соединений
    if (isHighlighted) {
        drawLine(
            color = Color(0xFF2979FF).copy(alpha = 0.6f),
            start = Offset(fromX, fromY),
            end = Offset(toX, toY),
            strokeWidth = 8f,
            cap = StrokeCap.Round
        )
    }

    // Основная линия
    drawLine(
        brush = gradient,
        start = Offset(fromX, fromY),
        end = Offset(toX, toY),
        strokeWidth = 3f,
        cap = StrokeCap.Round
    )

    // Рисуем стрелку
    val angle = atan2(toY - fromY, toX - fromX)
    val arrowSize = 12f

    // Точка конца стрелки (чуть до целевого узла)
    val arrowEndX = toX - 35 * cos(angle)
    val arrowEndY = toY - 35 * sin(angle)

    val path = Path()
    path.moveTo(
        arrowEndX + arrowSize * cos(angle - PI / 6).toFloat(),
        arrowEndY + arrowSize * sin(angle - PI / 6).toFloat()
    )
    path.lineTo(arrowEndX, arrowEndY)
    path.lineTo(
        arrowEndX + arrowSize * cos(angle + PI / 6).toFloat(),
        arrowEndY + arrowSize * sin(angle + PI / 6).toFloat()
    )

    drawPath(
        path = path,
        brush = gradient,
        style = Stroke(width = 3f, cap = StrokeCap.Round)
    )
}

// Функция для отрисовки прерывистого соединения
fun DrawScope.drawDashedConnection(
    fromX: Float,
    fromY: Float,
    toX: Float,
    toY: Float,
    isHighlighted: Boolean,
    gradient: Brush
) {
    val dashLength = 10f
    val gapLength = 5f
    val dx = toX - fromX
    val dy = toY - fromY
    val distance = sqrt(dx * dx + dy * dy)
    val dashCount = (distance / (dashLength + gapLength)).toInt()

    // Эффект свечения вокруг линии для выделенных соединений
    if (isHighlighted) {
        drawLine(
            color = Color(0xFF2979FF).copy(alpha = 0.5f),
            start = Offset(fromX, fromY),
            end = Offset(toX, toY),
            strokeWidth = 8f,
            cap = StrokeCap.Round
        )
    }

    for (i in 0 until dashCount) {
        val startFraction = i * (dashLength + gapLength) / distance
        val endFraction = (i * (dashLength + gapLength) + dashLength) / distance

        val startX = fromX + dx * startFraction
        val startY = fromY + dy * startFraction
        val endX = fromX + dx * min(endFraction, 1.0f)
        val endY = fromY + dy * min(endFraction, 1.0f)

        drawLine(
            brush = gradient,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    }
}

// Функция для отрисовки обычного соединения
fun DrawScope.drawSimpleConnection(
    fromX: Float,
    fromY: Float,
    toX: Float,
    toY: Float,
    isHighlighted: Boolean,
    gradient: Brush
) {
    // Эффект свечения вокруг линии для выделенных соединений
    if (isHighlighted) {
        drawLine(
            color = Color(0xFF2979FF).copy(alpha = 0.4f),
            start = Offset(fromX, fromY),
            end = Offset(toX, toY),
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )
    }

    drawLine(
        brush = gradient,
        start = Offset(fromX, fromY),
        end = Offset(toX, toY),
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )
}
