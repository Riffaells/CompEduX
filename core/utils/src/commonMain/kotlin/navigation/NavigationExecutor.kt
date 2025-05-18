package navigation

import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logging.Logger

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
 * @property logger Logger instance for logging operations and errors
 * @property tag Optional tag for log messages, defaults to "Navigation"
 */
@OptIn(DelicateDecomposeApi::class)
class NavigationExecutor<C : Any>(
    private val navigation: StackNavigation<C>,
    private val scope: CoroutineScope,
    private val mainDispatcher: CoroutineDispatcher,
    private val logger: Logger,
    private val tag: String = "Navigation"
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
            logger.d("Navigating to $config", tag = tag)
            navigation.bringToFront(config)
        } catch (e: Exception) {
            logger.e("Error navigating to $config: ${e.message}", e, tag)
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
            logger.d("Pushing $config to stack", tag = tag)
            navigation.push(config)
        } catch (e: Exception) {
            logger.e("Error pushing $config: ${e.message}", e, tag)
        }
    }

    /**
     * Navigate back (pop).
     *
     * Executes synchronously in the calling thread, so it should be called from the main thread.
     */
    fun pop() {
        try {
            logger.d("Navigating back (pop)", tag = tag)
            navigation.pop()
        } catch (e: Exception) {
            logger.e("Error navigating back: ${e.message}", e, tag)
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
            logger.d("Replacing current with $config", tag = tag)
            navigation.replaceCurrent(config)
        } catch (e: Exception) {
            logger.e("Error replacing with $config: ${e.message}", e, tag)
        }
    }


    /**
     * Replace the all configuration.
     *
     * Executes synchronously in the calling thread, so it should be called from the main thread.
     *
     * @param config Configuration to replace the current one with
     */
    fun replaceAll(config: C) {
        try {
            logger.d("Replacing current with $config", tag = tag)
            navigation.replaceAll(config)
        } catch (e: Exception) {
            logger.e("Error replacing with $config: ${e.message}", e, tag)
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
            logger.d("Executing custom navigation operation", tag = tag)
            navigation.block()
        } catch (e: Exception) {
            logger.e("Error executing navigation: ${e.message}", e, tag)
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
        onError: (Exception) -> Unit = {
            logger.e("Error executing async operation: ${it.message}", it, tag)
        }
    ) {
        scope.launch {
            try {
                logger.d("Starting async operation", tag = tag)
                // Execute the background operation in the IO dispatcher
                val result = withContext(rDispatchers.io) {
                    backgroundOperation()
                }

                // Switch to the main thread for navigation
                withContext(mainDispatcher) {
                    logger.d("Async operation completed successfully", tag = tag)
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
