package utils

/**
 * String extension functions for common operations
 */

/**
 * Checks if string is a valid email address
 * @return true if string is a valid email, false otherwise
 */
fun String.isValidEmail(): Boolean {
    val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    return this.matches(emailRegex)
}

/**
 * Checks if string is a valid username
 * Only English letters and underscore are allowed
 * @return true if string is a valid username, false otherwise
 */
fun String.isValidUsername(): Boolean {
    val usernameRegex = Regex("^[A-Za-z_]+$")
    return this.matches(usernameRegex)
}

/**
 * Truncates string to specified length and adds ellipsis if needed
 * @param maxLength maximum allowed length
 * @return truncated string
 */
fun String.truncate(maxLength: Int): String {
    return if (this.length <= maxLength) {
        this
    } else {
        this.substring(0, maxLength - 3) + "..."
    }
}

/**
 * Number formatting utilities
 */
object NumberFormatUtils {
    /**
     * Formats integer with digit separators
     * @param number integer to format
     * @param separator digit separator character (space by default)
     * @return formatted string
     */
    fun formatNumber(number: Int, separator: Char = ' '): String {
        return number.toString().reversed().chunked(3).joinToString(separator.toString()).reversed()
    }

    /**
     * Formats long integer with digit separators
     * @param number long integer to format
     * @param separator digit separator character (space by default)
     * @return formatted string
     */
    fun formatNumber(number: Long, separator: Char = ' '): String {
        return number.toString().reversed().chunked(3).joinToString(separator.toString()).reversed()
    }
}

/**
 * Text pluralization utilities
 */
object TextUtils {
    /**
     * Returns correct word form based on count (for Slavic languages)
     * @param count number value
     * @param one form for 1 (e.g. "day")
     * @param few form for 2-4 (e.g. "days" for values like 2, 3, 4)
     * @param many form for 5-20 (e.g. "days" for values like 5, 6, ..., 20)
     * @return correct word form
     */
    fun pluralize(count: Int, one: String, few: String, many: String): String {
        val mod100 = count % 100
        val mod10 = count % 10

        return when {
            mod100 in 11..19 -> many
            mod10 == 1 -> one
            mod10 in 2..4 -> few
            else -> many
        }
    }
}
