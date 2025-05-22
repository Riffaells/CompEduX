package api.tree

import base.BaseNetworkApi
import client.safeSendWithErrorBody
import config.NetworkConfig
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import logging.Logger
import mapper.toDomain
import mapper.toNetwork
import model.DomainError
import model.DomainResult
import model.tree.TechnologyTreeDomain
import model.tree.TreeConnectionDomain
import model.tree.TreeNodeDomain
import model.tree.*

/**
 * Implementation of TreeApi that uses Ktor HttpClient
 * to perform API requests
 */
class NetworkTreeApiImpl(
    client: HttpClient,
    networkConfig: NetworkConfig,
    logger: Logger
) : BaseNetworkApi(client, networkConfig, logger), NetworkTreeApi {

    /**
     * Get a technology tree for a specific course
     * @param courseId the course identifier
     * @param token authorization token
     * @return tree data result
     */
    override suspend fun getTreeForCourse(courseId: String, token: String?): DomainResult<TechnologyTreeDomain> {
        return executeWithRetry {
            try {
                val apiUrl = getApiUrl()

                client.safeSendWithErrorBody<NetworkTechnologyTree, NetworkTreeErrorResponse>(
                    {
                        url("$apiUrl/courses/$courseId/tree")
                        method = HttpMethod.Get
                        // Add authorization header if token is provided
                        token?.let { header(HttpHeaders.Authorization, "Bearer $it") }

                        logger.d("Getting technology tree for course: $courseId")
                    },
                    logger,
                    { errorResponse ->
                        logger.w("Get tree failed: ${errorResponse.getErrorMessage()}")
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details.toString()
                        )
                    }
                ).also {
                    if (it is DomainResult.Success) {
                        logger.i("Technology tree retrieved successfully for course: $courseId")

                        // Подробное логирование полученного дерева
                        val tree = it.data

                        if (tree.data != null) {
                            // Логирование для нового формата с полем data
                            logger.d("Получено дерево (новый формат) с ${tree.data.nodes.size} узлами и ${tree.data.connections.size} соединениями")

                            if (tree.data.nodes.isNotEmpty()) {
                                tree.data.nodes.forEach { (nodeId, node) ->
                                    logger.d("Узел $nodeId: " +
                                        "позиция=(${node.position.x}, ${node.position.y}), " +
                                        "тип=${node.type}, " +
                                        "title=${node.title}")
                                }
                            } else {
                                logger.w("Дерево не содержит узлов!")
                            }

                            if (tree.data.connections.isNotEmpty()) {
                                tree.data.connections.forEachIndexed { index, connection ->
                                    logger.d("Соединение #$index: id=${connection.id}, " +
                                        "from=${connection.from}, to=${connection.to}, " +
                                        "тип=${connection.type}")
                                }
                            } else {
                                logger.w("Дерево не содержит соединений!")
                            }
                        } else {
                            // Логирование для старого формата
                            logger.d("Получено дерево (старый формат) с ${tree.nodes.size} узлами и ${tree.connections.size} соединениями")

                            if (tree.nodes.isNotEmpty()) {
                                tree.nodes.forEachIndexed { index, node ->
                                    logger.d("Узел #$index: id=${node.id}, " +
                                        "позиция=(${node.position.x}, ${node.position.y}), " +
                                        "тип=${node.type}, " +
                                        "title=${node.title}")
                                }
                            } else {
                                logger.w("Дерево не содержит узлов!")
                            }

                            if (tree.connections.isNotEmpty()) {
                                tree.connections.forEachIndexed { index, connection ->
                                    logger.d("Соединение #$index: id=${connection.id}, " +
                                        "from=${connection.from}, to=${connection.to}, " +
                                        "тип=${connection.type}")
                                }
                            } else {
                                logger.w("Дерево не содержит соединений!")
                            }
                        }
                    }
                }.map { networkResponse ->
                    try {
                        val domainTree = networkResponse.toDomain()

                        // Подробное логирование преобразованного дерева
                        logger.d("Преобразованное дерево: ${domainTree.nodes.size} узлов, ${domainTree.connections.size} соединений")
                        if (domainTree.nodes.isNotEmpty()) {
                            domainTree.nodes.forEachIndexed { index, node ->
                                logger.d("Преобразованный узел #$index: id=${node.id}, " +
                                    "позиция=(${node.position.x}, ${node.position.y}), " +
                                    "тип=${node.type}, " +
                                    "title=${node.title.content}")
                            }
                        }

                        domainTree
                    } catch (e: Exception) {
                        logger.e("Ошибка при преобразовании дерева: ${e.message}")
                        logger.e("Стек трассировки: ${e.stackTraceToString()}")
                        throw e
                    }
                }
            } catch (e: Exception) {
                logger.e("Ошибка при получении дерева: ${e.message}")
                e.printStackTrace()
                processApiException(e, "Get technology tree")
            }
        }
    }

    /**
     * Create a new technology tree for a course
     * @param courseId the course identifier
     * @param tree tree data
     * @param token authorization token
     * @return created tree result
     */
    override suspend fun createTree(
        courseId: String,
        tree: TechnologyTreeDomain,
        token: String?
    ): DomainResult<TechnologyTreeDomain> {
        return executeWithRetry {
            if (token == null) {
                return@executeWithRetry DomainResult.Error(DomainError.authError("Authorization token is required"))
            }

            try {
                val apiUrl = getApiUrl()

                client.safeSendWithErrorBody<NetworkTechnologyTree, NetworkTreeErrorResponse>(
                    {
                        url("$apiUrl/courses/$courseId/tree")
                        method = HttpMethod.Post
                        header(HttpHeaders.Authorization, "Bearer $token")
                        contentType(ContentType.Application.Json)
                        setBody(tree.toNetwork())

                        logger.d("Creating technology tree for course: $courseId")
                    },
                    logger,
                    { errorResponse ->
                        logger.w("Create tree failed: ${errorResponse.getErrorMessage()}")
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details.toString()
                        )
                    }
                ).also {
                    if (it is DomainResult.Success) {
                        logger.i("Technology tree created successfully for course: $courseId")
                    }
                }.map { networkResponse ->
                    networkResponse.toDomain()
                }
            } catch (e: Exception) {
                processApiException(e, "Create technology tree")
            }
        }
    }

    /**
     * Update a technology tree for a course
     * @param courseId the course identifier
     * @param tree updated tree data
     * @param token authorization token
     * @return updated tree result
     */
    override suspend fun updateTree(
        courseId: String,
        tree: TechnologyTreeDomain,
        token: String?
    ): DomainResult<TechnologyTreeDomain> {
        return executeWithRetry {
            if (token == null) {
                return@executeWithRetry DomainResult.Error(DomainError.authError("Authorization token is required"))
            }

            try {
                val apiUrl = getApiUrl()

                client.safeSendWithErrorBody<NetworkTechnologyTree, NetworkTreeErrorResponse>(
                    {
                        url("$apiUrl/courses/$courseId/tree")
                        method = HttpMethod.Put
                        header(HttpHeaders.Authorization, "Bearer $token")
                        contentType(ContentType.Application.Json)
                        setBody(tree.toNetwork())

                        logger.d("Updating technology tree for course: $courseId")
                    },
                    logger,
                    { errorResponse ->
                        logger.w("Update tree failed: ${errorResponse.getErrorMessage()}")
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details.toString()
                        )
                    }
                ).also {
                    if (it is DomainResult.Success) {
                        logger.i("Technology tree updated successfully for course: $courseId")
                    }
                }.map { networkResponse ->
                    networkResponse.toDomain()
                }
            } catch (e: Exception) {
                processApiException(e, "Update technology tree")
            }
        }
    }

    /**
     * Delete a technology tree for a course
     * @param courseId the course identifier
     * @param token authorization token
     * @return operation result
     */
    override suspend fun deleteTree(courseId: String, token: String?): DomainResult<Unit> {
        return executeWithRetry {
            if (token == null) {
                return@executeWithRetry DomainResult.Error(DomainError.authError("Authorization token is required"))
            }

            try {
                val apiUrl = getApiUrl()

                client.safeSendWithErrorBody<Unit, NetworkTreeErrorResponse>(
                    {
                        url("$apiUrl/courses/$courseId/tree")
                        method = HttpMethod.Delete
                        header(HttpHeaders.Authorization, "Bearer $token")

                        logger.d("Deleting technology tree for course: $courseId")
                    },
                    logger,
                    { errorResponse ->
                        logger.w("Delete tree failed: ${errorResponse.getErrorMessage()}")
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details.toString()
                        )
                    }
                ).also {
                    if (it is DomainResult.Success) {
                        logger.i("Technology tree deleted successfully for course: $courseId")
                    }
                }
            } catch (e: Exception) {
                processApiException(e, "Delete technology tree")
            }
        }
    }

    /**
     * Update a node in the technology tree
     * @param courseId the course identifier
     * @param nodeId the node identifier
     * @param node updated node data
     * @param token authorization token
     * @return updated tree result
     */
    override suspend fun updateNode(
        courseId: String,
        nodeId: String,
        node: TreeNodeDomain,
        token: String?
    ): DomainResult<TechnologyTreeDomain> {
        return executeWithRetry {
            if (token == null) {
                return@executeWithRetry DomainResult.Error(DomainError.authError("Authorization token is required"))
            }

            try {
                val apiUrl = getApiUrl()

                client.safeSendWithErrorBody<NetworkTechnologyTree, NetworkTreeErrorResponse>(
                    {
                        url("$apiUrl/courses/$courseId/tree/nodes/$nodeId")
                        method = HttpMethod.Put
                        header(HttpHeaders.Authorization, "Bearer $token")
                        contentType(ContentType.Application.Json)
                        setBody(node.toNetwork())

                        logger.d("Updating node $nodeId in tree for course: $courseId")
                    },
                    logger,
                    { errorResponse ->
                        logger.w("Update node failed: ${errorResponse.getErrorMessage()}")
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details.toString()
                        )
                    }
                ).also {
                    if (it is DomainResult.Success) {
                        logger.i("Node updated successfully in tree for course: $courseId")
                    }
                }.map { networkResponse ->
                    networkResponse.toDomain()
                }
            } catch (e: Exception) {
                processApiException(e, "Update node")
            }
        }
    }

    /**
     * Add a new node to the technology tree
     * @param courseId the course identifier
     * @param node new node data
     * @param token authorization token
     * @return updated tree result
     */
    override suspend fun addNode(
        courseId: String,
        node: TreeNodeDomain,
        token: String?
    ): DomainResult<TechnologyTreeDomain> {
        return executeWithRetry {
            if (token == null) {
                return@executeWithRetry DomainResult.Error(DomainError.authError("Authorization token is required"))
            }

            try {
                val apiUrl = getApiUrl()

                client.safeSendWithErrorBody<NetworkTechnologyTree, NetworkTreeErrorResponse>(
                    {
                        url("$apiUrl/courses/$courseId/tree/nodes")
                        method = HttpMethod.Post
                        header(HttpHeaders.Authorization, "Bearer $token")
                        contentType(ContentType.Application.Json)
                        setBody(node.toNetwork())

                        logger.d("Adding node to tree for course: $courseId")
                    },
                    logger,
                    { errorResponse ->
                        logger.w("Add node failed: ${errorResponse.getErrorMessage()}")
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details.toString()
                        )
                    }
                ).also {
                    if (it is DomainResult.Success) {
                        logger.i("Node added successfully to tree for course: $courseId")
                    }
                }.map { networkResponse ->
                    networkResponse.toDomain()
                }
            } catch (e: Exception) {
                processApiException(e, "Add node")
            }
        }
    }

    /**
     * Remove a node from the technology tree
     * @param courseId the course identifier
     * @param nodeId the node identifier
     * @param token authorization token
     * @return updated tree result
     */
    override suspend fun removeNode(
        courseId: String,
        nodeId: String,
        token: String?
    ): DomainResult<TechnologyTreeDomain> {
        return executeWithRetry {
            if (token == null) {
                return@executeWithRetry DomainResult.Error(DomainError.authError("Authorization token is required"))
            }

            try {
                val apiUrl = getApiUrl()

                client.safeSendWithErrorBody<NetworkTechnologyTree, NetworkTreeErrorResponse>(
                    {
                        url("$apiUrl/courses/$courseId/tree/nodes/$nodeId")
                        method = HttpMethod.Delete
                        header(HttpHeaders.Authorization, "Bearer $token")

                        logger.d("Removing node $nodeId from tree for course: $courseId")
                    },
                    logger,
                    { errorResponse ->
                        logger.w("Remove node failed: ${errorResponse.getErrorMessage()}")
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details.toString()
                        )
                    }
                ).also {
                    if (it is DomainResult.Success) {
                        logger.i("Node removed successfully from tree for course: $courseId")
                    }
                }.map { networkResponse ->
                    networkResponse.toDomain()
                }
            } catch (e: Exception) {
                processApiException(e, "Remove node")
            }
        }
    }

    /**
     * Update a connection in the technology tree
     * @param courseId the course identifier
     * @param connectionId the connection identifier
     * @param connection updated connection data
     * @param token authorization token
     * @return updated tree result
     */
    override suspend fun updateConnection(
        courseId: String,
        connectionId: String,
        connection: TreeConnectionDomain,
        token: String?
    ): DomainResult<TechnologyTreeDomain> {
        return executeWithRetry {
            if (token == null) {
                return@executeWithRetry DomainResult.Error(DomainError.authError("Authorization token is required"))
            }

            try {
                val apiUrl = getApiUrl()

                client.safeSendWithErrorBody<NetworkTechnologyTree, NetworkTreeErrorResponse>(
                    {
                        url("$apiUrl/courses/$courseId/tree/connections/$connectionId")
                        method = HttpMethod.Put
                        header(HttpHeaders.Authorization, "Bearer $token")
                        contentType(ContentType.Application.Json)
                        setBody(connection.toNetwork())

                        logger.d("Updating connection $connectionId in tree for course: $courseId")
                    },
                    logger,
                    { errorResponse ->
                        logger.w("Update connection failed: ${errorResponse.getErrorMessage()}")
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details.toString()
                        )
                    }
                ).also {
                    if (it is DomainResult.Success) {
                        logger.i("Connection updated successfully in tree for course: $courseId")
                    }
                }.map { networkResponse ->
                    networkResponse.toDomain()
                }
            } catch (e: Exception) {
                processApiException(e, "Update connection")
            }
        }
    }

    /**
     * Add a new connection to the technology tree
     * @param courseId the course identifier
     * @param connection new connection data
     * @param token authorization token
     * @return updated tree result
     */
    override suspend fun addConnection(
        courseId: String,
        connection: TreeConnectionDomain,
        token: String?
    ): DomainResult<TechnologyTreeDomain> {
        return executeWithRetry {
            if (token == null) {
                return@executeWithRetry DomainResult.Error(DomainError.authError("Authorization token is required"))
            }

            try {
                val apiUrl = getApiUrl()

                client.safeSendWithErrorBody<NetworkTechnologyTree, NetworkTreeErrorResponse>(
                    {
                        url("$apiUrl/courses/$courseId/tree/connections")
                        method = HttpMethod.Post
                        header(HttpHeaders.Authorization, "Bearer $token")
                        contentType(ContentType.Application.Json)
                        setBody(connection.toNetwork())

                        logger.d("Adding connection to tree for course: $courseId")
                    },
                    logger,
                    { errorResponse ->
                        logger.w("Add connection failed: ${errorResponse.getErrorMessage()}")
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details.toString()
                        )
                    }
                ).also {
                    if (it is DomainResult.Success) {
                        logger.i("Connection added successfully to tree for course: $courseId")
                    }
                }.map { networkResponse ->
                    networkResponse.toDomain()
                }
            } catch (e: Exception) {
                processApiException(e, "Add connection")
            }
        }
    }

    /**
     * Remove a connection from the technology tree
     * @param courseId the course identifier
     * @param connectionId the connection identifier
     * @param token authorization token
     * @return updated tree result
     */
    override suspend fun removeConnection(
        courseId: String,
        connectionId: String,
        token: String?
    ): DomainResult<TechnologyTreeDomain> {
        return executeWithRetry {
            if (token == null) {
                return@executeWithRetry DomainResult.Error(DomainError.authError("Authorization token is required"))
            }

            try {
                val apiUrl = getApiUrl()

                client.safeSendWithErrorBody<NetworkTechnologyTree, NetworkTreeErrorResponse>(
                    {
                        url("$apiUrl/courses/$courseId/tree/connections/$connectionId")
                        method = HttpMethod.Delete
                        header(HttpHeaders.Authorization, "Bearer $token")

                        logger.d("Removing connection $connectionId from tree for course: $courseId")
                    },
                    logger,
                    { errorResponse ->
                        logger.w("Remove connection failed: ${errorResponse.getErrorMessage()}")
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details.toString()
                        )
                    }
                ).also {
                    if (it is DomainResult.Success) {
                        logger.i("Connection removed successfully from tree for course: $courseId")
                    }
                }.map { networkResponse ->
                    networkResponse.toDomain()
                }
            } catch (e: Exception) {
                processApiException(e, "Remove connection")
            }
        }
    }
}
