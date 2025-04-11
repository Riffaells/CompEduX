package logging

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Custom Antilog implementation with colored console output
 */
class CompEduXAntilog : Antilog() {
    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        // Get log level color
        val color = when (priority) {
            LogLevel.VERBOSE -> ConsoleColors.CYAN
            LogLevel.DEBUG -> ConsoleColors.BLUE
            LogLevel.INFO -> ConsoleColors.GREEN
            LogLevel.WARNING -> ConsoleColors.YELLOW
            LogLevel.ERROR -> ConsoleColors.RED
            LogLevel.ASSERT -> ConsoleColors.PURPLE
        }

        // Emoji for better visual distinction
        val emoji = when (priority) {
            LogLevel.VERBOSE -> "💬"
            LogLevel.DEBUG -> "🔍"
            LogLevel.INFO -> "ℹ️"
            LogLevel.WARNING -> "⚠️"
            LogLevel.ERROR -> "❌"
            LogLevel.ASSERT -> "🚨"
        }

        // Add tag if present
        val tagString = tag?.let { "[$it]" } ?: ""

        // Priority letter for short display
        val priorityChar = when (priority) {
            LogLevel.VERBOSE -> "V"
            LogLevel.DEBUG -> "D"
            LogLevel.INFO -> "I"
            LogLevel.WARNING -> "W"
            LogLevel.ERROR -> "E"
            LogLevel.ASSERT -> "A"
        }

        // Get current time
        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        // Format time without String.format (not available on all platforms)
        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')
        val second = localDateTime.second.toString().padStart(2, '0')
        val millis = (now.toEpochMilliseconds() % 1000).toString().padStart(3, '0')
        val timeStr = "$hour:$minute:$second.$millis"

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

            append(ConsoleColors.RESET) // Reset formatting
        }

        // Just use println for all platforms
        println(output)
    }
}
