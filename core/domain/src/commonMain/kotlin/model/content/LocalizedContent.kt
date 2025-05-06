package model.content

/**
 * Класс для представления локализованного контента.
 * Текущая реализация поддерживает только текст по умолчанию,
 * но может быть расширена для поддержки множества языков.
 */
data class LocalizedContent(
    val defaultText: String,
    val translations: Map<String, String> = emptyMap()
) {
    /**
     * Получить текст для указанного языка, или defaultText, если перевод не найден
     */
    fun getTextForLanguage(languageCode: String): String {
        return translations[languageCode] ?: defaultText
    }
    
    companion object {
        /**
         * Создать локализованный контент из строки.
         */
        fun fromString(text: String): LocalizedContent {
            return LocalizedContent(text)
        }
        
        /**
         * Создать локализованный контент из карты с языковыми кодами и текстами.
         * Первый элемент будет использован как defaultText.
         */
        fun fromMap(translations: Map<String, String>): LocalizedContent {
            if (translations.isEmpty()) {
                return LocalizedContent("")
            }
            
            val defaultEntry = translations.entries.first()
            return LocalizedContent(
                defaultText = defaultEntry.value,
                translations = translations - defaultEntry.key
            )
        }
    }
} 