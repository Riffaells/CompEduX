package navigation

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Wasm-специфичная реализация RDispatchers.
 * В Wasm нет отдельного Main диспетчера, поэтому используем Default.
 */
actual val rDispatchers: RDispatchers = object : RDispatchers {
    // В Wasm нет отдельного Main диспетчера, поэтому используем Default
    override val main: CoroutineDispatcher = Dispatchers.Default

    // В Wasm нет отдельного IO диспетчера, поэтому используем Default
    override val io: CoroutineDispatcher = Dispatchers.Default

    override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
    override val default: CoroutineDispatcher = Dispatchers.Default


}
