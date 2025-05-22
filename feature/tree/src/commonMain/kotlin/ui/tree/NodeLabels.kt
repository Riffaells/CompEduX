package ui.tree

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.tree.TechnologyTreeDomain
import model.tree.TreeNodeDomain

/**
 * Компонент для отображения текстовых меток для узлов дерева
 */
@Composable
fun NodeLabels(
    modifier: Modifier = Modifier,
    treeData: TechnologyTreeDomain,
    selectedNodeId: String?,
    panOffset: Offset
) {
    // Отображаем метки для всех узлов
    treeData.nodes.forEach { node ->
        // Позиция узла с учетом панорамирования
        val x = node.position.x + panOffset.x
        val y = node.position.y + panOffset.y
        val isSelected = node.id == selectedNodeId

        // Получаем заголовок узла в нужном языке (русском или любом доступном)
        val nodeTitle = node.title.content["ru"]
            ?: node.title.content["en"]
            ?: node.title.content.values.firstOrNull()
            ?: "Без названия"

        // Создаем метку с фоном, который соответствует выбранному узлу
        NodeLabel(
            title = nodeTitle,
            x = x,
            y = y,
            isSelected = isSelected,
            modifier = modifier
        )
    }
}

/**
 * Компонент для отображения отдельной метки узла
 */
@Composable
private fun NodeLabel(
    title: String,
    x: Float,
    y: Float,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    // Цвета для разных состояний метки
    val backgroundColor = if (isSelected)
        Color(0xFFFFF9C4).copy(alpha = 0.9f)
    else
        Color(0xFFE0E0E0).copy(alpha = 0.7f)

    val textColor = if (isSelected)
        Color(0xFF000000)
    else
        Color(0xFF424242)

    // Отображаем метку под узлом с отступом от узла
    Box(
        modifier = Modifier
            .width(140.dp)
            .absoluteOffset(x = (x - 70).dp, y = (y + 40).dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 8.dp)
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * Компонент для отображения содержимого информационной карточки о выбранном узле
 */
@Composable
fun SelectedNodeInfo(
    node: TreeNodeDomain,
    modifier: Modifier = Modifier
) {
    val nodeTitle = node.title.content["ru"]
        ?: node.title.content["en"]
        ?: node.title.content.values.firstOrNull()
        ?: "Без названия"

    val nodeDescription = node.description.content["ru"]
        ?: node.description.content["en"]
        ?: node.description.content.values.firstOrNull()
        ?: "Без описания"

    Box(modifier = modifier) {
        Text(
            text = nodeTitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )

        Text(
            text = nodeDescription,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )

        Text(
            text = "ID: ${node.id}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Text(
            text = "Тип: ${node.type.name}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        if (node.contentId != null) {
            Text(
                text = "Content ID: ${node.contentId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Text(
            text = "Позиция: (${node.position.x}, ${node.position.y})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Text(
            text = "Сложность: ${node.difficulty}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        if (node.requirements.isNotEmpty()) {
            Text(
                text = "Требования: ${node.requirements.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
