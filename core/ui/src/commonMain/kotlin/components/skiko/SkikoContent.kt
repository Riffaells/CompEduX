package components.skiko

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import component.app.skiko.SkikoComponent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import component.app.skiko.store.SkikoStore
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex

@Composable
fun SkikoContent(
    modifier: Modifier = Modifier,
    component: SkikoComponent
) {
    val state by component.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Визуализация дерева курса",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Область для отображения дерева
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            state.parsedTree?.let { treeData ->
                // Информация о выбранном узле
                state.selectedNodeId?.let { selectedNodeId ->
                    val selectedNode = treeData.nodes.find { it.id == selectedNodeId }
                    selectedNode?.let { node ->
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                                .width(180.dp)
                        ) {
                            Text(
                                text = SkikoStore.State.TRANSLATIONS[node.titleKey] ?: node.titleKey,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ID: ${node.id}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Content: ${node.contentId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Style: ${node.style}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Create a BoxWithConstraints to hold both Canvas and Text elements
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Canvas for drawing connections and nodes
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(treeData) {
                                detectTapGestures { offset ->
                                    // Проверяем, попал ли клик на какой-либо узел
                                    for (node in treeData.nodes) {
                                        val nodeX = node.position.x.toFloat()
                                        val nodeY = node.position.y.toFloat()
                                        val radius = when (node.style) {
                                            "circular" -> 25f
                                            "hexagon", "square" -> 30f
                                            else -> 25f
                                        }

                                        if (offset.x in (nodeX - radius)..(nodeX + radius) &&
                                            offset.y in (nodeY - radius)..(nodeY + radius)) {
                                            component.onEvent(SkikoStore.Intent.NodeClicked(node.id))
                                            break
                                        }
                                    }
                                }
                            }
                    ) {
                        // Сначала рисуем соединения
                        for (connection in treeData.connections) {
                            val fromNode = treeData.nodes.find { it.id == connection.from }
                            val toNode = treeData.nodes.find { it.id == connection.to }

                            if (fromNode != null && toNode != null) {
                                val fromX = fromNode.position.x.toFloat()
                                val fromY = fromNode.position.y.toFloat()
                                val toX = toNode.position.x.toFloat()
                                val toY = toNode.position.y.toFloat()

                                // Рисуем линию
                                when (connection.style) {
                                    "solid_arrow" -> {
                                        drawLine(
                                            color = Color.White.copy(alpha = 0.7f),
                                            start = androidx.compose.ui.geometry.Offset(fromX, fromY),
                                            end = androidx.compose.ui.geometry.Offset(toX, toY),
                                            strokeWidth = 2f
                                        )

                                        // Рисуем стрелку
                                        val angle = kotlin.math.atan2(toY - fromY, toX - fromX)
                                        val arrowSize = 10f

                                        // Точка конца стрелки (чуть до целевого узла)
                                        val arrowEndX = toX - 25 * kotlin.math.cos(angle)
                                        val arrowEndY = toY - 25 * kotlin.math.sin(angle)

                                        val path = Path()
                                        path.moveTo(
                                            arrowEndX + arrowSize * kotlin.math.cos(angle - Math.PI / 6).toFloat(),
                                            arrowEndY + arrowSize * kotlin.math.sin(angle - Math.PI / 6).toFloat()
                                        )
                                        path.lineTo(arrowEndX, arrowEndY)
                                        path.lineTo(
                                            arrowEndX + arrowSize * kotlin.math.cos(angle + Math.PI / 6).toFloat(),
                                            arrowEndY + arrowSize * kotlin.math.sin(angle + Math.PI / 6).toFloat()
                                        )

                                        drawPath(
                                            path = path,
                                            color = Color.White.copy(alpha = 0.7f),
                                            style = Stroke(width = 2f, cap = StrokeCap.Round)
                                        )
                                    }
                                    "dashed_line" -> {
                                        val dashLength = 5f
                                        val gapLength = 5f
                                        val dx = toX - fromX
                                        val dy = toY - fromY
                                        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                                        val dashCount = (distance / (dashLength + gapLength)).toInt()

                                        for (i in 0 until dashCount) {
                                            val startFraction = i * (dashLength + gapLength) / distance
                                            val endFraction = (i * (dashLength + gapLength) + dashLength) / distance

                                            val startX = fromX + dx * startFraction
                                            val startY = fromY + dy * startFraction
                                            val endX = fromX + dx * kotlin.math.min(endFraction, 1.0f)
                                            val endY = fromY + dy * kotlin.math.min(endFraction, 1.0f)

                                            drawLine(
                                                color = Color.White.copy(alpha = 0.5f),
                                                start = androidx.compose.ui.geometry.Offset(startX, startY),
                                                end = androidx.compose.ui.geometry.Offset(endX, endY),
                                                strokeWidth = 2f
                                            )
                                        }
                                    }
                                    else -> {
                                        drawLine(
                                            color = Color.White.copy(alpha = 0.3f),
                                            start = androidx.compose.ui.geometry.Offset(fromX, fromY),
                                            end = androidx.compose.ui.geometry.Offset(toX, toY),
                                            strokeWidth = 1f
                                        )
                                    }
                                }
                            }
                        }

                        // Затем рисуем все узлы
                        for (node in treeData.nodes) {
                            val x = node.position.x.toFloat()
                            val y = node.position.y.toFloat()
                            val isSelected = node.id == state.selectedNodeId

                            // Выбираем цвет узла в зависимости от состояния
                            val nodeColor = when {
                                isSelected -> Color(0xFFFFAB40) // Оранжевый для выбранного
                                node.requirements.isEmpty() -> Color(0xFF4CAF50) // Зеленый для начальных узлов
                                else -> Color(0xFF2196F3) // Синий для остальных
                            }

                            // Рисуем форму узла в зависимости от стиля
                            when (node.style) {
                                "circular" -> {
                                    // Круглый узел
                                    drawCircle(
                                        color = nodeColor,
                                        radius = 25f,
                                        center = androidx.compose.ui.geometry.Offset(x, y)
                                    )
                                    // Обводка
                                    drawCircle(
                                        color = if (isSelected) Color.White else Color.Black,
                                        radius = 25f,
                                        center = androidx.compose.ui.geometry.Offset(x, y),
                                        style = Stroke(width = 2f)
                                    )
                                }
                                "hexagon" -> {
                                    // Шестиугольник
                                    val path = Path()
                                    val radius = 30f
                                    val sides = 6
                                    for (i in 0 until sides) {
                                        val angle = 2.0 * Math.PI * i / sides - Math.PI / 2
                                        val nextX = x + radius * kotlin.math.cos(angle).toFloat()
                                        val nextY = y + radius * kotlin.math.sin(angle).toFloat()
                                        if (i == 0) path.moveTo(nextX, nextY) else path.lineTo(nextX, nextY)
                                    }
                                    path.close()
                                    drawPath(path, color = nodeColor)
                                    drawPath(
                                        path = path,
                                        color = if (isSelected) Color.White else Color.Black,
                                        style = Stroke(width = 2f)
                                    )
                                }
                                "square" -> {
                                    // Квадрат
                                    val size = 50f
                                    drawRect(
                                        color = nodeColor,
                                        topLeft = androidx.compose.ui.geometry.Offset(x - size/2, y - size/2),
                                        size = androidx.compose.ui.geometry.Size(size, size)
                                    )
                                    drawRect(
                                        color = if (isSelected) Color.White else Color.Black,
                                        topLeft = androidx.compose.ui.geometry.Offset(x - size/2, y - size/2),
                                        size = androidx.compose.ui.geometry.Size(size, size),
                                        style = Stroke(width = 2f)
                                    )
                                }
                                else -> {
                                    // По умолчанию круг
                                    drawCircle(
                                        color = nodeColor,
                                        radius = 25f,
                                        center = androidx.compose.ui.geometry.Offset(x, y)
                                    )
                                }
                            }
                        }
                    }

                    // Overlay Text composables for node labels instead of drawing on canvas
                    for (node in treeData.nodes) {
                        val x = node.position.x.toFloat()
                        val y = node.position.y.toFloat()
                        val nodeTitle = SkikoStore.State.TRANSLATIONS[node.titleKey] ?: node.titleKey

                        Text(
                            text = nodeTitle,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .width(80.dp)
                                .absoluteOffset(x = (x - 40).dp, y = (y + 30).dp)
                                .align(Alignment.TopStart)
                        )
                    }
                }
            } ?: run {
                // Если дерево не загружено
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text(
                            text = "Дерево не загружено",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Отображение ошибки, если есть
        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Текстовое поле для ввода JSON
        OutlinedTextField(
            value = state.jsonInput,
            onValueChange = { component.onEvent(SkikoStore.Intent.UpdateJsonInput(it)) },
            label = { Text("JSON Дерева") },
            modifier = Modifier.fillMaxWidth().height(200.dp),
            textStyle = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Кнопки действий
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { component.onEvent(SkikoStore.Intent.ParseJson(state.jsonInput)) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Применить JSON")
            }

            Button(
                onClick = { component.onBackClicked() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Назад")
            }
        }
    }
}
