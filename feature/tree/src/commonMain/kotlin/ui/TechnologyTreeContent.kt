package ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import component.TechnologyTreeComponent
import component.TechnologyTreeStore
import ui.tree.NodeLabels
import ui.tree.SelectedNodeInfo
import ui.tree.TreeCanvas

@Composable
fun TechnologyTreeContent(
    modifier: Modifier = Modifier,
    component: TechnologyTreeComponent
) {
    val state by component.state.collectAsState()
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    // Removed dragNodeId since we're only focusing on field panning for now

    // Improved animation for selected node
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAnimation by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Glow animation
    val glowAnimation by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Визуализация дерева курса",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Основной контейнер для отображения дерева - без рамки
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            state.parsedTree?.let { treeData ->
                // Отображение позиции (если смещено)
                if (panOffset != Offset.Zero) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Позиция: (${panOffset.x.toInt()}, ${panOffset.y.toInt()})",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Информация о выбранном узле
                state.selectedNodeId?.let { selectedNodeId ->
                    val selectedNode = treeData.nodes.find { it.id == selectedNodeId }
                    selectedNode?.let { node ->
                        Card(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .width(200.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                SelectedNodeInfo(node)
                            }
                        }
                    }
                }

                // Канвас для отрисовки дерева и взаимодействия
                Box(modifier = Modifier.fillMaxSize()) {
                    // Simplified to only handle panning
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { isDragging = true },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        // Only panning, no node movement
                                        panOffset += dragAmount
                                    },
                                    onDragEnd = { isDragging = false }
                                )
                            }
                    )

                    // Используем TreeCanvas компонент для отрисовки с улучшенной анимацией
                    TreeCanvas(
                        modifier = Modifier.fillMaxSize(),
                        treeData = treeData,
                        selectedNodeId = state.selectedNodeId,
                        panOffset = panOffset,
                        pulseScale = if (state.selectedNodeId != null) pulseAnimation else 1f,
                        glowAlpha = if (state.selectedNodeId != null) glowAnimation else 0.3f,
                        onNodeClick = { nodeId ->
                            component.onEvent(TechnologyTreeStore.Intent.NodeClicked(nodeId))
                        }
                    )

                    // Отображение текстовых меток
                    NodeLabels(
                        treeData = treeData,
                        selectedNodeId = state.selectedNodeId,
                        panOffset = panOffset
                    )
                }
            } ?: run {
                // Если данные дерева не загружены
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Text(
                            text = "Дерево не загружено",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Подсказка для пользователя
        Text(
            text = "Для перемещения схемы перетаскивайте поле",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Ошибка, если есть
        state.error?.let { error ->
            Text(
                text = "Ошибка: $error",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Текстовое поле для ввода JSON
        OutlinedTextField(
            value = state.jsonInput,
            onValueChange = { component.onEvent(TechnologyTreeStore.Intent.UpdateJsonInput(it)) },
            label = { Text("JSON Дерева") },
            modifier = Modifier.fillMaxWidth().height(150.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Кнопки действий
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { component.onEvent(TechnologyTreeStore.Intent.ParseJson(state.jsonInput)) },
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
