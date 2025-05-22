package component

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import logging.Logger
import model.course.*
import model.tree.CanvasSizeDomain
import model.tree.ConnectionTypeDomain
import model.tree.NodePositionDomain
import model.tree.NodeStateDomain
import model.tree.TechnologyTreeDomain
import model.tree.TreeConnectionDomain
import model.tree.TreeLayoutDirectionDomain
import model.tree.TreeLayoutTypeDomain
import model.tree.TreeMetadataDomain
import model.tree.TreeNodeDomain
import model.tree.TreeNodeTypeDomain
import navigation.rDispatchers
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import usecase.tree.TreeUseCases

interface TechnologyTreeStore : Store<TechnologyTreeStore.Intent, TechnologyTreeStore.State, Nothing> {

    sealed interface Intent {
        data object Init : Intent
        data object Back : Intent
        data class UpdateJsonInput(val jsonText: String) : Intent
        data class ParseJson(val jsonText: String) : Intent
        data class ImportTreeFromJson(val jsonText: String) : Intent
        data class NodeClicked(val nodeId: String?) : Intent
        data class NodeMoved(val nodeId: String, val newPosition: NodePositionDomain) : Intent
    }

    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
        val jsonInput: String = "",
        val technologyTree: TechnologyTreeDomain? = null,
        val selectedNodeId: String? = null
    )
}

internal class TechnologyTreeStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    private val logger by instance<Logger>()
    private val treeUseCases by instance<TreeUseCases>()

    fun create(courseId: String): TechnologyTreeStore =
        object : TechnologyTreeStore,
            Store<TechnologyTreeStore.Intent, TechnologyTreeStore.State, Nothing> by storeFactory.create(
                name = "TechnologyTreeStore",
                initialState = TechnologyTreeStore.State(),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = { ExecutorImpl(courseId) },
                reducer = ReducerImpl
            ) {}

    private sealed interface Msg {
        data object LoadingData : Msg
        data class TreeLoaded(val tree: TechnologyTreeDomain) : Msg
        data class JsonInputUpdated(val jsonText: String) : Msg
        data class ErrorOccurred(val error: String) : Msg
        data class NodeSelected(val nodeId: String?) : Msg
        data class NodePositionUpdated(val nodeId: String, val newPosition: NodePositionDomain) : Msg
    }

    private inner class ExecutorImpl(private val courseId: String) :
        CoroutineExecutor<TechnologyTreeStore.Intent, Unit, TechnologyTreeStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            scope.launch {
                loadTree()
            }
        }

        // Безопасный вызов dispatch, который перехватывает исключения
        private fun safeDispatch(msg: Msg) {
            try {
                dispatch(msg)
            } catch (e: Exception) {
                logger.e("Error in dispatch: ${e.message}")
                dispatch(Msg.ErrorOccurred("Ошибка: ${e.message}"))
            }
        }

        private fun loadTree() {
            scope.launch {
                try {
                    dispatch(Msg.LoadingData)
                    val result = treeUseCases.getTreeForCourse(courseId)
                    result.onSuccess { tree ->
                        logger.i("Дерево успешно загружено: ${tree.nodes.size} узлов, ${tree.connections.size} соединений")
                        // Подробное логирование для отладки
                        tree.nodes.forEachIndexed { index, node ->
                            logger.d("Узел #$index: id=${node.id}, позиция=(${node.position.x}, ${node.position.y}), тип=${node.type}")
                        }

                        // Вывод JSON представления дерева для копирования
                        try {
                            val jsonFormatter = Json {
                                prettyPrint = true
                                isLenient = true
                                ignoreUnknownKeys = true
                            }
                            val treeJson = jsonFormatter.encodeToString(TechnologyTreeDomain.serializer(), tree)
                            logger.i("JSON ПРЕДСТАВЛЕНИЕ ДЕРЕВА (для копирования):")
                            logger.i("-------------BEGIN TREE JSON-------------")
                            logger.i(treeJson)
                            logger.i("-------------END TREE JSON-------------")
                        } catch(e: Exception) {
                            logger.e("Ошибка сериализации дерева в JSON: ${e.message}")
                        }

                        dispatch(Msg.TreeLoaded(tree))
                    }.onError { error ->
                        logger.e("Ошибка загрузки дерева: ${error.message}")
                        dispatch(Msg.ErrorOccurred("Ошибка загрузки дерева: ${error.message}"))
                    }
                } catch (e: Exception) {
                    logger.e("Исключение при загрузке дерева: ${e.message}", e)
                    dispatch(Msg.ErrorOccurred("Ошибка загрузки дерева: ${e.message}"))
                }
            }
        }

        private fun importTreeFromJson(jsonText: String) {
            scope.launch {
                try {
                    dispatch(Msg.LoadingData)
                    logger.i("Начало импорта дерева из JSON (первые 100 символов): ${jsonText.take(100)}...")

                    val json = Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }

                    val jsonObject = json.parseToJsonElement(jsonText).jsonObject

                    // Определяем формат данных: новый (с полем data) или старый
                    val isCourseIdFormat = jsonObject.containsKey("course_id") && jsonObject.containsKey("data")

                    // Извлекаем данные в зависимости от формата
                    val dataObject = if (isCourseIdFormat) {
                        // Новый формат с course_id и data
                        logger.d("Обнаружен формат JSON с полями course_id и data")
                        jsonObject["data"]?.jsonObject ?: jsonObject
                    } else {
                        // Старый формат без data
                        logger.d("Обнаружен стандартный формат JSON")
                        jsonObject
                    }

                    // Получаем ID курса
                    val courseId = jsonObject["course_id"]?.jsonPrimitive?.content ?: this@ExecutorImpl.courseId

                    // Извлекаем узлы из JSON
                    val nodesMap = dataObject["nodes"]?.jsonObject

                    if (nodesMap == null) {
                        logger.e("В JSON отсутствует объект nodes: ${jsonText.take(100)}...")
                        dispatch(Msg.ErrorOccurred("В JSON отсутствует объект nodes"))
                        return@launch
                    }

                    logger.d("Обрабатываем ${nodesMap.entries.size} узлов")

                    val nodes = nodesMap.entries.map { (nodeId, nodeJson) ->
                        val nodeObj = nodeJson.jsonObject

                        // Получаем данные узла
                        val id = nodeObj["id"]?.jsonPrimitive?.content ?: nodeId

                        // Обрабатываем локализованный контент для заголовка
                        val titleObj = nodeObj["title"]?.jsonObject
                        val title = if (titleObj != null) {
                            LocalizedContent(titleObj.entries.associate {
                                it.key to (it.value.jsonPrimitive.content)
                            })
                        } else {
                            // Если title не объект, а строка
                            val titleStr = nodeObj["title"]?.jsonPrimitive?.content ?: ""
                            LocalizedContent(mapOf("ru" to titleStr, "en" to titleStr))
                        }

                        // Обрабатываем локализованный контент для описания
                        val descObj = nodeObj["description"]?.jsonObject
                        val description = if (descObj != null) {
                            LocalizedContent(descObj.entries.associate {
                                it.key to (it.value.jsonPrimitive.content)
                            })
                        } else {
                            // Если description не объект, а строка
                            val descStr = nodeObj["description"]?.jsonPrimitive?.content ?: ""
                            LocalizedContent(mapOf("ru" to descStr, "en" to descStr))
                        }

                        // Позиция узла
                        val posObj = nodeObj["position"]?.jsonObject
                        val x = posObj?.get("x")?.jsonPrimitive?.content?.toFloatOrNull() ?:
                               nodeObj["x"]?.jsonPrimitive?.content?.toFloatOrNull() ?:
                               0f
                        val y = posObj?.get("y")?.jsonPrimitive?.content?.toFloatOrNull() ?:
                               nodeObj["y"]?.jsonPrimitive?.content?.toFloatOrNull() ?:
                               0f
                        val position = NodePositionDomain(x, y)

                        // Остальные данные
                        val style = nodeObj["style"]?.jsonPrimitive?.content
                        val contentId = nodeObj["content_id"]?.jsonPrimitive?.content ?:
                                        nodeObj["contentId"]?.jsonPrimitive?.content

                        // Требования (ids других узлов)
                        val requirements = when {
                            nodeObj["requirements"]?.jsonArray != null -> {
                                nodeObj["requirements"]?.jsonArray?.mapNotNull {
                                    it.jsonPrimitive.contentOrNull
                                } ?: emptyList()
                            }
                            nodeObj["requirements"]?.jsonObject != null -> {
                                nodeObj["requirements"]?.jsonObject?.entries?.map { it.key } ?: emptyList()
                            }
                            else -> emptyList()
                        }

                        logger.d("Узел $id: позиция ($x, $y), требования: $requirements")

                        // Тип узла
                        val typeStr = nodeObj["type"]?.jsonPrimitive?.content?.lowercase() ?: "topic"
                        val type = try {
                            TreeNodeTypeDomain.valueOf(typeStr.uppercase())
                        } catch (e: Exception) {
                            when (typeStr.lowercase()) {
                                "topic" -> TreeNodeTypeDomain.TOPIC
                                "skill" -> TreeNodeTypeDomain.SKILL
                                "module" -> TreeNodeTypeDomain.MODULE
                                "article" -> TreeNodeTypeDomain.ARTICLE
                                else -> TreeNodeTypeDomain.TOPIC
                            }
                        }

                        // Статус узла - используем ключ status вместо state для совместимости с API
                        val statusStr = nodeObj["status"]?.jsonPrimitive?.content?.lowercase() ?: "available"
                        val state = try {
                            NodeStateDomain.valueOf(statusStr.uppercase())
                        } catch (e: Exception) {
                            when (statusStr.lowercase()) {
                                "available" -> NodeStateDomain.AVAILABLE
                                "locked" -> NodeStateDomain.LOCKED
                                "completed" -> NodeStateDomain.COMPLETED
                                "in_progress" -> NodeStateDomain.IN_PROGRESS
                                "published" -> NodeStateDomain.AVAILABLE
                                else -> NodeStateDomain.AVAILABLE
                            }
                        }

                        // Сложность и время
                        val difficulty = nodeObj["difficulty"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1
                        val estimatedTime = nodeObj["estimated_time"]?.jsonPrimitive?.content?.toIntOrNull() ?:
                                           nodeObj["estimatedTime"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0

                        // Создаем объект узла
                        TreeNodeDomain(
                            id = id,
                            title = title,
                            description = description,
                            type = type,
                            position = position,
                            style = style,
                            contentId = contentId,
                            requirements = requirements,
                            state = state,
                            difficulty = difficulty,
                            estimatedTime = estimatedTime
                        )
                    }

                    // Извлекаем соединения из JSON
                    val connectionsArray = dataObject["connections"]?.jsonArray
                    var connections = emptyList<TreeConnectionDomain>()

                    if (connectionsArray != null) {
                        connections = connectionsArray.mapIndexed { index, connJson ->
                            val connObj = connJson.jsonObject

                            val id = connObj["id"]?.jsonPrimitive?.content ?: "conn$index"
                            val from = connObj["from"]?.jsonPrimitive?.content ?: ""
                            val to = connObj["to"]?.jsonPrimitive?.content ?: ""

                            // Тип соединения
                            val typeStr = connObj["type"]?.jsonPrimitive?.content?.lowercase() ?: "required"
                            val type = try {
                                ConnectionTypeDomain.valueOf(typeStr.uppercase())
                            } catch (e: Exception) {
                                when (typeStr.lowercase()) {
                                    "required" -> ConnectionTypeDomain.REQUIRED
                                    "recommended" -> ConnectionTypeDomain.RECOMMENDED
                                    "optional" -> ConnectionTypeDomain.OPTIONAL
                                    else -> ConnectionTypeDomain.REQUIRED
                                }
                            }

                            val style = connObj["style"]?.jsonPrimitive?.content
                            val label = connObj["label"]?.jsonPrimitive?.content

                            TreeConnectionDomain(
                                id = id,
                                from = from,
                                to = to,
                                type = type,
                                style = style,
                                label = label
                            )
                        }

                        logger.d("Обработано ${connections.size} соединений")
                    } else {
                        // Если соединения не заданы явно, автоматически создаем их на основе requirements
                        val connectionMap = mutableMapOf<String, TreeConnectionDomain>()

                        nodes.forEach { node ->
                            node.requirements.forEach { reqId ->
                                val connId = "conn_${reqId}_${node.id}"
                                connectionMap[connId] = TreeConnectionDomain(
                                    id = connId,
                                    from = reqId,
                                    to = node.id,
                                    type = ConnectionTypeDomain.REQUIRED,
                                    style = null,
                                    label = null
                                )
                            }
                        }

                        connections = connectionMap.values.toList()
                        logger.d("Автоматически созданы ${connections.size} соединений на основе требований")
                    }

                    // Извлекаем метаданные
                    val metadataObj = dataObject["metadata"]?.jsonObject ?: jsonObject["metadata"]?.jsonObject

                    // Значения по умолчанию
                    val defaultLanguage = metadataObj?.get("defaultLanguage")?.jsonPrimitive?.content ?:
                                         metadataObj?.get("default_language")?.jsonPrimitive?.content ?: "ru"

                    // Доступные языки
                    val availableLanguages = when {
                        metadataObj?.get("availableLanguages")?.jsonArray != null -> {
                            metadataObj["availableLanguages"]?.jsonArray?.mapNotNull {
                                it.jsonPrimitive.contentOrNull
                            } ?: listOf("ru")
                        }
                        metadataObj?.get("available_languages")?.jsonArray != null -> {
                            metadataObj["available_languages"]?.jsonArray?.mapNotNull {
                                it.jsonPrimitive.contentOrNull
                            } ?: listOf("ru")
                        }
                        else -> listOf("ru")
                    }

                    // Тип и направление макета
                    val layoutTypeStr = metadataObj?.get("layoutType")?.jsonPrimitive?.content?.lowercase() ?:
                                       metadataObj?.get("layout_type")?.jsonPrimitive?.content?.lowercase() ?: "tree"
                    val layoutType = try {
                        TreeLayoutTypeDomain.valueOf(layoutTypeStr.uppercase())
                    } catch (e: Exception) {
                        TreeLayoutTypeDomain.TREE
                    }

                    val layoutDirStr = metadataObj?.get("layoutDirection")?.jsonPrimitive?.content?.lowercase() ?:
                                      metadataObj?.get("layout_direction")?.jsonPrimitive?.content?.lowercase() ?: "horizontal"
                    val layoutDirection = try {
                        TreeLayoutDirectionDomain.valueOf(layoutDirStr.uppercase())
                    } catch (e: Exception) {
                        TreeLayoutDirectionDomain.HORIZONTAL
                    }

                    // Размер канваса
                    val canvasSizeObj = metadataObj?.get("canvasSize")?.jsonObject ?:
                                       metadataObj?.get("canvas_size")?.jsonObject
                    val width = canvasSizeObj?.get("width")?.jsonPrimitive?.content?.toIntOrNull() ?: 800
                    val height = canvasSizeObj?.get("height")?.jsonPrimitive?.content?.toIntOrNull() ?: 600

                    val metadata = TreeMetadataDomain(
                        defaultLanguage = defaultLanguage,
                        availableLanguages = availableLanguages,
                        layoutType = layoutType,
                        layoutDirection = layoutDirection,
                        canvasSize = CanvasSizeDomain(width, height)
                    )

                    // ID дерева из JSON или из URL-параметра
                    val treeId = jsonObject["id"]?.jsonPrimitive?.content ?:
                                 dataObject["id"]?.jsonPrimitive?.content ?:
                                 "${courseId}_tree"

                    // Версия
                    val version = jsonObject["version"]?.jsonPrimitive?.content?.toIntOrNull() ?:
                                 dataObject["version"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1

                    // Даты
                    val createdAt = jsonObject["created_at"]?.jsonPrimitive?.content ?:
                                   dataObject["created_at"]?.jsonPrimitive?.content ?:
                                   java.time.Instant.now().toString()

                    val updatedAt = jsonObject["updated_at"]?.jsonPrimitive?.content ?:
                                   dataObject["updated_at"]?.jsonPrimitive?.content ?:
                                   java.time.Instant.now().toString()

                    // Создаем объект дерева
                    val treeData = TechnologyTreeDomain(
                        id = treeId,
                        version = version,
                        courseId = courseId,
                        createdAt = createdAt,
                        updatedAt = updatedAt,
                        nodes = nodes,
                        connections = connections,
                        groups = emptyList(),
                        metadata = metadata
                    )

                    logger.i("Успешно импортировано дерево: ${nodes.size} узлов, ${connections.size} соединений")

                    // Подробное логирование для отладки
                    nodes.forEachIndexed { index, node ->
                        logger.d("Импортированный узел #$index: id=${node.id}, позиция=(${node.position.x}, ${node.position.y}), тип=${node.type}")
                    }

                    // Вывод JSON представления дерева для копирования (уже отформатированного)
                    try {
                        val jsonFormatter = Json {
                            prettyPrint = true
                            isLenient = true
                            ignoreUnknownKeys = true
                        }
                        val treeJson = jsonFormatter.encodeToString(TechnologyTreeDomain.serializer(), treeData)
                        logger.i("JSON ПРЕДСТАВЛЕНИЕ ИМПОРТИРОВАННОГО ДЕРЕВА (для копирования):")
                        logger.i("-------------BEGIN IMPORTED TREE JSON-------------")
                        logger.i(treeJson)
                        logger.i("-------------END IMPORTED TREE JSON-------------")
                    } catch(e: Exception) {
                        logger.e("Ошибка сериализации импортированного дерева в JSON: ${e.message}")
                    }

                    dispatch(Msg.TreeLoaded(treeData))

                } catch (e: Exception) {
                    logger.e("Error importing tree from JSON: ${e.message}")
                    e.printStackTrace() // Добавляем стек-трейс для отладки
                    dispatch(Msg.ErrorOccurred("Ошибка импорта дерева из JSON: ${e.message}"))
                }
            }
        }

        override fun executeIntent(intent: TechnologyTreeStore.Intent): Unit =
            try {
                when (intent) {
                    is TechnologyTreeStore.Intent.Init -> {
                        loadTree()
                    }

                    is TechnologyTreeStore.Intent.Back -> {
                        // Обработка в компоненте
                    }

                    is TechnologyTreeStore.Intent.UpdateJsonInput -> {
                        dispatch(Msg.JsonInputUpdated(intent.jsonText))
                    }

                    is TechnologyTreeStore.Intent.ParseJson -> {
                        // Больше не используется, т.к. используем доменную модель
                    }

                    is TechnologyTreeStore.Intent.ImportTreeFromJson -> {
                        importTreeFromJson(intent.jsonText)
                    }

                    is TechnologyTreeStore.Intent.NodeClicked -> {
                        dispatch(Msg.NodeSelected(intent.nodeId))
                    }

                    is TechnologyTreeStore.Intent.NodeMoved -> {
                        dispatch(Msg.NodePositionUpdated(intent.nodeId, intent.newPosition))
                    }
                }
            } catch (e: Exception) {
                logger.e("Error in executeIntent: ${e.message}")
                dispatch(Msg.ErrorOccurred("Ошибка: ${e.message}"))
            }
    }

    private object ReducerImpl : Reducer<TechnologyTreeStore.State, Msg> {
        override fun TechnologyTreeStore.State.reduce(msg: Msg): TechnologyTreeStore.State =
            when (msg) {
                is Msg.LoadingData -> copy(isLoading = true, error = null)
                is Msg.TreeLoaded -> copy(isLoading = false, technologyTree = msg.tree, error = null)
                is Msg.JsonInputUpdated -> copy(jsonInput = msg.jsonText)
                is Msg.ErrorOccurred -> copy(error = msg.error, isLoading = false)
                is Msg.NodeSelected -> copy(selectedNodeId = msg.nodeId)
                is Msg.NodePositionUpdated -> {
                    // Обновляем позицию узла в дереве
                    technologyTree?.let { currentTree ->
                        val updatedNodes = currentTree.nodes.map { node ->
                            if (node.id == msg.nodeId) {
                                node.copy(position = msg.newPosition)
                            } else {
                                node
                            }
                        }
                        val updatedTree = currentTree.copy(nodes = updatedNodes)
                        copy(technologyTree = updatedTree)
                    } ?: this
                }
            }
    }
}
