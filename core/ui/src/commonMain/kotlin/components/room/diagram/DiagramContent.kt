package components.room.diagram

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import component.app.room.diagram.DiagramComponent
import component.app.room.diagram.store.DiagramStore

@Composable
fun DiagramContent(component: DiagramComponent) {
    val state by component.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Selector for diagram type
        DiagramTypeSelector(
            currentType = state.diagramType,
            onTypeSelected = { component.onAction(DiagramStore.Intent.UpdateDiagramType(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display the selected diagram type
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            when (state.diagramType) {
                DiagramStore.DiagramType.BAR_CHART -> BarChart(data = state.diagramData)
                DiagramStore.DiagramType.PIE_CHART -> PieChart(data = state.diagramData)
                DiagramStore.DiagramType.LINE_CHART -> LineChart(data = state.diagramData)
                DiagramStore.DiagramType.SCATTER_PLOT -> ScatterPlot(data = state.diagramData)
            }
        }
    }
}

@Composable
fun DiagramTypeSelector(
    currentType: DiagramStore.DiagramType,
    onTypeSelected: (DiagramStore.DiagramType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DiagramStore.DiagramType.values().forEach { type ->
            FilterChip(
                selected = type == currentType,
                onClick = { onTypeSelected(type) },
                label = {
                    Text(
                        text = when (type) {
                            DiagramStore.DiagramType.BAR_CHART -> "Столбцы"
                            DiagramStore.DiagramType.PIE_CHART -> "Круговая"
                            DiagramStore.DiagramType.LINE_CHART -> "Линейная"
                            DiagramStore.DiagramType.SCATTER_PLOT -> "Точечная"
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun BarChart(data: List<DiagramStore.DataPoint>) {
    if (data.isEmpty()) {
        EmptyDataPlaceholder()
        return
    }

    // Animation for bars
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    val maxValue = data.maxOfOrNull { it.value } ?: 1.0

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val barWidth = size.width / data.size * 0.8f
        val spacing = size.width / data.size * 0.2f
        val bottomPadding = 40f

        // Draw horizontal axis
        drawLine(
            color = Color.Gray,
            start = Offset(0f, size.height - bottomPadding),
            end = Offset(size.width, size.height - bottomPadding),
            strokeWidth = 2f
        )

        // Draw bars
        data.forEachIndexed { index, dataPoint ->
            val barHeight =
                (dataPoint.value / maxValue * (size.height - bottomPadding - 20f) * animatedProgress.value).toFloat()
            val startX = index * (barWidth + spacing) + spacing / 2

            // Draw bar
            drawRect(
                color = Color(0xFF6200EE),
                topLeft = Offset(startX, size.height - bottomPadding - barHeight),
                size = Size(barWidth, barHeight)
            )

            // Для мультиплатформенности мы не можем использовать нативные методы рисования текста
            // В реальном приложении здесь можно использовать другие подходы, например:
            // 1. Использовать Compose Text элементы вместо Canvas для меток
            // 2. Использовать платформо-специфичный код для каждой платформы
            // 3. Использовать библиотеки для кросс-платформенного рисования текста
        }
    }

    // Отображаем легенду под графиком
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        data.forEach { dataPoint ->
            Text(
                text = dataPoint.label,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun PieChart(data: List<DiagramStore.DataPoint>) {
    if (data.isEmpty()) {
        EmptyDataPlaceholder()
        return
    }

    // Animation for pie segments
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    val total = data.sumOf { it.value }
    val colors = listOf(
        Color(0xFF6200EE),
        Color(0xFF03DAC5),
        Color(0xFFFFB300),
        Color(0xFF00C853),
        Color(0xFFD50000),
        Color(0xFF304FFE)
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                val radius = minOf(size.width, size.height) / 2 * 0.8f
                val center = Offset(size.width / 2, size.height / 2)

                var startAngle = 0f

                data.forEachIndexed { index, dataPoint ->
                    val sweepAngle = (dataPoint.value / total * 360f * animatedProgress.value).toFloat()
                    val color = colors[index % colors.size]

                    // Draw pie segment
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )

                    // Draw segment outline
                    drawArc(
                        color = Color.White,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 2f)
                    )

                    startAngle += sweepAngle
                }
            }
        }

        // Легенда для круговой диаграммы
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            data.forEachIndexed { index, dataPoint ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(colors[index % colors.size])
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${dataPoint.label}: ${dataPoint.value}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${(dataPoint.value / total * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun LineChart(data: List<DiagramStore.DataPoint>) {
    if (data.isEmpty()) {
        EmptyDataPlaceholder()
        return
    }

    // Animation for line
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    val maxValue = data.maxOfOrNull { it.value } ?: 1.0

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                val pointWidth = size.width / (data.size - 1)
                val bottomPadding = 40f

                // Draw horizontal axis
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, size.height - bottomPadding),
                    end = Offset(size.width, size.height - bottomPadding),
                    strokeWidth = 2f
                )

                // Draw vertical axis
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height - bottomPadding),
                    strokeWidth = 2f
                )

                // Draw line connecting points
                for (i in 0 until (data.size - 1) * animatedProgress.value.toInt() + 1) {
                    if (i >= data.size - 1) break

                    val startX = i * pointWidth
                    val startY =
                        size.height - bottomPadding - (data[i].value / maxValue * (size.height - bottomPadding - 20f)).toFloat()

                    val endX = (i + 1) * pointWidth
                    val endY =
                        size.height - bottomPadding - (data[i + 1].value / maxValue * (size.height - bottomPadding - 20f)).toFloat()

                    // Draw line segment
                    drawLine(
                        color = Color(0xFF6200EE),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 3f
                    )

                    // Draw point
                    drawCircle(
                        color = Color(0xFF03DAC5),
                        radius = 6f,
                        center = Offset(startX, startY)
                    )

                    // Draw last point
                    if (i == data.size - 2) {
                        drawCircle(
                            color = Color(0xFF03DAC5),
                            radius = 6f,
                            center = Offset(endX, endY)
                        )
                    }
                }
            }
        }

        // Отображаем легенду под графиком
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { dataPoint ->
                Text(
                    text = dataPoint.label,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ScatterPlot(data: List<DiagramStore.DataPoint>) {
    if (data.isEmpty()) {
        EmptyDataPlaceholder()
        return
    }

    // Animation for points
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    val maxValue = data.maxOfOrNull { it.value } ?: 1.0

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                val pointWidth = size.width / data.size
                val bottomPadding = 40f

                // Draw horizontal axis
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, size.height - bottomPadding),
                    end = Offset(size.width, size.height - bottomPadding),
                    strokeWidth = 2f
                )

                // Draw vertical axis
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height - bottomPadding),
                    strokeWidth = 2f
                )

                // Draw points
                for (i in 0 until (data.size * animatedProgress.value).toInt() + 1) {
                    if (i >= data.size) break

                    val x = i * pointWidth + pointWidth / 2
                    val y =
                        size.height - bottomPadding - (data[i].value / maxValue * (size.height - bottomPadding - 20f)).toFloat()

                    // Draw point
                    drawCircle(
                        color = Color(0xFF6200EE),
                        radius = 8f,
                        center = Offset(x, y)
                    )
                }
            }
        }

        // Отображаем легенду под графиком
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { dataPoint ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dataPoint.label,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = dataPoint.value.toString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyDataPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Нет данных для отображения",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}
