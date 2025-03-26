package di

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

/**
 * Интерфейс логирования
 */
interface Logger {
    fun d(message: String, throwable: Throwable? = null, tag: String? = null)
    fun i(message: String, throwable: Throwable? = null, tag: String? = null)
    fun w(message: String, throwable: Throwable? = null, tag: String? = null)
    fun e(message: String, throwable: Throwable? = null, tag: String? = null)
    fun v(message: String, throwable: Throwable? = null, tag: String? = null)
}

/**
 * Кастомная реализация Antilog, которая выводит логи в более чистом формате
 */
class CustomAntilog : Antilog() {
    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        val tagString = if (tag != null) "[$tag]" else ""

        // Эмодзи для разных уровней логирования
        val emoji = when (priority) {
            LogLevel.VERBOSE -> "🟣" // фиолетовый круг
            LogLevel.DEBUG -> "🔵" // синий круг
            LogLevel.INFO -> "🟢" // зеленый круг
            LogLevel.WARNING -> "🟡" // желтый круг
            LogLevel.ERROR -> "🔴" // красный круг
            LogLevel.ASSERT -> "⚠️" // предупреждение
        }

        val priorityChar = when (priority) {
            LogLevel.VERBOSE -> "V"
            LogLevel.DEBUG -> "D"
            LogLevel.INFO -> "I"
            LogLevel.WARNING -> "W"
            LogLevel.ERROR -> "E"
            LogLevel.ASSERT -> "A"
        }

        // Получаем текущее время
        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val timeStr = String.format("%02d:%02d:%02d.%03d",
            localDateTime.hour, localDateTime.minute, localDateTime.second, now.toEpochMilliseconds() % 1000)

        val output = buildString {
            append("$emoji[$timeStr][$priorityChar] ")
            if (tag != null) {
                append("$tagString ")
            }
            append(message ?: "")

            if (throwable != null) {
                append("\n")
                append(throwable.stackTraceToString())
            }
        }

        println(output)
    }
}

/**
 * Реализация логирования через Napier с префиксами для лучшей визуальной отличимости
 */
class NapierLogger(antilog: Antilog) : Logger {
    init {
        Napier.base(antilog)
    }

    override fun d(message: String, throwable: Throwable?, tag: String?) {
        Napier.d(message, throwable, tag)
    }

    override fun i(message: String, throwable: Throwable?, tag: String?) {
        Napier.i(message, throwable, tag)
    }

    override fun w(message: String, throwable: Throwable?, tag: String?) {
        Napier.w(message, throwable, tag)
    }

    override fun e(message: String, throwable: Throwable?, tag: String?) {
        Napier.e(message, throwable, tag)
    }

    override fun v(message: String, throwable: Throwable?, tag: String?) {
        Napier.v(message, throwable, tag)
    }
}

/**
 * Модуль логирования для DI
 */
val loggingModule = DI.Module("loggingModule") {
    // Настраиваем бэкенд для логирования
    bind<Antilog>() with singleton { CustomAntilog() }

    // Создаем логгер
    bind<Logger>() with singleton { NapierLogger(instance()) }
}
