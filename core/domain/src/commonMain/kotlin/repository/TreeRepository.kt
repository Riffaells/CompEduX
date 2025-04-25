package repository

import model.DomainResult
import model.tree.TreeData

/**
 * Репозиторий для работы с деревом разработки
 */
interface TreeRepository {
    /**
     * Получить дерево разработки
     * @return [DomainResult] с деревом разработки
     */
    suspend fun getTree(): DomainResult<TreeData>

    /**
     * Обновить прогресс узла дерева
     * @param nodeId ID узла
     * @param completed Завершен ли узел
     * @param progress Прогресс узла
     * @return [DomainResult] с обновленным деревом
     */
    suspend fun updateNodeProgress(nodeId: String, completed: Boolean, progress: Float): DomainResult<TreeData>
}
