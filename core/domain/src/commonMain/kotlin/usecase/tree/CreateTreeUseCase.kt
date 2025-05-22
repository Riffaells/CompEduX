package usecase.tree

import model.DomainResult
import model.tree.TechnologyTreeDomain
import repository.tree.TreeRepository

/**
 * Use case for creating a technology tree
 */
class CreateTreeUseCase(
    private val repository: TreeRepository
) {
    /**
     * Create a new technology tree for a course
     * @param courseId the course identifier
     * @param tree tree data
     * @return created tree result
     */
    suspend operator fun invoke(courseId: String, tree: TechnologyTreeDomain): DomainResult<TechnologyTreeDomain> {
        return repository.createTree(courseId, tree)
    }
} 