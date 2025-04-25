package ui.tree

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.shape.RoundedCornerShape
import component.TechnologyTreeStore

/**
 * Компонент для отображения текстовых меток для узлов дерева
 */
@Composable
fun NodeLabels(
    modifier: Modifier = Modifier,
    treeData: TechnologyTreeStore.TreeData,
    selectedNodeId: String?,
    panOffset: Offset
) {
    treeData.nodes.forEach { node ->
        val x = node.position.x.toFloat() + panOffset.x
        val y = node.position.y.toFloat() + panOffset.y
        val nodeTitle = TechnologyTreeStore.State.TRANSLATIONS[node.titleKey] ?: node.titleKey
        val isSelected = node.id == selectedNodeId

        // Simplified subtle color scheme without borders
        val textColor = if (isSelected) Color(0xFF000000) else Color(0xFF000000)
        val backgroundColor = if (isSelected) Color(0xFFFFF59D).copy(alpha = 0.7f) else Color(0xFFE1E1E1).copy(alpha = 0.6f)

        Box(
            modifier = Modifier
                .width(110.dp)
                .absoluteOffset(x = (x - 55).dp, y = (y + 45).dp)
                .background(backgroundColor, RoundedCornerShape(4.dp))
                .padding(vertical = 3.dp, horizontal = 4.dp)
                .then(modifier),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nodeTitle,
                color = textColor,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            )
        }
    }
}

/**
 * Компонент для отображения содержимого информационной карточки о выбранном узле
 */
@Composable
fun SelectedNodeInfo(
    node: TechnologyTreeStore.TreeNode
) {
    Text(
        text = TechnologyTreeStore.State.TRANSLATIONS[node.titleKey] ?: node.titleKey,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold
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
        text = "Content: ${node.contentId}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )

    Text(
        text = "Style: ${node.style}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )

    Text(
        text = "Position: (${node.position.x}, ${node.position.y})",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )
}
