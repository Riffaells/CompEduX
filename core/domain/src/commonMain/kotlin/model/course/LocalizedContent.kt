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
    fun getContentWithFallback(primaryLanguage: String, fallbackLanguage: String = "en"): String {
        return getContent(primaryLanguage) ?: getContent(fallbackLanguage) ?: ""
    }

    /**
     * Get preferred string representation for displaying in UI
     * This is used by adapters to convert to common models
     */
    fun getPreferredString(preferredLanguage: String? = null): String {
        return if (preferredLanguage != null) {
            getContentWithFallback(preferredLanguage, defaultLanguage)
        } else if (content.containsKey(defaultLanguage)) {
            content[defaultLanguage] ?: ""
        } else {
            content.values.firstOrNull() ?: ""
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

    companion object {
        /**
         * Create localized content with a single value for the specified language
         */
        fun single(value: String, languageCode: String = "en"): LocalizedContent {
            return LocalizedContent(mapOf(languageCode.lowercase() to value))
        }
    }
}

/**
 * Custom serializer for LocalizedContent to handle JSON format conversion
 */
object LocalizedContentSerializer : KSerializer<LocalizedContent> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("LocalizedContent")

    override fun serialize(encoder: Encoder, value: LocalizedContent) {
        // Direct serialization of the content map
        val jsonEncoder = encoder as? JsonEncoder
        if (jsonEncoder != null) {
            val jsonObject = JsonObject(value.content.mapValues { (_, v) ->
                kotlinx.serialization.json.JsonPrimitive(v)
            })
            jsonEncoder.encodeJsonElement(jsonObject)
        }
    }

    override fun deserialize(decoder: Decoder): LocalizedContent {
        // Parse JSON directly to content map
        val jsonDecoder = decoder as? JsonDecoder
        if (jsonDecoder != null) {
            val jsonElement = jsonDecoder.decodeJsonElement()
            val contentMap = mutableMapOf<String, String>()

            if (jsonElement is JsonObject) {
                jsonElement.jsonObject.forEach { (key, value) ->
                    contentMap[key] = value.jsonPrimitive.content
                }
            }

            val defaultLanguage = if (contentMap.containsKey("en")) "en" else
                contentMap.keys.firstOrNull() ?: "en"

            return LocalizedContent(contentMap, defaultLanguage)
        }

        return LocalizedContent()
    }
}
