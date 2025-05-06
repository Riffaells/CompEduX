package utils

import model.course.LocalizedContent

/**
 * Получение локализованного текста из объекта LocalizedContent.
 * Текущая реализация просто берет контент для языка "ru", 
 * с фолбэком на "en" или первый доступный язык.
 */
fun getLocalizedText(content: LocalizedContent?): String {
    if (content == null) return ""
    
    // Пробуем получить контент на русском языке
    val russianContent = content.getContent("ru")
    if (russianContent != null) return russianContent
    
    // Если русского нет, пробуем получить на английском
    val englishContent = content.getContent("en")
    if (englishContent != null) return englishContent
    
    // Если нет ни русского, ни английского, берем первый доступный язык
    val availableLanguages = content.getAvailableLanguages()
    if (availableLanguages.isNotEmpty()) {
        val firstLanguage = availableLanguages.first()
        val firstContent = content.getContent(firstLanguage)
        if (firstContent != null) return firstContent
    }
    
    // Если контента вообще нет, возвращаем пустую строку
    return ""
} 