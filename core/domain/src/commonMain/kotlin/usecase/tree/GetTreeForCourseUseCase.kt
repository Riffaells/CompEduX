package usecase.tree

import model.DomainResult
import model.tree.TechnologyTreeDomain
import repository.tree.TreeRepository

/**
 * Use case for getting a technology tree for a course
 */
class GetTreeForCourseUseCase(
    private val repository: TreeRepository
) {
    /**
     * Get a technology tree for a specific course
     * @param courseId the course identifier
     * @return tree data result
     */
    suspend operator fun invoke(courseId: String): DomainResult<TechnologyTreeDomain> {
        return repository.getTreeForCourse(courseId)
    }
} 