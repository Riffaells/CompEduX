package util

/**
 * Utility class for reflection operations
 * Provides safe access to object properties via reflection
 */
object ReflectionUtil {
    /**
     * Gets a property value from an object using reflection
     *
     * @param obj The object to get the property from
     * @param propertyName The name of the property to get
     * @return The property value or null if not found
     */
    fun getProperty(obj: Any?, propertyName: String): Any? {
        if (obj == null) return null

        return try {
            val property = obj::class.java.getDeclaredField(propertyName)
            property.isAccessible = true
            property.get(obj)
        } catch (e: Exception) {
            try {
                // Try using the getter method as fallback
                val getterName = "get" + propertyName.capitalize()
                val method = obj::class.java.getMethod(getterName)
                method.invoke(obj)
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Gets localized text from various content models
     *
     * @param localizedContent The content object containing localized text
     * @return The localized text string
     */
    fun getLocalizedText(localizedContent: Any?): String {
        if (localizedContent == null) return ""

        return try {
            // If it's a string, return it directly
            if (localizedContent is String) return localizedContent

            // Try to get the default field
            val defaultText = getProperty(localizedContent, "default") as? String
            if (defaultText != null) return defaultText

            // Try to get the content map with localizations
            val contentMap = getProperty(localizedContent, "content") as? Map<*, *>

            // Try Russian first, then English, then the first available
            val text = contentMap?.get("ru")?.toString()
                ?: contentMap?.get("en")?.toString()
                ?: contentMap?.values?.firstOrNull()?.toString()

            text ?: localizedContent.toString()
        } catch (e: Exception) {
            localizedContent.toString()
        }
    }

    /**
     * Capitalize the first letter of a string
     */
    private fun String.capitalize(): String {
        return if (isEmpty()) this else this[0].uppercaseChar() + substring(1)
    }
} 