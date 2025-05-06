package ui.tree

// Import hexagon path creation functions and other node renderers
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import component.TechnologyTreeStore
import ui.theme.TreeTheme
import kotlin.math.sqrt

/**
 * Компонент, отвечающий за отрисовку дерева на канвасе с использованием системы тем
 */
@Composable
fun TreeCanvas(
    modifier: Modifier = Modifier,
    treeData: TechnologyTreeStore.TreeData,
    selectedNodeId: String?,
    panOffset: Offset,
    theme: TreeTheme? = null,
    pulseScale: Float = 1f,
    glowAlpha: Float = 0.3f,
    onNodeClick: (String) -> Unit = {}
) {
    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                // Применяем обратное смещение для проверки попадания по узлу
                val adjustedOffset = Offset(
                    offset.x - panOffset.x,
                    offset.y - panOffset.y
                )

                // Ищем узел, по которому кликнули
                val clickedNode = treeData.nodes.find { node ->
                    val nodePos = Offset(node.position.x.toFloat(), node.position.y.toFloat())
                    val distance = sqrt(
                        (adjustedOffset.x - nodePos.x) * (adjustedOffset.x - nodePos.x) +
                                (adjustedOffset.y - nodePos.y) * (adjustedOffset.y - nodePos.y)
                    )
                    distance <= 35f // радиус узла
                }

                // Если нашли узел, вызываем коллбэк
                clickedNode?.let { node ->
                    onNodeClick(node.id)
                }
            }
        }
    ) {
        // Применяем трансформацию canvas для учета панорамирования
        translate(panOffset.x, panOffset.y) {
            // Сначала рисуем соединения
            for (connection in treeData.connections) {
                val fromNode = treeData.nodes.find { it.id == connection.from }
                val toNode = treeData.nodes.find { it.id == connection.to }

                if (fromNode != null && toNode != null) {
                    val fromX = fromNode.position.x.toFloat()
                    val fromY = fromNode.position.y.toFloat()
                    val toX = toNode.position.x.toFloat()
                    val toY = toNode.position.y.toFloat()

                    // Определяем, выбран ли кто-то из узлов
                    val isConnectionHighlighted = fromNode.id == selectedNodeId || toNode.id == selectedNodeId

                    // Создаем градиент для линии с более яркими цветами
                    val gradient = Brush.linearGradient(
                        colors = listOf(
                            getNodeColorGradient(fromNode, selectedNodeId).first(),
                            getNodeColorGradient(toNode, selectedNodeId).last()
                        ),
                        start = Offset(fromX, fromY),
                        end = Offset(toX, toY)
                    )

                    // Рисуем линию в зависимости от стиля
                    when (connection.style) {
                        "solid_arrow" -> drawArrowConnection(
                            fromX, fromY, toX, toY, isConnectionHighlighted, gradient
                        )

                        "dashed_line" -> drawDashedConnection(
                            fromX, fromY, toX, toY, isConnectionHighlighted, gradient
                        )

                        else -> drawSimpleConnection(
                            fromX, fromY, toX, toY, isConnectionHighlighted, gradient
                        )
                    }
                }
            }

            // Затем рисуем все узлы
            for (node in treeData.nodes) {
                val x = node.position.x.toFloat()
                val y = node.position.y.toFloat()
                val isSelected = node.id == selectedNodeId

                // Получаем градиент для узла
                val nodeGradient = getNodeColorGradient(node, selectedNodeId)

                // Масштаб пульсации только для выбранного узла
                val currentScale = if (isSelected) pulseScale else 1f

                // Эффект свечения вокруг узла - улучшенный с анимированной прозрачностью
                if (isSelected) {
                    drawCircle(
                        color = nodeGradient.first().copy(alpha = glowAlpha),
                        radius = 50f * currentScale,
                        center = Offset(x, y),
                        blendMode = BlendMode.SrcOver
                    )
                }

                // Рисуем форму узла в зависимости от стиля
                when (node.style) {
                    "circular" -> drawCircularNode(node, x, y, isSelected, nodeGradient, currentScale)
                    "hexagon" -> drawHexagonNode(node, x, y, isSelected, nodeGradient, currentScale)
                    "square" -> drawSquareNode(node, x, y, isSelected, nodeGradient, currentScale)
                    else -> drawCircularNode(node, x, y, isSelected, nodeGradient, currentScale)
                }
            }
        }
    }
}
