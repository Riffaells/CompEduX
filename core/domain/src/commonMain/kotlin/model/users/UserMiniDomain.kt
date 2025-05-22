package model.users

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class UserMiniDomain(
    val id: String,
    val username: String,
    @SerialName("is_verified")
    val isVerified: Boolean,
    @SerialName("created_at")
    val createdAt: String = "",
) {
}

@Serializable
data class FriendDomain(
    val id: String,
    val username: String,
    @SerialName("is_verified")
    val isVerified: Boolean,
    @SerialName("created_at")
    val createdAt: String = "",
) {
}



@Serializable
data class UsersListDomain(
    val items: List<UserMiniDomain> = emptyList(),
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
}


