//package repository.lessons
//
//
//import model.DomainResult
//import model.lesson.LessonDomain
//import model.lesson.LessonListDomain
//import model.lesson.LessonQueryParams
//
///**
// * Repository for lesson operations
// * The lesson represents a structure/framework for educational content, not an active entity
// */
//interface LessonRepository {
//    /**
//     * Get a lesson by its identifier
//     * @param lessonId the lesson identifier
//     * @return operation result with lesson data
//     */
//    suspend fun getLesson(lessonId: String): DomainResult<LessonDomain>
//
//    /**
//     * Get a list of lessons with filtering and pagination
//     * @param params query parameters
//     * @return operation result with a paginated lesson list
//     */
//    suspend fun getLessons(params: LessonQueryParams = LessonQueryParams()): DomainResult<LessonListDomain>
//
//    /**
//     * Get lessons by author
//     * @param authorId author identifier
//     * @param page page number (1-based)
//     * @param pageSize number of items per page
//     * @return operation result with a paginated lesson list
//     */
//    suspend fun getLessonsByAuthor(
//        authorId: String,
//        page: Int = 1,
//        pageSize: Int = 20
//    ): DomainResult<LessonListDomain>
//
//    /**
//     * Get popular lessons
//     * @param limit maximum number of lessons to return
//     * @return operation result with a list of lessons
//     */
//    suspend fun getPopularLessons(limit: Int = 10): DomainResult<List<LessonDomain>>
//
//    /**
//     * Create a new lesson
//     * @param lesson lesson data
//     * @return operation result with the created lesson
//     */
//    suspend fun createLesson(lesson: LessonDomain): DomainResult<LessonDomain>
//
//    /**
//     * Update a lesson
//     * @param lessonId the lesson identifier
//     * @param lesson updated lesson data
//     * @return operation result with the updated lesson
//     */
//    suspend fun updateLesson(lessonId: String, lesson: LessonDomain): DomainResult<LessonDomain>
//
//    /**
//     * Delete a lesson
//     * @param lessonId the lesson identifier
//     * @return operation result
//     */
//    suspend fun deleteLesson(lessonId: String): DomainResult<Unit>
//}
