package api.tree

import model.DomainResult
import model.tree.TechnologyTreeDomain
import model.tree.TreeConnectionDomain
import model.tree.TreeNodeDomain

/**
 * API для работы с технологическим деревом
 */
interface NetworkTreeApi {
    /**
     * Получить технологическое дерево для курса
     * @param courseId идентификатор курса
     * @param token токен доступа (опционально)
     * @return результат с данными дерева
     */
    suspend fun getTreeForCourse(courseId: String, token: String? = null): DomainResult<TechnologyTreeDomain>
    
    /**
     * Создать новое технологическое дерево для курса
     * @param courseId идентификатор курса
     * @param tree данные дерева
     * @param token токен доступа
     * @return результат с созданным деревом
     */
    suspend fun createTree(courseId: String, tree: TechnologyTreeDomain, token: String): DomainResult<TechnologyTreeDomain>
    
    /**
     * Обновить технологическое дерево для курса
     * @param courseId идентификатор курса
     * @param tree обновленные данные дерева
     * @param token токен доступа
     * @return результат с обновленным деревом
     */
    suspend fun updateTree(courseId: String, tree: TechnologyTreeDomain, token: String): DomainResult<TechnologyTreeDomain>
    
    /**
     * Удалить технологическое дерево для курса
     * @param courseId идентификатор курса
     * @param token токен доступа
     * @return результат операции
     */
    suspend fun deleteTree(courseId: String, token: String): DomainResult<Unit>
    
    /**
     * Обновить узел в технологическом дереве
     * @param courseId идентификатор курса
     * @param nodeId идентификатор узла
     * @param node обновленные данные узла
     * @param token токен доступа
     * @return результат с обновленным деревом
     */
    suspend fun updateNode(courseId: String, nodeId: String, node: TreeNodeDomain, token: String): DomainResult<TechnologyTreeDomain>
    
    /**
     * Добавить узел в технологическое дерево
     * @param courseId идентификатор курса
     * @param node данные нового узла
     * @param token токен доступа
     * @return результат с обновленным деревом
     */
    suspend fun addNode(courseId: String, node: TreeNodeDomain, token: String): DomainResult<TechnologyTreeDomain>
    
    /**
     * Удалить узел из технологического дерева
     * @param courseId идентификатор курса
     * @param nodeId идентификатор узла
     * @param token токен доступа
     * @return результат с обновленным деревом
     */
    suspend fun removeNode(courseId: String, nodeId: String, token: String): DomainResult<TechnologyTreeDomain>
    
    /**
     * Обновить связь в технологическом дереве
     * @param courseId идентификатор курса
     * @param connectionId идентификатор связи
     * @param connection обновленные данные связи
     * @param token токен доступа
     * @return результат с обновленным деревом
     */
    suspend fun updateConnection(courseId: String, connectionId: String, connection: TreeConnectionDomain, token: String): DomainResult<TechnologyTreeDomain>
    
    /**
     * Добавить связь в технологическое дерево
     * @param courseId идентификатор курса
     * @param connection данные новой связи
     * @param token токен доступа
     * @return результат с обновленным деревом
     */
    suspend fun addConnection(courseId: String, connection: TreeConnectionDomain, token: String): DomainResult<TechnologyTreeDomain>
    
    /**
     * Удалить связь из технологического дерева
     * @param courseId идентификатор курса
     * @param connectionId идентификатор связи
     * @param token токен доступа
     * @return результат с обновленным деревом
     */
    suspend fun removeConnection(courseId: String, connectionId: String, token: String): DomainResult<TechnologyTreeDomain>
} 