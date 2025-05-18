package model.course

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

/**
 * Represents content that can be localized to different languages
 * The key in the content map is the language code (e.g., "en", "ru", "fr")
 * and the value is the content in that language
 */
@Serializable(with = LocalizedContentSerializer::class)
data class LocalizedContent(
    val content: Map<String, String> = emptyMap(),
    val defaultLanguage: String = "en"
) {
    /**
     * Get content for a specific language
     * @param languageCode the language code
     * @return content in the specified language or null if not available
     */
    fun getContent(languageCode: String): String? {
        return content[languageCode.lowercase()]
    }

    /**
     * Get content for a specific language with fallback to another language
     * @param primaryLanguage the primary language to try
     * @param fallbackLanguage the fallback language if primary is not available
     * @return content in one of the languages or empty string if none available
     */
    fun getContentWithFallback(primaryLanguage: String, fallbackLanguage: String = defaultLanguage): String {
        return getContent(primaryLanguage) ?: getContent(fallbackLanguage) ?: ""
    }

    /**
     * Get preferred string representation for displaying in UI
     * This is used by adapters to convert to common models
     */
    fun getPreferredString(preferredLanguage: String? = null): String {
        return when {
            preferredLanguage != null -> getContentWithFallback(preferredLanguage, defaultLanguage)
            content.containsKey(defaultLanguage) -> content[defaultLanguage] ?: ""
            else -> content.values.firstOrNull() ?: ""
        }
    }

    /**
     * Check if this content is available in a specific language
     * @param languageCode the language code to check
     * @return true if content is available in the specified language
     */
    fun hasLanguage(languageCode: String): Boolean {
        return content.containsKey(languageCode.lowercase())
    }

    /**
     * Get all available language codes in this content
     * @return set of language codes
     */
    fun getAvailableLanguages(): Set<String> {
        return content.keys
    }

    /**
     * Check if the localized content is empty (has no translations)
     * @return true if there are no translations available
     */
    fun isEmpty(): Boolean = content.isEmpty() || content.values.all { it.isBlank() }

    companion object {
        /**
         * Create localized content with a single value for the specified language
         */
        fun single(value: String, languageCode: String = "en"): LocalizedContent {
            return LocalizedContent(mapOf(languageCode.lowercase() to value), languageCode)
        }

        /**
         * Create localized content from a string, setting it for both English and Russian
         * This is a convenience method for the most common use case in this application
         */
        fun createBilingual(value: String): LocalizedContent {
            return LocalizedContent(mapOf("en" to value, "ru" to value))
        }
    }
}

/**
 * Custom serializer for LocalizedContent to handle JSON format conversion
 */
object LocalizedContentSerializer : KSerializer<LocalizedContent> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("LocalizedContent")

    override fun serialize(encoder: Encoder, value: LocalizedContent) {
        val jsonEncoder = encoder as? JsonEncoder ?: return
        val jsonObject = JsonObject(value.content.mapValues { JsonPrimitive(it.value) })
        jsonEncoder.encodeJsonElement(jsonObject)
    }

    override fun deserialize(decoder: Decoder): LocalizedContent {
        val jsonDecoder = decoder as? JsonDecoder ?: return LocalizedContent()
        val jsonElement = jsonDecoder.decodeJsonElement()
        
        if (jsonElement !is JsonObject) return LocalizedContent()
        
        val contentMap = jsonElement.mapValues { it.value.jsonPrimitive.content }
        val defaultLanguage = when {
            contentMap.containsKey("en") -> "en"
            contentMap.isNotEmpty() -> contentMap.keys.first()
            else -> "en"
        }

        return LocalizedContent(contentMap, defaultLanguage)
    }
}
