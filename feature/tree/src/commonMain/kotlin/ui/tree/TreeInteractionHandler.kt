package ui.tree

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import component.TechnologyTreeComponent
import component.TechnologyTreeStore

/**
 * Файл содержит функции для обработки взаимодействия с деревом технологий:
 * - панорамирование поля
 * - перемещение отдельных узлов
 */

/**
 * Модификатор для обработки панорамирования всего поля
 */
fun Modifier.handleTreePanning(
    isPanning: MutableState<Boolean>,
    panOffset: MutableState<Offset>,
    resetPanButtonVisible: MutableState<Boolean>
): Modifier = this.pointerInput(Unit) {
    detectDragGestures(
        onDragStart = { isPanning.value = true },
        onDragEnd = {
            isPanning.value = false
            resetPanButtonVisible.value = panOffset.value != Offset.Zero
        },
        onDrag = { change, dragAmount ->
            change.consume()
            panOffset.value += dragAmount
            resetPanButtonVisible.value = true
        }
    )
}

/**
 * Модификатор для обработки перетаскивания узлов
 */
fun Modifier.handleNodeDragging(
    treeData: TechnologyTreeStore.TreeData,
    component: TechnologyTreeComponent,
    panOffset: MutableState<Offset>,
    selectedNodeId: String?
): Modifier = this.pointerInput(treeData) {
    detectDragGestures(
        onDragStart = { offset ->
            // Проверяем, попал ли клик на какой-либо узел с учетом смещения
            for (node in treeData.nodes) {
                val nodeX = node.position.x.toFloat() + panOffset.value.x
                val nodeY = node.position.y.toFloat() + panOffset.value.y
                val radius = when (node.style) {
                    "circular" -> 32f
                    "hexagon", "square" -> 35f
                    else -> 32f
                }

                if (offset.x in (nodeX - radius)..(nodeX + radius) &&
                    offset.y in (nodeY - radius)..(nodeY + radius)
                ) {
                    // Выбираем узел в начале перемещения
                    component.onEvent(TechnologyTreeStore.Intent.NodeClicked(node.id))
                    break
                }
            }
        },
        onDrag = { change, dragAmount ->
            val dragNodeId = selectedNodeId ?: return@detectDragGestures
            val dragNode = treeData.nodes.find { it.id == dragNodeId } ?: return@detectDragGestures

            // Обновляем позицию узла
            val newPosX = (dragNode.position.x + dragAmount.x).toInt()
            val newPosY = (dragNode.position.y + dragAmount.y).toInt()

            // Отправляем событие о перемещении узла
            component.onEvent(
                TechnologyTreeStore.Intent.NodeMoved(
                    dragNodeId,
                    TechnologyTreeStore.Position(newPosX, newPosY)
                )
            )
        }
    )
}

/**
 * Модификатор для обработки щелчка по узлу
 */
fun Modifier.handleNodeTapping(
    treeData: TechnologyTreeStore.TreeData,
    component: TechnologyTreeComponent,
    panOffset: MutableState<Offset>
): Modifier = this.pointerInput(treeData) {
    detectTapGestures { tapOffset ->
        // Проверяем, попал ли клик на какой-либо узел с учетом смещения
        for (node in treeData.nodes) {
            val nodeX = node.position.x.toFloat() + panOffset.value.x
            val nodeY = node.position.y.toFloat() + panOffset.value.y
            val radius = when (node.style) {
                "circular" -> 32f
                "hexagon", "square" -> 35f
                else -> 32f
            }

            if (tapOffset.x in (nodeX - radius)..(nodeX + radius) &&
                tapOffset.y in (nodeY - radius)..(nodeY + radius)
            ) {
                component.onEvent(TechnologyTreeStore.Intent.NodeClicked(node.id))
                break
            }
        }
    }
}

/**
 * Функция для определения, попадает ли точка в узел
 */
fun isPointInNode(
    node: TechnologyTreeStore.TreeNode,
    point: Offset,
    panOffset: Offset
): Boolean {
    val nodeX = node.position.x.toFloat() + panOffset.x
    val nodeY = node.position.y.toFloat() + panOffset.y
    val radius = when (node.style) {
        "circular" -> 32f
        "hexagon", "square" -> 35f
        else -> 32f
    }

    return point.x in (nodeX - radius)..(nodeX + radius) &&
            point.y in (nodeY - radius)..(nodeY + radius)
}
