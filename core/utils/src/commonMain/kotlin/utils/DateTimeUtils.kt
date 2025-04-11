package utils

import kotlinx.datetime.*

/**
 * Extensions for working with date and time
 */

/**
 * Formats date to string in "dd.MM.yyyy" format
 * @return formatted date string
 */
fun LocalDate.format(): String {
    val day = this.dayOfMonth.toString().padStart(2, '0')
    val month = this.monthNumber.toString().padStart(2, '0')
    val year = this.year
    return "$day.$month.$year"
}

/**
 * Formats time to string in "HH:mm:ss" format
 * @return formatted time string
 */
fun LocalTime.format(): String {
    val hour = this.hour.toString().padStart(2, '0')
    val minute = this.minute.toString().padStart(2, '0')
    val second = this.second.toString().padStart(2, '0')
    return "$hour:$minute:$second"
}

/**
 * Formats date and time to string in "dd.MM.yyyy HH:mm:ss" format
 * @return formatted date and time string
 */
fun LocalDateTime.format(): String {
    return "${this.date.format()} ${this.time.format()}"
}

/**
 * Gets current date and time
 * @return LocalDateTime object with current date and time
 */
fun Clock.System.nowDateTime(): LocalDateTime {
    return now().toLocalDateTime(TimeZone.currentSystemDefault())
}

/**
 * Gets current local date
 * @return LocalDate object with current date
 */
fun Clock.System.nowDate(): LocalDate {
    return now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}

/**
 * Gets current local time
 * @return LocalTime object with current time
 */
fun Clock.System.nowTime(): LocalTime {
    return now().toLocalDateTime(TimeZone.currentSystemDefault()).time
}

/**
 * Converts milliseconds to time string in "HH:mm:ss.SSS" format
 * @return formatted time string
 */
fun Long.formatAsTime(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val millisRemainder = this % 1000

    return buildString {
        append(hours.toString().padStart(2, '0'))
        append(':')
        append(minutes.toString().padStart(2, '0'))
        append(':')
        append(seconds.toString().padStart(2, '0'))
        append('.')
        append(millisRemainder.toString().padStart(3, '0'))
    }
}
