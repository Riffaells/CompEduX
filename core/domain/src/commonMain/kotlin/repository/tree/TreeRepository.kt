package repository.tree

import model.DomainResult
import model.tree.TechnologyTreeDomain
import model.tree.TreeConnectionDomain
import model.tree.TreeNodeDomain

/**
 * Repository for technology tree operations
 */
interface TreeRepository {

    /**
     * Get a technology tree for a specific course
     * @param courseId the course identifier
     * @return tree data result
     */
    suspend fun getTreeForCourse(courseId: String): DomainResult<TechnologyTreeDomain>

    /**
     * Create a new technology tree for a course
     * @param courseId the course identifier
     * @param tree tree data
     * @return created tree result
     */
    suspend fun createTree(courseId: String, tree: TechnologyTreeDomain): DomainResult<TechnologyTreeDomain>

    /**
     * Update a technology tree for a course
     * @param courseId the course identifier
     * @param tree updated tree data
     * @return updated tree result
     */
    suspend fun updateTree(courseId: String, tree: TechnologyTreeDomain): DomainResult<TechnologyTreeDomain>

    /**
     * Delete a technology tree for a course
     * @param courseId the course identifier
     * @return operation result
     */
    suspend fun deleteTree(courseId: String): DomainResult<Unit>

    /**
     * Update a node in the technology tree
     * @param courseId the course identifier
     * @param nodeId the node identifier
     * @param node updated node data
     * @return updated tree result
     */
    suspend fun updateNode(courseId: String, nodeId: String, node: TreeNodeDomain): DomainResult<TechnologyTreeDomain>

    /**
     * Add a new node to the technology tree
     * @param courseId the course identifier
     * @param node new node data
     * @return updated tree result
     */
    suspend fun addNode(courseId: String, node: TreeNodeDomain): DomainResult<TechnologyTreeDomain>

    /**
     * Remove a node from the technology tree
     * @param courseId the course identifier
     * @param nodeId the node identifier
     * @return updated tree result
     */
    suspend fun removeNode(courseId: String, nodeId: String): DomainResult<TechnologyTreeDomain>

    /**
     * Update a connection in the technology tree
     * @param courseId the course identifier
     * @param connectionId the connection identifier
     * @param connection updated connection data
     * @return updated tree result
     */
    suspend fun updateConnection(
        courseId: String,
        connectionId: String,
        connection: TreeConnectionDomain
    ): DomainResult<TechnologyTreeDomain>

    /**
     * Add a new connection to the technology tree
     * @param courseId the course identifier
     * @param connection new connection data
     * @return updated tree result
     */
    suspend fun addConnection(courseId: String, connection: TreeConnectionDomain): DomainResult<TechnologyTreeDomain>

    /**
     * Remove a connection from the technology tree
     * @param courseId the course identifier
     * @param connectionId the connection identifier
     * @return updated tree result
     */
    suspend fun removeConnection(courseId: String, connectionId: String): DomainResult<TechnologyTreeDomain>
} 