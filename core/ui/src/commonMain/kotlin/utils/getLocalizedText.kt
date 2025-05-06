package utils

/**
 * Получает локализованный текст из словаря
 * @param content Словарь с локализованным контентом
 * @return Строка с локализованным текстом (приоритет: русский, английский, первый доступный)
 */
fun getLocalizedText(content: Map<String, String>?): String {
    if (content == null || content.isEmpty()) return ""
    
    // Приоритет языков: русский, английский, первый доступный
    return content["ru"] ?: content["en"] ?: content.values.firstOrNull() ?: ""
} 