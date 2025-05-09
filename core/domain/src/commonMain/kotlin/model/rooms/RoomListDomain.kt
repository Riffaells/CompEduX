package model.rooms

import kotlinx.serialization.Serializable

/**
 * Paginated list of rooms
 */
@Serializable
data class RoomListDomain(
    val items: List<RoomDomain> = emptyList(),
    val total: Int = 0,
    val page: Int = 0,
    val size: Int = 20,
    val pages: Int = 0
) {
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
                size = 0,
                pages = 0
            )
        }

        /**
         * Create a list from rooms without pagination info
         */
        fun fromList(rooms: List<RoomDomain>): RoomListDomain {
            val nonEmptyList = rooms.isNotEmpty()
            return RoomListDomain(
                items = rooms,
                total = rooms.size,
                page = 0,
                size = rooms.size,
                pages = if (nonEmptyList) 1 else 0
            )
        }
    }
}
