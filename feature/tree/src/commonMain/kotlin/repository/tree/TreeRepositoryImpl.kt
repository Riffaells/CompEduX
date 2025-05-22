package repository.tree

import api.tree.NetworkTreeApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logging.Logger
import model.DomainError
import model.DomainResult
import model.tree.TechnologyTreeDomain
import model.tree.TreeConnectionDomain
import model.tree.TreeNodeDomain
import navigation.rDispatchers
import repository.auth.TokenRepository

/**
 * Реализация репозитория технологического дерева
 * Обеспечивает взаимодействие между доменным слоем и сетевым API
 */
class TreeRepositoryImpl(
    private val treeApi: NetworkTreeApi,
    private val tokenRepository: TokenRepository,
    private val logger: Logger
) : TreeRepository {

    /**
     * Получить технологическое дерево для курса
     * @param courseId идентификатор курса
     * @return результат с данными дерева
     */
    override suspend fun getTreeForCourse(courseId: String): DomainResult<TechnologyTreeDomain> =
        withContext(rDispatchers.default) {
            logger.d("TechnologyTreeRepositoryImpl: getTreeForCourse($courseId)")

            // Выполняем запрос, передавая токен если он доступен
            val result = treeApi.getTreeForCourse(courseId, tokenRepository.getAccessToken())

            // Обрабатываем результат
            when (result) {
                is DomainResult.Success -> {
                    logger.i("Technology tree retrieved successfully for course: $courseId")
                    logger.i("Technology tree retrieved successfully for course: $result")
                }

                is DomainResult.Error -> {
                    logger.e("Failed to get technology tree: ${result.error.message}")
                }

                is DomainResult.Loading -> {
                    // Не выполняем действий в состоянии загрузки
                }
            }

            result
        }

    /**
     * Создать новое технологическое дерево для курса
     * @param courseId идентификатор курса
     * @param tree данные дерева
     * @return результат с созданным деревом
     */
    override suspend fun createTree(courseId: String, tree: TechnologyTreeDomain): DomainResult<TechnologyTreeDomain> =
        withContext(rDispatchers.default) {
            logger.d("TechnologyTreeRepositoryImpl: createTree($courseId)")

            // Получаем токен из репозитория
            val token = tokenRepository.getAccessToken()

            if (token == null) {
                logger.w("Cannot create technology tree: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }

            val result = treeApi.createTree(courseId, tree, token)

            // Обрабатываем результат
            when (result) {
                is DomainResult.Success -> {
                    logger.i("Technology tree created successfully for course: $courseId")
                }

                is DomainResult.Error -> {
                    logger.e("Failed to create technology tree: ${result.error.message}")
                }

                is DomainResult.Loading -> {
                    // Не выполняем действий в состоянии загрузки
                }
            }

            result
        }

    /**
     * Обновить технологическое дерево для курса
     * @param courseId идентификатор курса
     * @param tree обновленные данные дерева
     * @return результат с обновленным деревом
     */
    override suspend fun updateTree(courseId: String, tree: TechnologyTreeDomain): DomainResult<TechnologyTreeDomain> =
        withContext(Dispatchers.Default) {
            logger.d("TechnologyTreeRepositoryImpl: updateTree($courseId)")

            // Получаем токен из репозитория
            val token = tokenRepository.getAccessToken()

            if (token == null) {
                logger.w("Cannot update technology tree: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }

            val result = treeApi.updateTree(courseId, tree, token)

            // Обрабатываем результат
            when (result) {
                is DomainResult.Success -> {
                    logger.i("Technology tree updated successfully for course: $courseId")
                }

                is DomainResult.Error -> {
                    logger.e("Failed to update technology tree: ${result.error.message}")
                }

                is DomainResult.Loading -> {
                    // Не выполняем действий в состоянии загрузки
                }
            }

            result
        }

    /**
     * Удалить технологическое дерево для курса
     * @param courseId идентификатор курса
     * @return результат операции
     */
    override suspend fun deleteTree(courseId: String): DomainResult<Unit> = withContext(Dispatchers.Default) {
        logger.d("TechnologyTreeRepositoryImpl: deleteTree($courseId)")

        // Получаем токен из репозитория
        val token = tokenRepository.getAccessToken()

        if (token == null) {
            logger.w("Cannot delete technology tree: No access token")
            return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
        }

        val result = treeApi.deleteTree(courseId, token)

        // Обрабатываем результат
        when (result) {
            is DomainResult.Success -> {
                logger.i("Technology tree deleted successfully for course: $courseId")
            }

            is DomainResult.Error -> {
                logger.e("Failed to delete technology tree: ${result.error.message}")
            }

            is DomainResult.Loading -> {
                // Не выполняем действий в состоянии загрузки
            }
        }

        result
    }

    /**
     * Обновить узел в технологическом дереве
     * @param courseId идентификатор курса
     * @param nodeId идентификатор узла
     * @param node обновленные данные узла
     * @return результат с обновленным деревом
     */
    override suspend fun updateNode(
        courseId: String,
        nodeId: String,
        node: TreeNodeDomain
    ): DomainResult<TechnologyTreeDomain> = withContext(Dispatchers.Default) {
        logger.d("TechnologyTreeRepositoryImpl: updateNode($courseId, $nodeId)")

        // Получаем токен из репозитория
        val token = tokenRepository.getAccessToken()

        if (token == null) {
            logger.w("Cannot update node: No access token")
            return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
        }

        val result = treeApi.updateNode(courseId, nodeId, node, token)

        // Обрабатываем результат
        when (result) {
            is DomainResult.Success -> {
                logger.i("Node updated successfully in tree for course: $courseId")
            }

            is DomainResult.Error -> {
                logger.e("Failed to update node: ${result.error.message}")
            }

            is DomainResult.Loading -> {
                // Не выполняем действий в состоянии загрузки
            }
        }

        result
    }

    /**
     * Добавить узел в технологическое дерево
     * @param courseId идентификатор курса
     * @param node данные нового узла
     * @return результат с обновленным деревом
     */
    override suspend fun addNode(courseId: String, node: TreeNodeDomain): DomainResult<TechnologyTreeDomain> =
        withContext(Dispatchers.Default) {
            logger.d("TechnologyTreeRepositoryImpl: addNode($courseId)")

            // Получаем токен из репозитория
            val token = tokenRepository.getAccessToken()

            if (token == null) {
                logger.w("Cannot add node: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }

            val result = treeApi.addNode(courseId, node, token)

            // Обрабатываем результат
            when (result) {
                is DomainResult.Success -> {
                    logger.i("Node added successfully to tree for course: $courseId")
                }

                is DomainResult.Error -> {
                    logger.e("Failed to add node: ${result.error.message}")
                }

                is DomainResult.Loading -> {
                    // Не выполняем действий в состоянии загрузки
                }
            }

            result
        }

    /**
     * Удалить узел из технологического дерева
     * @param courseId идентификатор курса
     * @param nodeId идентификатор узла
     * @return результат с обновленным деревом
     */
    override suspend fun removeNode(courseId: String, nodeId: String): DomainResult<TechnologyTreeDomain> =
        withContext(Dispatchers.Default) {
            logger.d("TechnologyTreeRepositoryImpl: removeNode($courseId, $nodeId)")

            // Получаем токен из репозитория
            val token = tokenRepository.getAccessToken()

            if (token == null) {
                logger.w("Cannot remove node: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }

            val result = treeApi.removeNode(courseId, nodeId, token)

            // Обрабатываем результат
            when (result) {
                is DomainResult.Success -> {
                    logger.i("Node removed successfully from tree for course: $courseId")
                }

                is DomainResult.Error -> {
                    logger.e("Failed to remove node: ${result.error.message}")
                }

                is DomainResult.Loading -> {
                    // Не выполняем действий в состоянии загрузки
                }
            }

            result
        }

    /**
     * Обновить связь в технологическом дереве
     * @param courseId идентификатор курса
     * @param connectionId идентификатор связи
     * @param connection обновленные данные связи
     * @return результат с обновленным деревом
     */
    override suspend fun updateConnection(
        courseId: String,
        connectionId: String,
        connection: TreeConnectionDomain
    ): DomainResult<TechnologyTreeDomain> = withContext(Dispatchers.Default) {
        logger.d("TechnologyTreeRepositoryImpl: updateConnection($courseId, $connectionId)")

        // Получаем токен из репозитория
        val token = tokenRepository.getAccessToken()

        if (token == null) {
            logger.w("Cannot update connection: No access token")
            return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
        }

        val result = treeApi.updateConnection(courseId, connectionId, connection, token)

        // Обрабатываем результат
        when (result) {
            is DomainResult.Success -> {
                logger.i("Connection updated successfully in tree for course: $courseId")
            }

            is DomainResult.Error -> {
                logger.e("Failed to update connection: ${result.error.message}")
            }

            is DomainResult.Loading -> {
                // Не выполняем действий в состоянии загрузки
            }
        }

        result
    }

    /**
     * Добавить связь в технологическое дерево
     * @param courseId идентификатор курса
     * @param connection данные новой связи
     * @return результат с обновленным деревом
     */
    override suspend fun addConnection(
        courseId: String,
        connection: TreeConnectionDomain
    ): DomainResult<TechnologyTreeDomain> = withContext(Dispatchers.Default) {
        logger.d("TechnologyTreeRepositoryImpl: addConnection($courseId)")

        // Получаем токен из репозитория
        val token = tokenRepository.getAccessToken()

        if (token == null) {
            logger.w("Cannot add connection: No access token")
            return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
        }

        val result = treeApi.addConnection(courseId, connection, token)

        // Обрабатываем результат
        when (result) {
            is DomainResult.Success -> {
                logger.i("Connection added successfully to tree for course: $courseId")
            }

            is DomainResult.Error -> {
                logger.e("Failed to add connection: ${result.error.message}")
            }

            is DomainResult.Loading -> {
                // Не выполняем действий в состоянии загрузки
            }
        }

        result
    }

    /**
     * Удалить связь из технологического дерева
     * @param courseId идентификатор курса
     * @param connectionId идентификатор связи
     * @return результат с обновленным деревом
     */
    override suspend fun removeConnection(courseId: String, connectionId: String): DomainResult<TechnologyTreeDomain> =
        withContext(Dispatchers.Default) {
            logger.d("TechnologyTreeRepositoryImpl: removeConnection($courseId, $connectionId)")

            // Получаем токен из репозитория
            val token = tokenRepository.getAccessToken()

            if (token == null) {
                logger.w("Cannot remove connection: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }

            val result = treeApi.removeConnection(courseId, connectionId, token)

            // Обрабатываем результат
            when (result) {
                is DomainResult.Success -> {
                    logger.i("Connection removed successfully from tree for course: $courseId")
                }

                is DomainResult.Error -> {
                    logger.e("Failed to remove connection: ${result.error.message}")
                }

                is DomainResult.Loading -> {
                    // Не выполняем действий в состоянии загрузки
                }
            }

            result
        }
} 