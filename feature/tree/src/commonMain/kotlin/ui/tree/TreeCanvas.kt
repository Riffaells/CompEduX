package ui.tree

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import component.TechnologyTreeStore
import ui.theme.TreeTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.atan2
// Import hexagon path creation functions and other node renderers
import ui.tree.createHexagonPath
import ui.tree.createTopHexagonPath
import ui.tree.getNodeColorGradient
import ui.tree.drawArrowConnection
import ui.tree.drawDashedConnection
import ui.tree.drawSimpleConnection

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
