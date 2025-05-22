package usecase.tree

import model.DomainResult
import model.tree.TechnologyTreeDomain
import repository.tree.TreeRepository

/**
 * Use case for removing a connection from a technology tree
 */
class RemoveConnectionUseCase(
    private val repository: TreeRepository
) {
    /**
     * Remove a connection from the technology tree
     * @param courseId the course identifier
     * @param connectionId the connection identifier
     * @return updated tree result
     */
    suspend operator fun invoke(courseId: String, connectionId: String): DomainResult<TechnologyTreeDomain> {
        return repository.removeConnection(courseId, connectionId)
    }
} 