package component.app.course.course

import com.arkivanov.decompose.ComponentContext

/**
 * Parameters for creating the course component.
 *
 * @property componentContext Decompose component context.
 * @property courseId ID of the course to display or edit.
 * @property onBack Callback for returning from the course component.
 */
data class CourseComponentParams(
    val componentContext: ComponentContext,
    val courseId: String?,
    val onBack: () -> Unit
) 