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
 * Цвета текста в консоли ANSI
 */
object ConsoleColors {
    const val RESET = "\u001B[0m"
    const val BLACK = "\u001B[30m"
    const val RED = "\u001B[31m"
    const val GREEN = "\u001B[32m"
    const val YELLOW = "\u001B[33m"
    const val BLUE = "\u001B[34m"
    const val PURPLE = "\u001B[35m"
    const val CYAN = "\u001B[36m"
    const val WHITE = "\u001B[37m"

    // Яркие цвета
    const val BRIGHT_RED = "\u001B[91m"
    const val BRIGHT_GREEN = "\u001B[92m"
    const val BRIGHT_YELLOW = "\u001B[93m"
    const val BRIGHT_BLUE = "\u001B[94m"
    const val BRIGHT_PURPLE = "\u001B[95m"
    const val BRIGHT_CYAN = "\u001B[96m"
    const val BRIGHT_WHITE = "\u001B[97m"

    // Фоновые цвета
    const val BG_RED = "\u001B[41m"
    const val BG_GREEN = "\u001B[42m"
    const val BG_YELLOW = "\u001B[43m"
    const val BG_BLUE = "\u001B[44m"
    const val BG_PURPLE = "\u001B[45m"
    const val BG_CYAN = "\u001B[46m"
    const val BG_WHITE = "\u001B[47m"

    // Стили текста
    const val BOLD = "\u001B[1m"
    const val UNDERLINE = "\u001B[4m"
    const val ITALIC = "\u001B[3m"
}

/**
 * Кастомная реализация Antilog, которая выводит логи в более визуально различимом формате с цветами
 */
class CustomAntilog : Antilog() {
    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        val tagString = if (tag != null) "[$tag]" else ""

        // Цвета и эмодзи для разных уровней логирования
        val (color, emoji) = when (priority) {
            LogLevel.VERBOSE -> Pair(ConsoleColors.PURPLE, "🟣") // фиолетовый
            LogLevel.DEBUG -> Pair(ConsoleColors.BLUE, "🔵") // синий
            LogLevel.INFO -> Pair(ConsoleColors.GREEN, "🟢") // зеленый
            LogLevel.WARNING -> Pair(ConsoleColors.YELLOW, "🟡") // желтый
            LogLevel.ERROR -> Pair(ConsoleColors.BRIGHT_RED, "🔴") // красный
            LogLevel.ASSERT -> Pair(ConsoleColors.BG_RED + ConsoleColors.WHITE, "⚠️") // красный фон с белым текстом
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
            append("$color$emoji[$timeStr][$priorityChar]")
            if (tag != null) {
                append(" $tagString")
            }
            append(" $message")

            if (throwable != null) {
                append("\n")
                append(throwable.stackTraceToString())
            }

            append(ConsoleColors.RESET) // Сбросить форматирование
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
