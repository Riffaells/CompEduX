package di

import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.bindSingleton

/**
 * Модуль сериализации
 */
val serializationModule = DI.Module("serializationModule") {
    bindSingleton {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            prettyPrint = true
            coerceInputValues = true
        }
    }
}
