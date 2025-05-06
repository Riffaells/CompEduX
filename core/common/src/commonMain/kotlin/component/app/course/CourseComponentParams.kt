package component.app.course

import com.arkivanov.decompose.ComponentContext

/**
 * Parameters for creating the course component.
 *
 * @property componentContext Decompose component context.
 * @property onBack Callback for returning from the course component.
 */
data class CourseComponentParams(
    val componentContext: ComponentContext,
    val onBack: () -> Unit
)

/**
 * Parameters for creating the course detail component.
 *
 * @property componentContext Decompose component context.
 * @property courseId ID of the course to display.
 * @property onBack Callback for returning.
 * @property onEdit Callback for editing a course.
 * @property onModuleSelected Callback for when a module is selected.
 */
data class CourseDetailComponentParams(
    val componentContext: ComponentContext,
    val courseId: String,
    val onBack: () -> Unit,
    val onEdit: (String) -> Unit,
    val onModuleSelected: (String, String) -> Unit
)
