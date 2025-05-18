package components

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import logging.Logger
import navigation.rDispatchers
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

/**
 * Store для компонента курсов
 */
interface CourseStore : Store<CourseStore.Intent, CourseStore.State, CourseStore.Label> {
    
    /**
     * Intent - действия, которые могут быть выполнены в этом Store
     */
    sealed interface Intent {
        data object NavigateBack : Intent
        data object NavigateToList : Intent
        data class NavigateToView(val courseId: String) : Intent
        data object NavigateToCreate : Intent
    }
    
    /**
     * State - состояние компонента курсов
     */
    data class State(
        val isLoading: Boolean = false,
        val error: String? = null
    )
    
    /**
     * Label - события, на которые должен реагировать компонент
     */
    sealed interface Label {
        data object NavigateBack : Label
        data object NavigateToList : Label
        data class NavigateToView(val courseId: String) : Label
        data object NavigateToCreate : Label
    }
}

/**
 * Фабрика для создания CourseStore
 */
class CourseStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {
    
    private val logger by instance<Logger>()
    
    fun create(): CourseStore =
        object : CourseStore, Store<CourseStore.Intent, CourseStore.State, CourseStore.Label> by storeFactory.create(
            name = "CourseStore",
            initialState = CourseStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = { ExecutorImpl() },
            reducer = ReducerImpl
        ) {}
        
    // Приватные сообщения для редуктора
    private sealed interface Msg {
        data object Loading : Msg
        data class ErrorOccurred(val error: String) : Msg
        data object ClearError : Msg
    }
    
    private inner class ExecutorImpl : CoroutineExecutor<CourseStore.Intent, Unit, CourseStore.State, Msg, CourseStore.Label>(
        rDispatchers.main
    ) {
        override fun executeAction(action: Unit) {
            // Инициализация, если требуется
        }
        
        override fun executeIntent(intent: CourseStore.Intent) {
            when (intent) {
                CourseStore.Intent.NavigateBack -> {
                    publish(CourseStore.Label.NavigateBack)
                }
                CourseStore.Intent.NavigateToList -> {
                    publish(CourseStore.Label.NavigateToList)
                }
                is CourseStore.Intent.NavigateToView -> {
                    publish(CourseStore.Label.NavigateToView(intent.courseId))
                }
                CourseStore.Intent.NavigateToCreate -> {
                    publish(CourseStore.Label.NavigateToCreate)
                }
            }
        }
    }
    
    private object ReducerImpl : Reducer<CourseStore.State, Msg> {
        override fun CourseStore.State.reduce(msg: Msg): CourseStore.State =
            when (msg) {
                is Msg.Loading -> copy(isLoading = true, error = null)
                is Msg.ErrorOccurred -> copy(isLoading = false, error = msg.error)
                is Msg.ClearError -> copy(error = null)
            }
    }
}
