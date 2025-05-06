package component.app.course.list

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.course.list.store.CoursesListStore
import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.DI

/**
 * Component for displaying course list
 */
interface CoursesListComponent {
    /**
     * Course list state
     */
    val state: StateFlow<CoursesListStore.State>

    /**
     * Handle UI intents
     * @param intent UI intent
     */
    fun accept(intent: CoursesListStore.Intent)

    /**
     * Handle back button click
     */
    fun onBack()

    companion object {
        /**
         * Create component
         */
        fun create(
            componentContext: ComponentContext,
            di: DI,
            storeFactory: StoreFactory,
            onBack: () -> Unit,
            onCourseSelected: (String) -> Unit
        ): CoursesListComponent =
            DefaultCoursesListComponent(
                componentContext = componentContext,
                di = di,
                storeFactory = storeFactory,
                onBack = onBack,
                onCourseSelected = onCourseSelected
            )
    }
}

/**
 * Default implementation of courses list component
 */
class DefaultCoursesListComponent(
    componentContext: ComponentContext,
    private val di: DI,
    private val storeFactory: StoreFactory,
    private val onBack: () -> Unit,
    private val onCourseSelected: (String) -> Unit
) : CoursesListComponent, ComponentContext by componentContext {

    /**
     * Store for managing component state
     */
    private val store = instanceKeeper.getStore {
        CoursesListStore.create(
            di = di,
            storeFactory = storeFactory
        )
    }

    /**
     * Component state
     */
    override val state: StateFlow<CoursesListStore.State> = store.stateFlow

    /**
     * Handle intents
     */
    override fun accept(intent: CoursesListStore.Intent) {
        when (intent) {
            is CoursesListStore.Intent.CourseClick -> onCourseSelected(intent.courseId)
            else -> store.accept(intent)
        }
    }

    /**
     * Handle back button click
     */
    override fun onBack() {
        onBack.invoke()
    }
}
