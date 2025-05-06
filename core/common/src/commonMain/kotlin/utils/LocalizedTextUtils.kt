package utils

import model.course.LocalizedContent

/**
 * Возвращает локализованный текст из LocalizedContent с учетом предпочтительного языка
 * @param content объект с локализованным контентом
 * @param preferredLanguage предпочтительный язык (по умолчанию "ru")
 * @param fallbackLanguage запасной язык, если предпочтительный не найден (по умолчанию "en")
 * @return локализованный текст или пустую строку, если контент пуст
 */
fun getLocalizedText(content: LocalizedContent?, preferredLanguage: String = "ru", fallbackLanguage: String = "en"): String {
    if (content == null) return ""
    
    // Пробуем получить текст на предпочтительном языке
    return content.content[preferredLanguage]
        // Если не нашли, пробуем получить на запасном языке
        ?: content.content[fallbackLanguage]
        // Если и запасной язык не нашли, берем первый доступный
        ?: content.content.values.firstOrNull() 
        // Если контент пуст, возвращаем пустую строку
        ?: ""
}

/**
 * Вспомогательная функция для получения локализованного текста из Map<String, String>
 */
fun Map<String, String>.getLocalizedText(preferredLanguage: String = "ru", fallbackLanguage: String = "en"): String {
    return this[preferredLanguage]
        ?: this[fallbackLanguage]
        ?: this.values.firstOrNull()
        ?: ""
} 