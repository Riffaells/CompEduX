package usecase.tree

import model.DomainResult
import model.tree.TechnologyTreeDomain
import model.tree.TreeNodeDomain
import repository.tree.TreeRepository

/**
 * Use case for updating a node in a technology tree
 */
class UpdateNodeUseCase(
    private val repository: TreeRepository
) {
    /**
     * Update a node in the technology tree
     * @param courseId the course identifier
     * @param nodeId the node identifier
     * @param node updated node data
     * @return updated tree result
     */
    suspend operator fun invoke(courseId: String, nodeId: String, node: TreeNodeDomain): DomainResult<TechnologyTreeDomain> {
        return repository.updateNode(courseId, nodeId, node)
    }
} 