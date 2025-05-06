package model.course

import kotlinx.serialization.Serializable

/**
 * Paginated list of courses
 */
@Serializable
data class CourseListDomain(
    val items: List<CourseDomain> = emptyList(),
    val total: Int = 0,
    val page: Int = 0,
    val size: Int = 20,
    val pages: Int = 0
) {
    /**
     * Проверка, пуст ли список
     */
    val isEmpty: Boolean
        get() = items.isEmpty()

    /**
     * Проверка, является ли эта страница первой
     */
    val isFirst: Boolean
        get() = page == 0

    /**
     * Проверка, является ли эта страница последней
     */
    val isLast: Boolean
        get() = page >= pages - 1 || isEmpty

    companion object {
        /**
         * Create an empty course list
         */
        fun empty(): CourseListDomain {
            return CourseListDomain(
                items = emptyList(),
                total = 0,
                page = 0,
                size = 0,
                pages = 0
            )
        }

        /**
         * Create a list from courses without pagination info
         */
        fun fromList(courses: List<CourseDomain>): CourseListDomain {
            val nonEmptyList = courses.isNotEmpty()
            return CourseListDomain(
                items = courses,
                total = courses.size,
                page = 0,
                size = courses.size,
                pages = if (nonEmptyList) 1 else 0
            )
        }
    }
}
