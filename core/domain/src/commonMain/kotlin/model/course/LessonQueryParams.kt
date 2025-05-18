package model.course

import kotlinx.serialization.Serializable

/**
 * Параметры запроса для получения списка уроков
 */
@Serializable
data class LessonQueryParams(
    /**
     * ID курса для фильтрации
     */
    val courseId: String? = null,
    
    /**
     * ID модуля для фильтрации
     */
    val moduleId: String? = null,
    
    /**
     * ID узла дерева технологий для фильтрации
     */
    val treeNodeId: String? = null,
    
    /**
     * Код языка для фильтрации
     */
    val language: String? = null,
    
    /**
     * Фильтр по статусу публикации
     */
    val isPublished: Boolean? = null,
    
    /**
     * Тип содержимого урока для фильтрации
     */
    val contentType: LessonContentTypeDomain? = null,
    
    /**
     * Поисковый запрос для поиска по заголовку и описанию
     */
    val search: String? = null,
    
    /**
     * Номер страницы для пагинации
     */
    val page: Int = 0,
    
    /**
     * Размер страницы для пагинации
     */
    val size: Int = 20,
    
    /**
     * Поле для сортировки
     */
    val sortBy: LessonSortOptionDomain = LessonSortOptionDomain.ORDER,
    
    /**
     * Направление сортировки
     */
    val sortDirection: SortDirectionDomain = SortDirectionDomain.ASC
)

/**
 * Опции сортировки для уроков
 */
enum class LessonSortOptionDomain {
    ORDER,
    TITLE,
    CREATED_AT,
    UPDATED_AT,
    DURATION
} 