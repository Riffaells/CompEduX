package util

/**
 * Utility class for handling localized text
 * Provides type-safe access to localized content
 */
object LocalizationUtil {
    /**
     * Get localized text from a LocalizedText object
     *
     * @param text The localized text object
     * @param locale The preferred locale (default is "ru")
     * @return The localized string
     */
    fun getLocalizedText(text: LocalizedText, locale: String = "ru"): String {
        return text.getForLocale(locale)
    }

    /**
     * Get localized text with fallback to default value
     *
     * @param text The localized text object, or null
     * @param default The default value if text is null
     * @param locale The preferred locale (default is "ru")
     * @return The localized string or default value
     */
    fun getLocalizedTextOrDefault(text: LocalizedText?, default: String, locale: String = "ru"): String {
        return text?.getForLocale(locale) ?: default
    }
}

/**
 * Interface for localized text content
 */
interface LocalizedText {
    /**
     * Get text for specific locale
     *
     * @param locale The locale code ("ru", "en", etc)
     * @return The localized text for the requested locale
     */
    fun getForLocale(locale: String): String
}

/**
 * Simple implementation of localized text
 *
 * @property content Map of locale codes to text values
 * @property default Default text to use if locale not found
 */
data class SimpleLocalizedText(
    val content: Map<String, String>,
    val default: String = ""
) : LocalizedText {
    override fun getForLocale(locale: String): String {
        return content[locale] ?: content["en"] ?: content.values.firstOrNull() ?: default
    }
} 