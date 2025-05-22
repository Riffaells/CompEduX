package component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import logging.Logger
import model.tree.NodePositionDomain
import model.tree.TechnologyTreeDomain
import model.tree.TreeNodeDomain
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

/**
 * Параметры для создания компонента дерева технологий
 */
data class TechnologyTreeComponentParams(
    val componentContext: ComponentContext,
    val courseId: String,
    val onBack: () -> Unit
)

/**
 * Компонент для работы с деревом технологий
 */
interface TechnologyTreeComponent {
    /**
     * Состояние компонента
     */
    val state: StateFlow<TechnologyTreeStore.State>

    /**
     * Отправить событие в стор
     */
    fun onEvent(event: TechnologyTreeStore.Intent)

    /**
     * Обработать нажатие кнопки "Назад"
     */
    fun onBackClicked()

    /**
     * Импортировать дерево из JSON
     */
    fun importTreeFromJson(jsonText: String)

    /**
     * Обновить позицию узла
     */
    fun updateNodePosition(nodeId: String, newPosition: NodePositionDomain)

    /**
     * Выбрать узел
     */
    fun selectNode(nodeId: String?)

    /**
     * Получить выбранный узел
     */
    fun getSelectedNode(): TreeNodeDomain?

    /**
     * Сбросить смещение панорамирования
     */
    fun resetPanOffset()
}

/**
 * Реализация компонента дерева технологий
 */
class DefaultTechnologyTreeComponent(
    componentContext: ComponentContext,
    private val courseId: String,
    private val onBack: () -> Unit,
    override val di: DI
) : TechnologyTreeComponent, DIAware, ComponentContext by componentContext {

    private val technologyTreeStoreFactory by instance<TechnologyTreeStoreFactory>()
    private val logger by instance<Logger>()
    private val scope = CoroutineScope(Dispatchers.Main)
    private val jsonFormatter = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    private val store = instanceKeeper.getStore {
        technologyTreeStoreFactory.create(courseId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<TechnologyTreeStore.State> = store.stateFlow

    override fun onEvent(event: TechnologyTreeStore.Intent) {
        logger.d("TechnologyTreeComponent: onEvent($event)")
        try {
            store.accept(event)
        } catch (e: Exception) {
            logger.e("Error sending event to store: ${e.message}")
        }
    }

    override fun onBackClicked() {
        logger.d("TechnologyTreeComponent: onBackClicked()")
        onBack()
    }

    override fun importTreeFromJson(jsonText: String) {
        logger.d("TechnologyTreeComponent: importTreeFromJson(${jsonText.take(50)}...)")

        if (jsonText.isBlank()) {
            logger.w("Attempt to import empty JSON")
            return
        }

        scope.launch {
            try {
                onEvent(TechnologyTreeStore.Intent.ImportTreeFromJson(jsonText))
            } catch (e: Exception) {
                logger.e("Error importing tree from JSON: ${e.message}")
            }
        }
    }

    override fun updateNodePosition(nodeId: String, newPosition: NodePositionDomain) {
        logger.d("TechnologyTreeComponent: updateNodePosition($nodeId, $newPosition)")
        onEvent(TechnologyTreeStore.Intent.NodeMoved(nodeId, newPosition))
    }

    override fun selectNode(nodeId: String?) {
        logger.d("TechnologyTreeComponent: selectNode($nodeId)")
        onEvent(TechnologyTreeStore.Intent.NodeClicked(nodeId))
    }

    override fun getSelectedNode(): TreeNodeDomain? {
        val selectedNodeId = state.value.selectedNodeId ?: return null
        return state.value.technologyTree?.nodes?.find { it.id == selectedNodeId }
    }

    override fun resetPanOffset() {
        // Этот метод должен быть реализован в UI слое, так как панорамирование
        // обрабатывается на уровне Compose, а не в сторе
        logger.d("TechnologyTreeComponent: resetPanOffset()")
    }
}
