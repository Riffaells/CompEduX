package usecase.tree

import model.DomainResult
import model.tree.TechnologyTreeDomain
import repository.tree.TreeRepository

/**
 * Use case for updating a technology tree
 */
class UpdateTreeUseCase(
    private val repository: TreeRepository
) {
    /**
     * Update a technology tree for a course
     * @param courseId the course identifier
     * @param tree updated tree data
     * @return updated tree result
     */
    suspend operator fun invoke(courseId: String, tree: TechnologyTreeDomain): DomainResult<TechnologyTreeDomain> {
        return repository.updateTree(courseId, tree)
    }
} 