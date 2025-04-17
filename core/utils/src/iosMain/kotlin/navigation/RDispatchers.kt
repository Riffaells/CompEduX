package navigation

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * iOS-специфичная реализация RDispatchers.
 * Использует Dispatchers.Main для UI-операций на iOS.
 */
actual val rDispatchers: RDispatchers = object : RDispatchers {
    // На iOS Dispatchers.Main доступен через kotlinx-coroutines-core
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.Default // iOS не имеет отдельного IO диспетчера
    override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
    override val default: CoroutineDispatcher = Dispatchers.Default
}
