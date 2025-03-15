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
     * Использовать биометрическую аутентификацию
     */
    val useBiometricFlow: StateFlow<Boolean>
    fun saveUseBiometric(value: Boolean)

    /**
     * Автоматический выход из приложения через N минут
     */
    val autoLogoutTimeFlow: StateFlow<Int>
    fun saveAutoLogoutTime(value: Int)

    // TODO: Добавить настройку для управления сложностью пароля
    // TODO: Реализовать настройку для управления историей паролей
    // TODO: Добавить настройку для блокировки приложения после N неудачных попыток входа
}

/**
 * Реализация интерфейса SecuritySettings
 */
internal class SecuritySettingsImpl(settings: Settings) : BaseSettings(settings), SecuritySettings {

    private val useBiometric = createBooleanSetting(
        key = "USE_BIOMETRIC_OPTION",
        defaultValue = false
    )

    override val useBiometricFlow: StateFlow<Boolean> get() = useBiometric.flow
    override fun saveUseBiometric(value: Boolean) = useBiometric.save(value)

    private val autoLogoutTime = createIntSetting(
        key = "AUTO_LOGOUT_TIME_OPTION",
        defaultValue = 15 // 15 минут по умолчанию
    )

    override val autoLogoutTimeFlow: StateFlow<Int> get() = autoLogoutTime.flow
    override fun saveAutoLogoutTime(value: Int) = autoLogoutTime.save(value)

    // TODO: Реализовать шифрование чувствительных настроек
    // TODO: Добавить механизм для безопасного хранения ключей шифрования
    // TODO: Реализовать интеграцию с системными хранилищами учетных данных
}
