package usecase.tree

import model.DomainResult
import model.tree.TechnologyTreeDomain
import model.tree.TreeConnectionDomain
import repository.tree.TreeRepository

/**
 * Use case for adding a connection to a technology tree
 */
class AddConnectionUseCase(
    private val repository: TreeRepository
) {
    /**
     * Add a new connection to the technology tree
     * @param courseId the course identifier
     * @param connection new connection data
     * @return updated tree result
     */
    suspend operator fun invoke(courseId: String, connection: TreeConnectionDomain): DomainResult<TechnologyTreeDomain> {
        return repository.addConnection(courseId, connection)
    }
} 