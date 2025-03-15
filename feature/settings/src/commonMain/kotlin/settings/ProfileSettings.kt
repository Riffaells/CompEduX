package settings

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Интерфейс для настроек профиля пользователя
 *
 * // TODO: Добавить поддержку нескольких профилей пользователей
 * // TODO: Реализовать синхронизацию профиля между устройствами
 * // TODO: Добавить возможность экспорта/импорта профиля
 */
interface ProfileSettings {
    /**
     * Имя пользователя
     */
    val usernameFlow: StateFlow<String>

    /**
     * Email пользователя
     */
    val emailFlow: StateFlow<String>

    /**
     * URL аватара пользователя
     */
    val avatarUrlFlow: StateFlow<String>

    /**
     * Статус пользователя
     */
    val statusFlow: StateFlow<String>

    /**
     * Настройки приватности профиля
     */
    val isProfilePublicFlow: StateFlow<Boolean>

    /**
     * Настройки уведомлений для профиля
     */
    val enableProfileNotificationsFlow: StateFlow<Boolean>

    /**
     * Сохранить имя пользователя
     */
    fun saveUsername(username: String)

    /**
     * Сохранить email пользователя
     */
    fun saveEmail(email: String)

    /**
     * Сохранить URL аватара пользователя
     */
    fun saveAvatarUrl(url: String)

    /**
     * Сохранить статус пользователя
     */
    fun saveStatus(status: String)

    /**
     * Установить приватность профиля
     */
    fun setProfilePublic(isPublic: Boolean)

    /**
     * Включить/выключить уведомления для профиля
     */
    fun setProfileNotifications(enabled: Boolean)

    /**
     * Проверить, заполнен ли профиль пользователя
     */
    fun isProfileComplete(): Boolean

    /**
     * Очистить все данные профиля
     */
    fun clearProfileData()

    // TODO: Добавить методы для управления дополнительными полями профиля
    // TODO: Реализовать валидацию данных профиля
    // TODO: Добавить поддержку социальных сетей и интеграций
}

/**
 * Реализация настроек профиля пользователя
 */
internal class ProfileSettingsImpl(settings: Settings) : BaseSettings(settings), ProfileSettings {

    private val usernameKey = "profile_username"
    private val emailKey = "profile_email"
    private val avatarUrlKey = "profile_avatar_url"
    private val statusKey = "profile_status"
    private val isProfilePublicKey = "profile_is_public"
    private val enableProfileNotificationsKey = "profile_enable_notifications"

    private val _usernameFlow = MutableStateFlow(settings.getString(usernameKey, ""))
    private val _emailFlow = MutableStateFlow(settings.getString(emailKey, ""))
    private val _avatarUrlFlow = MutableStateFlow(settings.getString(avatarUrlKey, ""))
    private val _statusFlow = MutableStateFlow(settings.getString(statusKey, ""))
    private val _isProfilePublicFlow = MutableStateFlow(settings.getBoolean(isProfilePublicKey, true))
    private val _enableProfileNotificationsFlow = MutableStateFlow(settings.getBoolean(enableProfileNotificationsKey, true))

    override val usernameFlow: StateFlow<String> = _usernameFlow.asStateFlow()
    override val emailFlow: StateFlow<String> = _emailFlow.asStateFlow()
    override val avatarUrlFlow: StateFlow<String> = _avatarUrlFlow.asStateFlow()
    override val statusFlow: StateFlow<String> = _statusFlow.asStateFlow()
    override val isProfilePublicFlow: StateFlow<Boolean> = _isProfilePublicFlow.asStateFlow()
    override val enableProfileNotificationsFlow: StateFlow<Boolean> = _enableProfileNotificationsFlow.asStateFlow()

    override fun saveUsername(username: String) {
        settings.putString(usernameKey, username)
        _usernameFlow.value = username
    }

    override fun saveEmail(email: String) {
        settings.putString(emailKey, email)
        _emailFlow.value = email
    }

    override fun saveAvatarUrl(url: String) {
        settings.putString(avatarUrlKey, url)
        _avatarUrlFlow.value = url
    }

    override fun saveStatus(status: String) {
        settings.putString(statusKey, status)
        _statusFlow.value = status
    }

    override fun setProfilePublic(isPublic: Boolean) {
        settings.putBoolean(isProfilePublicKey, isPublic)
        _isProfilePublicFlow.value = isPublic
    }

    override fun setProfileNotifications(enabled: Boolean) {
        settings.putBoolean(enableProfileNotificationsKey, enabled)
        _enableProfileNotificationsFlow.value = enabled
    }

    override fun isProfileComplete(): Boolean {
        return usernameFlow.value.isNotEmpty() && emailFlow.value.isNotEmpty()
    }

    override fun clearProfileData() {
        saveUsername("")
        saveEmail("")
        saveAvatarUrl("")
        saveStatus("")
        setProfilePublic(true)
        setProfileNotifications(true)
    }

    // TODO: Реализовать кэширование данных профиля для офлайн-доступа
    // TODO: Добавить шифрование чувствительных данных профиля
    // TODO: Реализовать механизм резервного копирования профиля
}
