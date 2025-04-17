package navigation

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Интерфейс для предоставления диспетчеров корутин для разных платформ.
 * Это абстракция над Dispatchers из kotlinx.coroutines, которая позволяет
 * использовать правильные диспетчеры на каждой платформе.
 */
interface RDispatchers {
    /**
     * Диспетчер для UI-операций. Эквивалент Dispatchers.Main.
     * На разных платформах имеет разную реализацию:
     * - Android: Dispatchers.Main.immediate
     * - JVM: Dispatchers.Default (т.к. нет выделенного UI-потока)
     * - iOS: Dispatchers из NativeCoroutines
     * - JS: Dispatchers.Default
     */
    val main: CoroutineDispatcher

    /**
     * Диспетчер для IO-операций. Эквивалент Dispatchers.IO.
     */
    val io: CoroutineDispatcher

    /**
     * Диспетчер без ограничений. Эквивалент Dispatchers.Unconfined.
     */
    val unconfined: CoroutineDispatcher

    /**
     * Диспетчер по умолчанию. Эквивалент Dispatchers.Default.
     */
    val default: CoroutineDispatcher
}

/**
 * Получение платформо-специфичной реализации RDispatchers.
 */
expect val rDispatchers: RDispatchers
