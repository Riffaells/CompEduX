package settings

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.StateFlow

/**
 * Интерфейс для доступа к настройкам уведомлений приложения
 */
interface NotificationSettings {
    /**
     * Включены ли уведомления в целом
     */
    val enabledFlow: StateFlow<Boolean>
    fun saveEnabled(value: Boolean)

    /**
     * Включены ли звуковые уведомления
     */
    val soundEnabledFlow: StateFlow<Boolean>
    fun saveSoundEnabled(value: Boolean)

    /**
     * Громкость звуковых уведомлений (0.0 - 1.0)
     */
    val soundVolumeFlow: StateFlow<Float>
    fun saveSoundVolume(value: Float)

    /**
     * Включены ли вибрации при уведомлениях
     */
    val vibrationEnabledFlow: StateFlow<Boolean>
    fun saveVibrationEnabled(value: Boolean)

    /**
     * Включены ли уведомления о новых сообщениях
     */
    val messageNotificationsFlow: StateFlow<Boolean>
    fun saveMessageNotifications(value: Boolean)

    /**
     * Включены ли уведомления о событиях системы
     */
    val systemNotificationsFlow: StateFlow<Boolean>
    fun saveSystemNotifications(value: Boolean)

    /**
     * Время тишины - начало (часы, 0-23)
     */
    val quietTimeStartFlow: StateFlow<Int>
    fun saveQuietTimeStart(value: Int)

    /**
     * Время тишины - конец (часы, 0-23)
     */
    val quietTimeEndFlow: StateFlow<Int>
    fun saveQuietTimeEnd(value: Int)

    /**
     * Включено ли время тишины
     */
    val quietTimeEnabledFlow: StateFlow<Boolean>
    fun saveQuietTimeEnabled(value: Boolean)
}

/**
 * Реализация интерфейса NotificationSettings
 */
internal class NotificationSettingsImpl(settings: Settings) : BaseSettings(settings), NotificationSettings {

    private val enabled = createBooleanSetting(
        key = "NOTIFICATIONS_ENABLED",
        defaultValue = true
    )

    override val enabledFlow: StateFlow<Boolean> get() = enabled.flow
    override fun saveEnabled(value: Boolean) = enabled.save(value)

    private val soundEnabled = createBooleanSetting(
        key = "NOTIFICATIONS_SOUND_ENABLED",
        defaultValue = true
    )

    override val soundEnabledFlow: StateFlow<Boolean> get() = soundEnabled.flow
    override fun saveSoundEnabled(value: Boolean) = soundEnabled.save(value)

    private val soundVolume = createFloatSetting(
        key = "NOTIFICATIONS_SOUND_VOLUME",
        defaultValue = 0.7f
    )

    override val soundVolumeFlow: StateFlow<Float> get() = soundVolume.flow
    override fun saveSoundVolume(value: Float) = soundVolume.save(value)

    private val vibrationEnabled = createBooleanSetting(
        key = "NOTIFICATIONS_VIBRATION_ENABLED",
        defaultValue = true
    )

    override val vibrationEnabledFlow: StateFlow<Boolean> get() = vibrationEnabled.flow
    override fun saveVibrationEnabled(value: Boolean) = vibrationEnabled.save(value)

    private val messageNotifications = createBooleanSetting(
        key = "NOTIFICATIONS_MESSAGES_ENABLED",
        defaultValue = true
    )

    override val messageNotificationsFlow: StateFlow<Boolean> get() = messageNotifications.flow
    override fun saveMessageNotifications(value: Boolean) = messageNotifications.save(value)

    private val systemNotifications = createBooleanSetting(
        key = "NOTIFICATIONS_SYSTEM_ENABLED",
        defaultValue = true
    )

    override val systemNotificationsFlow: StateFlow<Boolean> get() = systemNotifications.flow
    override fun saveSystemNotifications(value: Boolean) = systemNotifications.save(value)

    private val quietTimeStart = createIntSetting(
        key = "NOTIFICATIONS_QUIET_TIME_START",
        defaultValue = 22 // 10 PM
    )

    override val quietTimeStartFlow: StateFlow<Int> get() = quietTimeStart.flow
    override fun saveQuietTimeStart(value: Int) = quietTimeStart.save(value)

    private val quietTimeEnd = createIntSetting(
        key = "NOTIFICATIONS_QUIET_TIME_END",
        defaultValue = 7 // 7 AM
    )

    override val quietTimeEndFlow: StateFlow<Int> get() = quietTimeEnd.flow
    override fun saveQuietTimeEnd(value: Int) = quietTimeEnd.save(value)

    private val quietTimeEnabled = createBooleanSetting(
        key = "NOTIFICATIONS_QUIET_TIME_ENABLED",
        defaultValue = false
    )

    override val quietTimeEnabledFlow: StateFlow<Boolean> get() = quietTimeEnabled.flow
    override fun saveQuietTimeEnabled(value: Boolean) = quietTimeEnabled.save(value)
}
