package model

/**
 * Generic class for domain operation results
 * Used to transfer results between layers
 */
sealed class DomainResult<out T> {
    /**
     * Successful result with data
     * @param data result data
     */
    data class Success<T>(val data: T) : DomainResult<T>()

    /**
     * Operation error
     * @param error error object
     */
    data class Error(val error: DomainError) : DomainResult<Nothing>()

    /**
     * Loading state
     */
    data object Loading : DomainResult<Nothing>()

    /**
     * Check if result is successful
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Check if result is an error
     */
    val isError: Boolean get() = this is Error

    /**
     * Check if result is in loading state
     */
    val isLoading: Boolean get() = this is Loading

    /**
     * Get data from successful result or null
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Get error from error result or null
     */
    fun errorOrNull(): DomainError? = when (this) {
        is Error -> error
        else -> null
    }

    /**
     * Transform data of a successful result
     */
    fun <R> map(transform: (T) -> R): DomainResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> Loading
        }
    }

    /**
     * Execute an action depending on result type
     */
    inline fun onSuccess(action: (T) -> Unit): DomainResult<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Execute an action in case of error
     */
    inline fun onError(action: (DomainError) -> Unit): DomainResult<T> {
        if (this is Error) action(error)
        return this
    }

    /**
     * Execute an action in loading state
     */
    inline fun onLoading(action: () -> Unit): DomainResult<T> {
        if (this is Loading) action()
        return this
    }
}
