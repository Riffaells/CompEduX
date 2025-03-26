package settings

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.StateFlow

/**
 * Интерфейс для доступа к настройкам безопасности приложения
 *
 * // TODO: Добавить поддержку двухфакторной аутентификации
 * // TODO: Реализовать настройки для управления паролями
 * // TODO: Добавить настройки для шифрования данных
 */
interface SecuritySettings {
    /**
     * Поток, содержащий состояние биометрической аутентификации
     */
    val useBiometricFlow: StateFlow<Boolean>

    /**
     * Поток, содержащий состояние сохранения учетных данных
     */
    val saveCredentialsFlow: StateFlow<Boolean>

    /**
     * Поток, содержащий время автоматического выхода из системы
     */
    val autoLogoutTimeFlow: StateFlow<Int>

    /**
     * Сохраняет настройку биометрической аутентификации
     */
    fun saveUseBiometric(value: Boolean)

    /**
     * Сохраняет настройку сохранения учетных данных
     */
    fun saveSaveCredentials(value: Boolean)

    /**
     * Сохраняет время автоматического выхода из системы
     */
    fun saveAutoLogoutTime(minutes: Int)

    /**
     * Сохраняет токен аутентификации
     *
     * @param token Токен для сохранения
     */
    fun saveAuthToken(token: String)

    /**
     * Получает токен аутентификации
     *
     * @return Токен аутентификации или null, если не найден
     */
    fun getAuthToken(): String?

    /**
     * Очищает токен аутентификации
     */
    fun clearAuthToken()

    /**
     * Проверяет, есть ли токен аутентификации
     *
     * @return true, если токен существует
     */
    fun hasAuthToken(): Boolean

    /**
     * Сохраняет токен обновления
     *
     * @param token Токен обновления для сохранения
     */
    fun saveRefreshToken(token: String)

    /**
     * Получает токен обновления
     *
     * @return Токен обновления или null, если не найден
     */
    fun getRefreshToken(): String?

    /**
     * Очищает токен обновления
     */
    fun clearRefreshToken()

    // TODO: Добавить настройку для управления сложностью пароля
    // TODO: Добавить настройку для блокировки приложения после N неудачных попыток входа
}

/**
 * Реализация настроек безопасности приложения
 */
internal class SecuritySettingsImpl(settings: Settings) : BaseSettings(settings), SecuritySettings {
    // Ключи для настроек
    private val USE_BIOMETRIC_KEY = "USE_BIOMETRIC"
    private val AUTO_LOGOUT_TIME_KEY = "AUTO_LOGOUT_TIME"
    private val SAVE_CREDENTIALS_KEY = "SAVE_CREDENTIALS"
    private val AUTH_TOKEN_KEY = "AUTH_TOKEN"
    private val REFRESH_TOKEN_KEY = "REFRESH_TOKEN"

    // Настройка биометрической аутентификации
    private val useBiometric = createBooleanSetting(
        key = USE_BIOMETRIC_KEY,
        defaultValue = false
    )

    override val useBiometricFlow: StateFlow<Boolean> get() = useBiometric.flow
    override fun saveUseBiometric(value: Boolean) = useBiometric.save(value)

    // Настройка времени автоматического выхода из системы
    private val autoLogoutTime = createIntSetting(
        key = AUTO_LOGOUT_TIME_KEY,
        defaultValue = 30 // 30 минут по умолчанию
    )

    override val autoLogoutTimeFlow: StateFlow<Int> get() = autoLogoutTime.flow
    override fun saveAutoLogoutTime(minutes: Int) = autoLogoutTime.save(minutes)

    // Настройка сохранения учетных данных
    private val saveCredentials = createBooleanSetting(
        key = SAVE_CREDENTIALS_KEY,
        defaultValue = false
    )

    override val saveCredentialsFlow: StateFlow<Boolean> get() = saveCredentials.flow
    override fun saveSaveCredentials(value: Boolean) = saveCredentials.save(value)

    // Методы для работы с токеном аутентификации
    override fun saveAuthToken(token: String) {
        settings.putString(AUTH_TOKEN_KEY, token)
    }

    override fun getAuthToken(): String? {
        val token = settings.getStringOrNull(AUTH_TOKEN_KEY)
        return if (token.isNullOrBlank()) null else token
    }

    override fun clearAuthToken() {
        settings.remove(AUTH_TOKEN_KEY)
    }

    override fun hasAuthToken(): Boolean {
        return getAuthToken() != null
    }

    // Методы для работы с токеном обновления
    override fun saveRefreshToken(token: String) {
        settings.putString(REFRESH_TOKEN_KEY, token)
    }

    override fun getRefreshToken(): String? {
        val token = settings.getStringOrNull(REFRESH_TOKEN_KEY)
        return if (token.isNullOrBlank()) null else token
    }

    override fun clearRefreshToken() {
        settings.remove(REFRESH_TOKEN_KEY)
    }

    // TODO: Реализовать шифрование чувствительных настроек
    // TODO: Добавить механизм для безопасного хранения ключей шифрования
    // TODO: Реализовать интеграцию с системными хранилищами учетных данных
}
