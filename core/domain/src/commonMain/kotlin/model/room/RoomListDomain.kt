package model.room

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Доменная модель списка комнат
 *
 * @property items Список комнат
 * @property total Общее количество комнат
 * @property page Текущая страница
 * @property pageSize Размер страницы
 */
@Serializable
data class RoomListDomain(
    val items: List<RoomDomain>,
    val total: Int,
    val page: Int,
    @SerialName("page_size")
    val pageSize: Int
) {
    /**
     * Количество страниц
     */
    val pages: Int
        get() = if (pageSize > 0) (total + pageSize - 1) / pageSize else 0

    /**
     * Проверка, пуст ли список
     */
    val isEmpty: Boolean
        get() = items.isEmpty()

    /**
     * Проверка, является ли эта страница первой
     */
    val isFirst: Boolean
        get() = page == 0

    /**
     * Проверка, является ли эта страница последней
     */
    val isLast: Boolean
        get() = page >= pages - 1 || isEmpty

    companion object {
        /**
         * Create an empty room list
         */
        fun empty(): RoomListDomain {
            return RoomListDomain(
                items = emptyList(),
                total = 0,
                page = 0,
                pageSize = 0
            )
        }

        /**
         * Create a list from rooms without pagination info
         */
        fun fromList(rooms: List<RoomDomain>): RoomListDomain {
            return RoomListDomain(
                items = rooms,
                total = rooms.size,
                page = 0,
                pageSize = rooms.size
            )
        }
    }
}
