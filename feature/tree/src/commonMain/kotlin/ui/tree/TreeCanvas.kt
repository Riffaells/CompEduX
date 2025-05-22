package ui.tree

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import model.tree.ConnectionTypeDomain
import model.tree.TechnologyTreeDomain
import model.tree.TreeConnectionDomain
import model.tree.TreeNodeDomain
import model.tree.TreeNodeTypeDomain

/**
 * Компонент для отрисовки дерева технологий на канвасе
 */
@Composable
fun TreeCanvas(
    modifier: Modifier = Modifier,
    treeData: TechnologyTreeDomain,
    selectedNodeId: String? = null,
    panOffset: Offset = Offset.Zero,
    pulseScale: Float = 1f,
    glowAlpha: Float = 0.3f,
    showGrid: Boolean = true,
    onNodeClick: (String) -> Unit = {}
) {
    // Создаем TextMeasurer для измерения и отрисовки текста
    val textMeasurer = rememberTextMeasurer()

    // Отладочное логирование
    LaunchedEffect(treeData) {
        println("TreeCanvas: получено дерево с ${treeData.nodes.size} узлами и ${treeData.connections.size} соединениями")
        println("TreeCanvas: панорамирование $panOffset")
        println("TreeCanvas: размеры экрана: width=${treeData.metadata.canvasSize.width}, height=${treeData.metadata.canvasSize.height}")
        treeData.nodes.forEachIndexed { index, node ->
            println("TreeCanvas узел #$index: ${node.id}: позиция (${node.position.x}, ${node.position.y}), тип ${node.type}")
            println("   - Заголовок: ${node.title.content}")
            println("   - Описание: ${node.description.content}")
        }
    }

    // Обработка перетаскивания для навигации по канвасу
    var localPanOffset by remember { mutableStateOf(panOffset) }
    var isDragging by remember { mutableStateOf(false) }

    // Обновляем localPanOffset при изменении внешнего panOffset
    LaunchedEffect(panOffset) {
        localPanOffset = panOffset
        println("TreeCanvas: обновлено панорамирование $localPanOffset")
    }

    // Модификатор для взаимодействия с канвасом
    val interactionModifier = Modifier
        .pointerInput(treeData) {
            // Обработка перетаскивания для навигации
            detectDragGestures(
                onDragStart = { isDragging = true },
                onDragEnd = { isDragging = false },
                onDrag = { change, dragAmount ->
                    change.consume()
                    if (!isDragging) return@detectDragGestures
                    localPanOffset += dragAmount
                }
            )
        }
        .pointerInput(treeData) {
            // Обработка кликов по узлам
            detectTapGestures { tapPosition ->
                println("TreeCanvas: клик по позиции $tapPosition")

                treeData.nodes.forEach { node ->
                    val nodePosition = Offset(
                        node.position.x + localPanOffset.x,
                        node.position.y + localPanOffset.y
                    )

                    // Используем радиус 40 для определения клика (увеличен для лучшей UX)
                    if (isInNodeRadius(tapPosition, nodePosition, 40f)) {
                        println("TreeCanvas: клик по узлу ${node.id} на позиции $nodePosition")
                        onNodeClick(node.id)
                        return@detectTapGestures
                    }
                }
                println("TreeCanvas: клик не попал ни на один узел")
            }
        }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(20, 20, 30)) // Темный фон для лучшей видимости
            .then(interactionModifier)
    ) {
        // Рисуем сетку для отладки, если она включена
        if (showGrid) {
            drawGrid(
                cellSize = 50f,
                color = Color.Gray.copy(alpha = 0.2f),
                offset = localPanOffset
            )
        }

        // Рисуем отладочную информацию
        val paint = androidx.compose.ui.graphics.Paint()
        paint.color = Color.White.copy(alpha = 0.7f)

        val debugInfo = listOf(
            "Панорамирование: (${localPanOffset.x.toInt()}, ${localPanOffset.y.toInt()})",
            "Размер канваса: (${size.width.toInt()} x ${size.height.toInt()})",
            "Количество узлов: ${treeData.nodes.size}",
            "Количество соединений: ${treeData.connections.size}",
            "Выбранный узел: ${selectedNodeId ?: "нет"}"
        )

        // Отрисовка отладочной информации с помощью drawText
        var yPos = 20f
        debugInfo.forEach { text ->
            val textResult = textMeasurer.measure(
                text = text,
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            )
            drawText(
                textLayoutResult = textResult,
                topLeft = Offset(10f, yPos)
            )

            // Для отладки отображаем границы текста
            drawRect(
                color = Color.Red.copy(alpha = 0.3f),
                topLeft = Offset(10f, yPos),
                size = androidx.compose.ui.geometry.Size(
                    width = textResult.size.width.toFloat(),
                    height = textResult.size.height.toFloat()
                )
            )

            yPos += textResult.size.height + 5f
        }

        // Сначала отрисовываем соединения между узлами
        treeData.connections.forEach { connection ->
            val fromNode = treeData.nodes.find { it.id == connection.from }
            val toNode = treeData.nodes.find { it.id == connection.to }

            if (fromNode != null && toNode != null) {
                drawConnection(
                    fromNode = fromNode,
                    toNode = toNode,
                    connection = connection,
                    panOffset = localPanOffset,
                    isSelected = selectedNodeId == fromNode.id || selectedNodeId == toNode.id
                )
            }
        }

        // Затем отрисовываем узлы
        treeData.nodes.forEach { node ->
            val nodeX = node.position.x + localPanOffset.x
            val nodeY = node.position.y + localPanOffset.y
            val isSelected = node.id == selectedNodeId

            drawNode(
                node = node,
                x = nodeX,
                y = nodeY,
                color = getNodeColor(node.type),
                isSelected = isSelected,
                scale = if (isSelected) pulseScale else 1f,
                glowAlpha = glowAlpha
            )
        }
    }
}

/**
 * Проверка, находится ли точка внутри радиуса узла
 */
private fun isInNodeRadius(point: Offset, nodePosition: Offset, radius: Float): Boolean {
    val distance = kotlin.math.sqrt(
        (point.x - nodePosition.x) * (point.x - nodePosition.x) +
        (point.y - nodePosition.y) * (point.y - nodePosition.y)
    )
    return distance <= radius
}

/**
 * Отрисовка соединения между узлами
 */
private fun DrawScope.drawConnection(
    fromNode: TreeNodeDomain,
    toNode: TreeNodeDomain,
    connection: TreeConnectionDomain,
    panOffset: Offset,
    isSelected: Boolean
) {
    val startX = fromNode.position.x + panOffset.x
    val startY = fromNode.position.y + panOffset.y
    val endX = toNode.position.x + panOffset.x
    val endY = toNode.position.y + panOffset.y

    // Определяем стиль линии на основе типа соединения
    val (color, strokeWidth, dashPattern) = when (connection.type) {
        ConnectionTypeDomain.REQUIRED -> Triple(Color(0xFF2196F3), 3.0f, null) // Синий, жирная линия
        ConnectionTypeDomain.RECOMMENDED -> Triple(Color(0xFF4CAF50), 2.5f, floatArrayOf(10f, 5f)) // Зеленый, пунктир
        ConnectionTypeDomain.OPTIONAL -> Triple(Color(0xFFFF9800), 2.0f, floatArrayOf(5f, 5f)) // Оранжевый, мелкий пунктир
    }

    // Рисуем линию с опциональным пунктиром
    if (dashPattern != null) {
        // Рисуем пунктирную линию
        val dx = endX - startX
        val dy = endY - startY
        val length = kotlin.math.sqrt(dx * dx + dy * dy)
        val normalizedDx = dx / length
        val normalizedDy = dy / length

        val dashSize = dashPattern[0]
        val gapSize = dashPattern[1]
        val dashCount = (length / (dashSize + gapSize)).toInt()

        for (i in 0 until dashCount) {
            val startFraction = i * (dashSize + gapSize) / length
            val endFraction = (i * (dashSize + gapSize) + dashSize) / length

            val dashStartX = startX + normalizedDx * startFraction * length
            val dashStartY = startY + normalizedDy * startFraction * length
            val dashEndX = startX + normalizedDx * kotlin.math.min(endFraction * length, length)
            val dashEndY = startY + normalizedDy * kotlin.math.min(endFraction * length, length)

            drawLine(
                color = color.copy(alpha = if (isSelected) 0.9f else 0.8f),
                start = Offset(dashStartX, dashStartY),
                end = Offset(dashEndX, dashEndY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    } else {
        // Рисуем сплошную линию
        drawLine(
            color = color.copy(alpha = if (isSelected) 0.9f else 0.8f),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }

    // Рисуем стрелку на конце соединения
    drawArrow(
        start = Offset(startX, startY),
        end = Offset(endX, endY),
        color = color.copy(alpha = if (isSelected) 0.9f else 0.8f),
        arrowSize = 15f // Увеличенный размер стрелки
    )
}

/**
 * Отрисовка сетки
 */
private fun DrawScope.drawGrid(cellSize: Float, color: Color, offset: Offset) {
    val xLines = (size.width / cellSize).toInt() + 1
    val yLines = (size.height / cellSize).toInt() + 1

    val xOffset = offset.x % cellSize
    val yOffset = offset.y % cellSize

    // Вертикальные линии
    for (i in 0..xLines) {
        val x = i * cellSize + xOffset
        drawLine(
            color = color,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 0.5f
        )
    }

    // Горизонтальные линии
    for (i in 0..yLines) {
        val y = i * cellSize + yOffset
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 0.5f
        )
    }
}

/**
 * Отрисовка стрелки для соединения
 */
private fun DrawScope.drawArrow(
    start: Offset,
    end: Offset,
    color: Color,
    arrowSize: Float = 10f
) {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val angle = kotlin.math.atan2(dy, dx)

    // Используем kotlin.math.PI вместо Math.PI
    val arrowPoint1X = end.x - arrowSize * kotlin.math.cos(angle - kotlin.math.PI / 6)
    val arrowPoint1Y = end.y - arrowSize * kotlin.math.sin(angle - kotlin.math.PI / 6)
    val arrowPoint2X = end.x - arrowSize * kotlin.math.cos(angle + kotlin.math.PI / 6)
    val arrowPoint2Y = end.y - arrowSize * kotlin.math.sin(angle + kotlin.math.PI / 6)

    val arrowPath = Path().apply {
        moveTo(end.x, end.y)
        lineTo(arrowPoint1X.toFloat(), arrowPoint1Y.toFloat())
        lineTo(arrowPoint2X.toFloat(), arrowPoint2Y.toFloat())
        close()
    }

    drawPath(
        path = arrowPath,
        color = color
    )
}

/**
 * Получение цвета для узла на основе его типа
 */
private fun getNodeColor(type: TreeNodeTypeDomain): Color {
    return when (type) {
        TreeNodeTypeDomain.TOPIC -> Color(0xFF2196F3)  // Ярко-синий
        TreeNodeTypeDomain.SKILL -> Color(0xFF4CAF50)  // Ярко-зеленый
        TreeNodeTypeDomain.MODULE -> Color(0xFFF44336) // Ярко-красный
        TreeNodeTypeDomain.ARTICLE -> Color(0xFFFF9800) // Ярко-оранжевый
    }
}

/**
 * Отрисовка узла
 */
private fun DrawScope.drawNode(
    node: TreeNodeDomain,
    x: Float,
    y: Float,
    color: Color,
    isSelected: Boolean,
    scale: Float = 1f,
    glowAlpha: Float = 0.3f
) {
    val nodeRadius = 40f * scale  // Увеличиваем размер узла для лучшей видимости

    // Если узел выбран, рисуем свечение
    if (isSelected) {
        drawCircle(
            color = color.copy(alpha = glowAlpha),
            radius = nodeRadius * 1.5f,
            center = Offset(x, y)
        )
    }

    // Рисуем тень
    drawCircle(
        color = Color.Black.copy(alpha = 0.3f),
        radius = nodeRadius,
        center = Offset(x + 2f, y + 2f)
    )

    // Рисуем основной круг узла
    drawCircle(
        color = color.copy(alpha = if (isSelected) 1f else 0.9f),
        radius = nodeRadius,
        center = Offset(x, y)
    )

    // Рисуем обводку
    drawCircle(
        color = if (isSelected) Color.White else Color(0xFF212121),
        radius = nodeRadius,
        center = Offset(x, y),
        style = Stroke(width = if (isSelected) 3f else 2f)
    )

    // Рисуем маленький круг для эффекта блика
    drawCircle(
        color = Color.White.copy(alpha = 0.5f),
        radius = nodeRadius * 0.4f,
        center = Offset(x - nodeRadius * 0.3f, y - nodeRadius * 0.3f)
    )

    // Рисуем маленькую точку в центре для визуального эффекта
    drawCircle(
        color = color.copy(brightness(0.8f)),
        radius = 5f,
        center = Offset(x, y)
    )
}

// Вспомогательная функция для настройки яркости цвета
private fun Color.copy(brightness: Float): Color {
    val hsl = FloatArray(3)
    android.graphics.Color.colorToHSV(
        android.graphics.Color.argb(
            (alpha * 255).toInt(),
            (red * 255).toInt(),
            (green * 255).toInt(),
            (blue * 255).toInt()
        ),
        hsl
    )
    hsl[2] = brightness // Устанавливаем яркость
    val argb = android.graphics.Color.HSVToColor(hsl)
    return Color(
        red = android.graphics.Color.red(argb) / 255f,
        green = android.graphics.Color.green(argb) / 255f,
        blue = android.graphics.Color.blue(argb) / 255f,
        alpha = alpha
    )
}
