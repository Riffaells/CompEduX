package components.content

class ContentViewStore {
    data class State(
        val isLoading: Boolean = true,
        val title: String = "",
        val content: String = "",
        val error: String? = null
    )
} 