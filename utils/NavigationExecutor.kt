package utils

import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.mainthread.MainThreadWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Класс для безопасного выполнения операций навигации
 * Гарантирует, что все операции выполняются в главном потоке и
 * обеспечивает обработку ошибок
 */
class NavigationExecutor<C : Any>(
    private val navigation: StackNavigation<C>,
    private val scope: CoroutineScope,
    private val mainDispatcher: kotlinx.coroutines.CoroutineDispatcher,
    private val logger: (String) -> Unit = { println(it) }
) {
    // Используем MainThreadWorker для гарантированного выполнения в главном потоке Decompose
    private val mainThreadWorker = MainThreadWorker()

    /**
     * Переход к указанной конфигурации (аналог bringToFront)
     */
    fun navigateTo(config: C) {
        // Выполняем в главном потоке через MainThreadWorker
        mainThreadWorker.submitTask {
            try {
                navigation.bringToFront(config)
            } catch (e: Exception) {
                logger("Error navigating to $config: ${e.message}")
            }
        }
    }

    /**
     * Добавление новой конфигурации в стек (push)
     */
    fun push(config: C) {
        // Выполняем в главном потоке через MainThreadWorker
        mainThreadWorker.submitTask {
            try {
                navigation.push(config)
            } catch (e: Exception) {
                logger("Error pushing $config: ${e.message}")
            }
        }
    }

    /**
     * Возврат назад (pop)
     */
    fun pop() {
        // Выполняем в главном потоке через MainThreadWorker
        mainThreadWorker.submitTask {
            try {
                navigation.pop()
            } catch (e: Exception) {
                logger("Error navigating back: ${e.message}")
            }
        }
    }

    /**
     * Замена текущей конфигурации (replace)
     */
    fun replace(config: C) {
        // Выполняем в главном потоке через MainThreadWorker
        mainThreadWorker.submitTask {
            try {
                navigation.replaceCurrent(config)
            } catch (e: Exception) {
                logger("Error replacing with $config: ${e.message}")
            }
        }
    }

    /**
     * Выполнение произвольной операции навигации в main потоке
     */
    fun execute(block: StackNavigation<C>.() -> Unit) {
        // Выполняем в главном потоке через MainThreadWorker
        mainThreadWorker.submitTask {
            try {
                navigation.block()
            } catch (e: Exception) {
                logger("Error executing navigation: ${e.message}")
            }
        }
    }

    /**
     * Выполнение асинхронной операции, а затем навигация
     */
    fun <T> executeAsync(
        backgroundOperation: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: (Exception) -> Unit = { logger("Error executing async operation: ${it.message}") }
    ) {
        scope.launch(mainDispatcher) {
            try {
                val result = withContext(scope.coroutineContext) {
                    backgroundOperation()
                }
                // Выполняем коллбэк в главном потоке через MainThreadWorker
                mainThreadWorker.submitTask {
                    onSuccess(result)
                }
            } catch (e: Exception) {
                // Выполняем коллбэк для ошибки в главном потоке через MainThreadWorker
                mainThreadWorker.submitTask {
                    onError(e)
                }
            }
        }
    }
}
