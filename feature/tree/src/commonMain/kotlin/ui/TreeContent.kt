package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import component.tree.TreeComponent
import component.tree.store.TreeStore

/**
 * Контент для отображения дерева развития
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeContent(
    modifier: Modifier = Modifier,
    component: TreeComponent
) {
    val state by component.state.collectAsState()
    var zoomLevel by remember { mutableStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset(0f, 0f)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Дерево развития") },
                navigationIcon = {
                    IconButton(onClick = { component.onBackClicked() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        zoomLevel = (zoomLevel * 1.2f).coerceAtMost(3f)
                        component.onEvent(TreeStore.Intent.ZoomChanged(zoomLevel))
                    }) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Увеличить"
                        )
                    }
                    IconButton(onClick = {
                        zoomLevel = (zoomLevel * 0.8f).coerceAtLeast(0.5f)
                        component.onEvent(TreeStore.Intent.ZoomChanged(zoomLevel))
                    }) {
                        Icon(
                            imageVector = Icons.Default.ZoomOut,
                            contentDescription = "Уменьшить"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Область для отображения дерева
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                    .padding(16.dp)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            // Обновляем состояние панорамирования и масштабирования
                            panOffset += pan
                            zoomLevel = (zoomLevel * zoom).coerceIn(0.5f, 3f)
                            component.onEvent(TreeStore.Intent.ZoomChanged(zoomLevel))
                            component.onEvent(TreeStore.Intent.PanChanged(panOffset.x, panOffset.y))
                        }
                    }
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
                                    text = TreeStore.State.TRANSLATIONS[node.titleKey] ?: node.titleKey,
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
                                Text(
                                    text = "Difficulty: ${node.difficulty}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "Time: ${node.estimatedTime} min",
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
                                        // Adjusting tap position for zoom and pan
                                        val adjustedOffset = Offset(
                                            (offset.x - panOffset.x) / zoomLevel,
                                            (offset.y - panOffset.y) / zoomLevel
                                        )

                                        // Проверяем, попал ли клик на какой-либо узел
                                        for (node in treeData.nodes) {
                                            val nodeX = node.position.x.toFloat()
                                            val nodeY = node.position.y.toFloat()
                                            val radius = when (node.style) {
                                                "circular" -> 25f
                                                "hexagon", "square" -> 30f
                                                else -> 25f
                                            }

                                            if (adjustedOffset.x in (nodeX - radius)..(nodeX + radius) &&
                                                adjustedOffset.y in (nodeY - radius)..(nodeY + radius)) {
                                                component.onEvent(TreeStore.Intent.NodeClicked(node.id))
                                                break
                                            }
                                        }
                                    }
                                }
                        ) {
                            // Apply zoom and pan
                            translate(panOffset.x, panOffset.y) {
                                // Сначала рисуем все соединения
                                for (connection in treeData.connections) {
                                    val fromNode = treeData.nodes.find { it.id == connection.from }
                                    val toNode = treeData.nodes.find { it.id == connection.to }

                                    if (fromNode != null && toNode != null) {
                                        val fromX = fromNode.position.x.toFloat() * zoomLevel
                                        val fromY = fromNode.position.y.toFloat() * zoomLevel
                                        val toX = toNode.position.x.toFloat() * zoomLevel
                                        val toY = toNode.position.y.toFloat() * zoomLevel

                                        when (connection.style) {
                                            "solid_arrow" -> {
                                                // Рисуем линию со стрелкой
                                                drawLine(
                                                    color = Color.White.copy(alpha = 0.7f),
                                                    start = Offset(fromX, fromY),
                                                    end = Offset(toX, toY),
                                                    strokeWidth = 2f
                                                )

                                                // Рисуем стрелку на конце
                                                val angle = kotlin.math.atan2(toY - fromY, toX - fromX)
                                                val arrowSize = 10f * zoomLevel
                                                val arrowEndX = toX - arrowSize * 1.5f * kotlin.math.cos(angle).toFloat()
                                                val arrowEndY = toY - arrowSize * 1.5f * kotlin.math.sin(angle).toFloat()

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
                                                val dashLength = 5f * zoomLevel
                                                val gapLength = 5f * zoomLevel
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
                                                        start = Offset(startX, startY),
                                                        end = Offset(endX, endY),
                                                        strokeWidth = 2f
                                                    )
                                                }
                                            }
                                            else -> {
                                                drawLine(
                                                    color = Color.White.copy(alpha = 0.3f),
                                                    start = Offset(fromX, fromY),
                                                    end = Offset(toX, toY),
                                                    strokeWidth = 1f
                                                )
                                            }
                                        }
                                    }
                                }

                                // Затем рисуем все узлы
                                for (node in treeData.nodes) {
                                    val x = node.position.x.toFloat() * zoomLevel
                                    val y = node.position.y.toFloat() * zoomLevel
                                    val isSelected = node.id == state.selectedNodeId

                                    // Выбираем цвет узла в зависимости от состояния
                                    val nodeColor = when (node.state) {
                                        "COMPLETED" -> Color(0xFF4CAF50) // Зеленый для выполненных
                                        "IN_PROGRESS" -> Color(0xFFFFC107) // Желтый для текущих
                                        "LOCKED" -> Color(0xFF9E9E9E) // Серый для заблокированных
                                        "AVAILABLE" -> Color(0xFF2196F3) // Синий для доступных
                                        else -> if (isSelected) Color(0xFFFFAB40) else Color(0xFF2196F3) // Оранжевый для выбранных, синий по умолчанию
                                    }

                                    // Рисуем форму узла в зависимости от стиля
                                    when (node.style) {
                                        "circular" -> {
                                            // Круглый узел
                                            drawCircle(
                                                color = nodeColor,
                                                radius = 25f * zoomLevel,
                                                center = Offset(x, y)
                                            )
                                            // Обводка
                                            drawCircle(
                                                color = if (isSelected) Color.White else Color.Black,
                                                radius = 25f * zoomLevel,
                                                center = Offset(x, y),
                                                style = Stroke(width = 2f * zoomLevel)
                                            )
                                        }
                                        "hexagon" -> {
                                            // Шестиугольник
                                            val path = Path()
                                            val radius = 30f * zoomLevel
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
                                                style = Stroke(width = 2f * zoomLevel)
                                            )
                                        }
                                        "square" -> {
                                            // Квадрат
                                            val size = 50f * zoomLevel
                                            drawRect(
                                                color = nodeColor,
                                                topLeft = Offset(x - size/2, y - size/2),
                                                size = Size(size, size)
                                            )
                                            drawRect(
                                                color = if (isSelected) Color.White else Color.Black,
                                                topLeft = Offset(x - size/2, y - size/2),
                                                size = Size(size, size),
                                                style = Stroke(width = 2f * zoomLevel)
                                            )
                                        }
                                        else -> {
                                            // По умолчанию круг
                                            drawCircle(
                                                color = nodeColor,
                                                radius = 25f * zoomLevel,
                                                center = Offset(x, y)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Overlay Text composables for node labels
                        for (node in treeData.nodes) {
                            val x = node.position.x.toFloat() * zoomLevel + panOffset.x
                            val y = node.position.y.toFloat() * zoomLevel + panOffset.y
                            val nodeTitle = TreeStore.State.TRANSLATIONS[node.titleKey] ?: node.titleKey

                            Text(
                                text = nodeTitle,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .width((80 * zoomLevel).dp)
                                    .absoluteOffset(x = (x - 40 * zoomLevel).dp, y = (y + 30 * zoomLevel).dp)
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

            // JSON input section
            Text(
                text = "JSON редактор дерева",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            OutlinedTextField(
                value = state.jsonInput,
                onValueChange = { component.onEvent(TreeStore.Intent.UpdateJsonInput(it)) },
                label = { Text("JSON Дерева") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                textStyle = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { component.onEvent(TreeStore.Intent.ParseJson(state.jsonInput)) },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Применить JSON")
                }
            }
        }
    }
}
