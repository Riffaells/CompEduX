package components.content

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ContentViewComponent(
    private val componentContext: ComponentContext,
    private val contentId: String,
    private val onBack: () -> Unit
) : ComponentContext by componentContext {

    private val _state = MutableStateFlow(ContentViewStore.State())
    val state: StateFlow<ContentViewStore.State> = _state.asStateFlow()

    init {
        lifecycle.doOnCreate {
            loadContent()
        }
    }

    private fun loadContent() {
        // Здесь будет загрузка контента по ID
        _state.value = _state.value.copy(
            isLoading = false,
            title = "Название $contentId"
        )
    }

    fun onBack() {
        onBack.invoke()
    }
} 