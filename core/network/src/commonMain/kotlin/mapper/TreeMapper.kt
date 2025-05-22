package mapper

import model.course.*
import model.tree.*

/**
 * Extension functions for mapping between network and domain technology tree models
 */

// Network to domain mappings
fun NetworkTechnologyTree.toDomain(): TechnologyTreeDomain {
    // Если данные содержатся в поле data, используем их
    if (data != null) {
        val nodesList = data.nodes.map { (nodeId, node) ->
            // Убедимся, что ID узла установлен правильно
            val nodeWithId = if (node.id.isBlank()) node.copy(id = nodeId) else node
            nodeWithId.toDomain()
        }

        return TechnologyTreeDomain(
            id = id,
            version = version,
            courseId = courseId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            nodes = nodesList,
            connections = data.connections.map { it.toDomain() },
            groups = emptyList(), // Обычно группы не используются в этом формате
            metadata = data.metadata.toDomain()
        )
    }

    // Иначе используем старый формат с прямым списком узлов
    return TechnologyTreeDomain(
        id = id,
        version = version,
        courseId = courseId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        nodes = nodes.map { it.toDomain() },
        connections = connections.map { it.toDomain() },
        groups = groups.map { it.toDomain() },
        metadata = metadata.toDomain()
    )
}

fun NetworkTreeNode.toDomain(): TreeNodeDomain {
    // Проверка и установка значения позиции по умолчанию, если position = null
    val safePosition = position?.toDomain() ?: NodePositionDomain(
        x = 100f + (id.hashCode() % 500),  // Генерируем случайную позицию на основе id
        y = 150f + (id.hashCode() % 300)
    )

    return TreeNodeDomain(
        id = id,
        title = LocalizedContent(title),
        description = LocalizedContent(description),
        type = parseNodeType(type),
        position = safePosition,
        style = style,
        contentId = contentId,
        requirements = requirements,
        state = parseNodeState(status),
        difficulty = 1, // Значение по умолчанию, так как в новом формате нет difficulty
        estimatedTime = 0 // Значение по умолчанию, так как в новом формате нет estimatedTime
    )
}

fun NetworkNodePosition.toDomain(): NodePositionDomain {
    // Проверка и установка значений по умолчанию для предотвращения проблем с отображением
    val safeX = if (x.isNaN() || x == Float.NEGATIVE_INFINITY || x == Float.POSITIVE_INFINITY) 100f else x
    val safeY = if (y.isNaN() || y == Float.NEGATIVE_INFINITY || y == Float.POSITIVE_INFINITY) 150f else y

    return NodePositionDomain(
        x = safeX,
        y = safeY
    )
}

fun NetworkTreeConnection.toDomain(): TreeConnectionDomain {
    return TreeConnectionDomain(
        id = id,
        from = from,
        to = to,
        type = parseConnectionType(type),
        style = style,
        label = label
    )
}

fun NetworkTreeGroup.toDomain(): TreeGroupDomain {
    return TreeGroupDomain(
        id = id,
        name = LocalizedContent(name),
        nodes = nodes,
        style = style
    )
}

fun NetworkTreeMetadata.toDomain(): TreeMetadataDomain {
    return TreeMetadataDomain(
        defaultLanguage = defaultLanguage,
        availableLanguages = availableLanguages,
        layoutType = parseLayoutType(layoutType),
        layoutDirection = parseLayoutDirection(layoutDirection),
        canvasSize = canvasSize.toDomain()
    )
}

fun NetworkCanvasSize.toDomain(): CanvasSizeDomain {
    // Установка разумных значений по умолчанию, если получены некорректные данные
    val safeWidth = if (width <= 0) 800 else width
    val safeHeight = if (height <= 0) 600 else height

    return CanvasSizeDomain(
        width = safeWidth,
        height = safeHeight
    )
}

// Domain to network mappings
fun TechnologyTreeDomain.toNetwork(): NetworkTechnologyTree {
    // Создаем объект в обоих форматах для совместимости
    val nodesList = nodes.map { it.toNetwork() }
    val nodesMap = nodes.associate { it.id to it.toNetwork() }

    // Создаем data-объект для нового формата
    val treeData = NetworkTreeData(
        nodes = nodesMap,
        connections = connections.map { it.toNetwork() },
        metadata = metadata.toNetwork()
    )

    return NetworkTechnologyTree(
        id = id,
        version = version,
        courseId = courseId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isPublished = false, // Устанавливаем по умолчанию false, если требуется
        data = treeData,
        nodes = nodesList,
        connections = connections.map { it.toNetwork() },
        groups = groups.map { it.toNetwork() },
        metadata = metadata.toNetwork()
    )
}

fun TreeNodeDomain.toNetwork(): NetworkTreeNode {
    return NetworkTreeNode(
        id = id,
        title = title.content,
        description = description.content,
        type = type.toString().lowercase(),
        position = position.toNetwork(),
        style = style,
        contentId = contentId,
        requirements = requirements,
        status = state.toString().lowercase()
    )
}

fun NodePositionDomain.toNetwork(): NetworkNodePosition {
    return NetworkNodePosition(
        x = x,
        y = y
    )
}

fun TreeConnectionDomain.toNetwork(): NetworkTreeConnection {
    return NetworkTreeConnection(
        id = id,
        from = from,
        to = to,
        type = type.toString().lowercase(),
        style = style,
        label = label
    )
}

fun TreeGroupDomain.toNetwork(): NetworkTreeGroup {
    return NetworkTreeGroup(
        id = id,
        name = name.content,
        nodes = nodes,
        style = style
    )
}

fun TreeMetadataDomain.toNetwork(): NetworkTreeMetadata {
    return NetworkTreeMetadata(
        defaultLanguage = defaultLanguage,
        availableLanguages = availableLanguages,
        layoutType = layoutType.toString().lowercase(),
        layoutDirection = layoutDirection.toString().lowercase(),
        canvasSize = canvasSize.toNetwork()
    )
}

fun CanvasSizeDomain.toNetwork(): NetworkCanvasSize {
    return NetworkCanvasSize(
        width = width,
        height = height
    )
}

// Helper functions for parsing enums
private fun parseNodeType(type: String): TreeNodeTypeDomain {
    return when (type.lowercase()) {
        "skill" -> TreeNodeTypeDomain.SKILL
        "module" -> TreeNodeTypeDomain.MODULE
        "article" -> TreeNodeTypeDomain.ARTICLE
        else -> TreeNodeTypeDomain.TOPIC
    }
}

private fun parseNodeState(state: String): NodeStateDomain {
    return when (state.lowercase()) {
        "locked" -> NodeStateDomain.LOCKED
        "completed" -> NodeStateDomain.COMPLETED
        "in_progress" -> NodeStateDomain.IN_PROGRESS
        "published" -> NodeStateDomain.AVAILABLE
        else -> NodeStateDomain.AVAILABLE
    }
}

private fun parseConnectionType(type: String): ConnectionTypeDomain {
    return when (type.lowercase()) {
        "recommended" -> ConnectionTypeDomain.RECOMMENDED
        "optional" -> ConnectionTypeDomain.OPTIONAL
        else -> ConnectionTypeDomain.REQUIRED
    }
}

private fun parseLayoutType(type: String): TreeLayoutTypeDomain {
    return when (type.lowercase()) {
        "mesh" -> TreeLayoutTypeDomain.MESH
        "radial" -> TreeLayoutTypeDomain.RADIAL
        else -> TreeLayoutTypeDomain.TREE
    }
}

private fun parseLayoutDirection(direction: String): TreeLayoutDirectionDomain {
    return when (direction.lowercase()) {
        "vertical" -> TreeLayoutDirectionDomain.VERTICAL
        else -> TreeLayoutDirectionDomain.HORIZONTAL
    }
}
