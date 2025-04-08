package utils

import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Class for safe execution of navigation operations in Decompose.
 *
 * NavigationExecutor helps solve threading issues when working with navigation,
 * ensuring that all navigation operations are executed on the main thread, and
 * providing an error handling mechanism.
 *
 * The class offers the following features:
 * - Synchronous execution of navigation operations when called from UI
 * - Asynchronous execution of long-running operations followed by navigation
 * - Navigation error handling
 * - Logging of navigation operations
 *
 * @param C The type of navigation configuration used in StackNavigation
 * @property navigation StackNavigation instance for executing navigation operations
 * @property scope CoroutineScope bound to the component's lifecycle
 * @property mainDispatcher Dispatcher for executing operations on the main thread
 * @property logger Function for logging operations and errors
 */
@OptIn(DelicateDecomposeApi::class)
class NavigationExecutor<C : Any>(
    private val navigation: StackNavigation<C>,
    private val scope: CoroutineScope,
    private val mainDispatcher: kotlinx.coroutines.CoroutineDispatcher,
    private val logger: (String) -> Unit = { println(it) }
) {
    /**
     * Navigate to the specified configuration (equivalent to bringToFront).
     *
     * Executes synchronously in the calling thread, so it should be called from the main thread.
     *
     * @param config Configuration to navigate to
     */
    fun navigateTo(config: C) {
        try {
            navigation.bringToFront(config)
        } catch (e: Exception) {
            logger("Error navigating to $config: ${e.message}")
        }
    }

    /**
     * Add a new configuration to the stack (push).
     *
     * Executes synchronously in the calling thread, so it should be called from the main thread.
     *
     * @param config Configuration to add to the stack
     */
    fun push(config: C) {
        try {
            navigation.push(config)
        } catch (e: Exception) {
            logger("Error pushing $config: ${e.message}")
        }
    }

    /**
     * Navigate back (pop).
     *
     * Executes synchronously in the calling thread, so it should be called from the main thread.
     */
    fun pop() {
        try {
            navigation.pop()
        } catch (e: Exception) {
            logger("Error navigating back: ${e.message}")
        }
    }

    /**
     * Replace the current configuration.
     *
     * Executes synchronously in the calling thread, so it should be called from the main thread.
     *
     * @param config Configuration to replace the current one with
     */
    fun replace(config: C) {
        try {
            navigation.replaceCurrent(config)
        } catch (e: Exception) {
            logger("Error replacing with $config: ${e.message}")
        }
    }

    /**
     * Execute an arbitrary navigation operation.
     *
     * Executes synchronously in the calling thread, so it should be called from the main thread.
     *
     * @param block Extension function for StackNavigation that performs the navigation operation
     */
    fun execute(block: StackNavigation<C>.() -> Unit) {
        try {
            navigation.block()
        } catch (e: Exception) {
            logger("Error executing navigation: ${e.message}")
        }
    }

    /**
     * Execute an asynchronous operation, then navigate based on the results.
     *
     * The method launches a background operation in the IO dispatcher, then performs
     * result processing on the main thread for safe navigation.
     *
     * @param T Type of the background operation result
     * @param backgroundOperation Background operation to execute asynchronously
     * @param onSuccess Function to handle the successful operation result, executed on the main thread
     * @param onError Function to handle errors, executed on the main thread
     */
    fun <T> executeAsync(
        backgroundOperation: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: (Exception) -> Unit = { logger("Error executing async operation: ${it.message}") }
    ) {
        scope.launch {
            try {
                // Execute the background operation in the IO dispatcher
                val result = withContext(rDispatchers.io) {
                    backgroundOperation()
                }

                // Switch to the main thread for navigation
                withContext(mainDispatcher) {
                    onSuccess(result)
                }
            } catch (e: Exception) {
                // Switch to the main thread for error handling
                withContext(mainDispatcher) {
                    onError(e)
                }
            }
        }
    }
}
