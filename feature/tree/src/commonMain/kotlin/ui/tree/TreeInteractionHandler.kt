package ui.tree

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import component.TechnologyTreeComponent
import component.TechnologyTreeStore
import model.tree.NodePositionDomain
import model.tree.TechnologyTreeDomain
import model.tree.TreeNodeDomain

/**
 * Файл содержит функции для обработки взаимодействия с деревом технологий:
 * - панорамирование (перемещение) поля
 * - перемещение отдельных узлов
 * - обработка кликов по узлам
 */

/**
 * Модификатор для обработки панорамирования всего поля
 */
fun Modifier.handleCanvasPanning(
    isPanning: MutableState<Boolean>,
    panOffset: MutableState<Offset>,
    onPanningChanged: (Boolean) -> Unit = {}
): Modifier = this.pointerInput(Unit) {
    detectDragGestures(
        onDragStart = {
            isPanning.value = true
            onPanningChanged(true)
        },
        onDragEnd = {
            isPanning.value = false
            onPanningChanged(false)
        },
        onDrag = { change, dragAmount ->
            change.consume()
            // Обновляем смещение панорамирования
            panOffset.value += dragAmount
        }
    )
}

/**
 * Модификатор для обработки перетаскивания узлов
 */
fun Modifier.handleNodeDragging(
    treeData: TechnologyTreeDomain,
    component: TechnologyTreeComponent,
    panOffset: MutableState<Offset>,
    selectedNodeId: MutableState<String?>,
    isDraggingNode: MutableState<Boolean>
): Modifier = this.pointerInput(treeData) {
    detectDragGestures(
        onDragStart = { startPoint ->
            // Находим узел, по которому кликнули
            val clickedNode = findNodeAtPosition(treeData, startPoint, panOffset.value)

            if (clickedNode != null) {
                // Отмечаем узел как выбранный
                selectedNodeId.value = clickedNode.id
                isDraggingNode.value = true
                component.onEvent(TechnologyTreeStore.Intent.NodeClicked(clickedNode.id))
            }
        },
        onDragEnd = {
            isDraggingNode.value = false
        },
        onDrag = { change, dragAmount ->
            change.consume()

            // Если выбран узел - перемещаем его
            val nodeId = selectedNodeId.value
            if (nodeId != null && isDraggingNode.value) {
                val node = treeData.nodes.find { it.id == nodeId }

                if (node != null) {
                    // Вычисляем новую позицию с учетом смещения
                    val newX = node.position.x + dragAmount.x
                    val newY = node.position.y + dragAmount.y

                    // Отправляем событие перемещения узла
                    component.onEvent(
                        TechnologyTreeStore.Intent.NodeMoved(
                            nodeId,
                            NodePositionDomain(newX, newY)
                        )
                    )
                }
            }
        }
    )
}

/**
 * Модификатор для обработки кликов по узлам
 */
fun Modifier.handleNodeTapping(
    treeData: TechnologyTreeDomain,
    component: TechnologyTreeComponent,
    panOffset: MutableState<Offset>
): Modifier = this.pointerInput(treeData) {
    detectTapGestures { tapPoint ->
        // Находим узел, по которому кликнули
        val clickedNode = findNodeAtPosition(treeData, tapPoint, panOffset.value)

        if (clickedNode != null) {
            // Отправляем событие клика по узлу
            component.onEvent(TechnologyTreeStore.Intent.NodeClicked(clickedNode.id))
        } else {
            // Если клик был вне узла, снимаем выделение
            component.onEvent(TechnologyTreeStore.Intent.NodeClicked(null))
        }
    }
}

/**
 * Функция для определения узла по клику с учетом смещения панорамирования
 */
private fun findNodeAtPosition(
    treeData: TechnologyTreeDomain,
    point: Offset,
    panOffset: Offset,
    nodeRadius: Float = 30f
): TreeNodeDomain? {
    return treeData.nodes.firstOrNull { node ->
        val nodeX = node.position.x + panOffset.x
        val nodeY = node.position.y + panOffset.y

        // Расчет расстояния между точкой клика и центром узла
        val distance = kotlin.math.sqrt(
            (point.x - nodeX) * (point.x - nodeX) +
            (point.y - nodeY) * (point.y - nodeY)
        )

        // Проверяем, попадает ли клик внутрь узла
        distance <= nodeRadius
    }
}

/**
 * Функция-расширение для проверки, находится ли точка внутри узла
 */
fun TreeNodeDomain.containsPoint(
    point: Offset,
    panOffset: Offset,
    radius: Float = 30f
): Boolean {
    val nodeX = position.x + panOffset.x
    val nodeY = position.y + panOffset.y

    val distance = kotlin.math.sqrt(
        (point.x - nodeX) * (point.x - nodeX) +
        (point.y - nodeY) * (point.y - nodeY)
    )

    return distance <= radius
}
