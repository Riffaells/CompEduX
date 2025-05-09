package usecase.course


/**
 * Container for all course-related use cases
 * Provides access to all course use cases through a single object
 */
data class CourseUseCases(
    val getCourse: GetCourseUseCase,
    val getCourses: GetCoursesUseCase,
    val createCourse: CreateCourseUseCase,
    val updateCourse: UpdateCourseUseCase,
    val deleteCourse: DeleteCourseUseCase
)
