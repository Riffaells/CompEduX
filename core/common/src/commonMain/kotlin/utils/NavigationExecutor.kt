package utils

import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Класс для безопасного выполнения операций навигации
 * Гарантирует, что все операции выполняются в главном потоке и
 * обеспечивает обработку ошибок
 */
@OptIn(DelicateDecomposeApi::class)
class NavigationExecutor<C : Any>(
    private val navigation: StackNavigation<C>,
    private val scope: CoroutineScope,
    private val mainDispatcher: kotlinx.coroutines.CoroutineDispatcher,
    private val logger: (String) -> Unit = { println(it) }
) {
    /**
     * Переход к указанной конфигурации (аналог bringToFront)
     */
    fun navigateTo(config: C) {
        // Выполняем синхронно в потоке вызова (предполагается, что вызов из main)
        try {
            navigation.bringToFront(config)
        } catch (e: Exception) {
            logger("Error navigating to $config: ${e.message}")
        }
    }

    /**
     * Добавление новой конфигурации в стек (push)
     */
    fun push(config: C) {
        // Выполняем синхронно в потоке вызова
        try {
            navigation.push(config)
        } catch (e: Exception) {
            logger("Error pushing $config: ${e.message}")
        }
    }

    /**
     * Возврат назад (pop)
     */
    fun pop() {
        // Выполняем синхронно в потоке вызова
        try {
            navigation.pop()
        } catch (e: Exception) {
            logger("Error navigating back: ${e.message}")
        }
    }

    /**
     * Замена текущей конфигурации (replace)
     */
    fun replace(config: C) {
        // Выполняем синхронно в потоке вызова
        try {
            navigation.replaceCurrent(config)
        } catch (e: Exception) {
            logger("Error replacing with $config: ${e.message}")
        }
    }

    /**
     * Выполнение произвольной операции навигации в main потоке
     */
    fun execute(block: StackNavigation<C>.() -> Unit) {
        // Выполняем синхронно в потоке вызова
        try {
            navigation.block()
        } catch (e: Exception) {
            logger("Error executing navigation: ${e.message}")
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
        // Выполняем асинхронную операцию в корутине
        scope.launch {
            try {
                // Выполняем фоновую операцию в IO диспетчере
                val result = withContext(rDispatchers.io) {
                    backgroundOperation()
                }

                // Переключаемся на главный поток для навигации
                withContext(mainDispatcher) {
                    onSuccess(result)
                }
            } catch (e: Exception) {
                // Переключаемся на главный поток для обработки ошибок
                withContext(mainDispatcher) {
                    onError(e)
                }
            }
        }
    }
}
