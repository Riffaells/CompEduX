package usecase.tree

import model.DomainResult
import model.tree.TechnologyTreeDomain
import model.tree.TreeConnectionDomain
import repository.tree.TreeRepository

/**
 * Use case for updating a connection in a technology tree
 */
class UpdateConnectionUseCase(
    private val repository: TreeRepository
) {
    /**
     * Update a connection in the technology tree
     * @param courseId the course identifier
     * @param connectionId the connection identifier
     * @param connection updated connection data
     * @return updated tree result
     */
    suspend operator fun invoke(courseId: String, connectionId: String, connection: TreeConnectionDomain): DomainResult<TechnologyTreeDomain> {
        return repository.updateConnection(courseId, connectionId, connection)
    }
} 