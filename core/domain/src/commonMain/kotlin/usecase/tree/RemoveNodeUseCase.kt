package usecase.tree

import model.DomainResult
import model.tree.TechnologyTreeDomain
import repository.tree.TreeRepository

/**
 * Use case for removing a node from a technology tree
 */
class RemoveNodeUseCase(
    private val repository: TreeRepository
) {
    /**
     * Remove a node from the technology tree
     * @param courseId the course identifier
     * @param nodeId the node identifier
     * @return updated tree result
     */
    suspend operator fun invoke(courseId: String, nodeId: String): DomainResult<TechnologyTreeDomain> {
        return repository.removeNode(courseId, nodeId)
    }
}