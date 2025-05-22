package usecase.tree

import model.DomainResult
import model.tree.TechnologyTreeDomain
import model.tree.TreeNodeDomain
import repository.tree.TreeRepository

/**
 * Use case for adding a node to a technology tree
 */
class AddNodeUseCase(
    private val repository: TreeRepository
) {
    /**
     * Add a new node to the technology tree
     * @param courseId the course identifier
     * @param node new node data
     * @return updated tree result
     */
    suspend operator fun invoke(courseId: String, node: TreeNodeDomain): DomainResult<TechnologyTreeDomain> {
        return repository.addNode(courseId, node)
    }
} 