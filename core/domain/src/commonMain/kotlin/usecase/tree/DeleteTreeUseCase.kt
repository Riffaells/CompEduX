package usecase.tree

import model.DomainResult
import repository.tree.TreeRepository

/**
 * Use case for deleting a technology tree
 */
class DeleteTreeUseCase(
    private val repository: TreeRepository
) {
    /**
     * Delete a technology tree for a course
     * @param courseId the course identifier
     * @return operation result
     */
    suspend operator fun invoke(courseId: String): DomainResult<Unit> {
        return repository.deleteTree(courseId)
    }
} 