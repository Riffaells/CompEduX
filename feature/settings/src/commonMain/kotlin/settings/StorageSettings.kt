package settings

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.StateFlow

/**
 * Интерфейс для доступа к настройкам хранения данных приложения
 */
interface StorageSettings {
    /**
     * Автоматическая очистка кэша (в днях, 0 - никогда)
     */
    val autoClearCacheDaysFlow: StateFlow<Int>
    fun saveAutoClearCacheDays(value: Int)

    /**
     * Максимальный размер кэша (в МБ)
     */
    val maxCacheSizeFlow: StateFlow<Int>
    fun saveMaxCacheSize(value: Int)

    /**
     * Сохранять ли историю поиска
     */
    val saveSearchHistoryFlow: StateFlow<Boolean>
    fun saveSaveSearchHistory(value: Boolean)

    /**
     * Автоматическая загрузка изображений
     */
    val autoDownloadImagesFlow: StateFlow<Boolean>
    fun saveAutoDownloadImages(value: Boolean)

    /**
     * Автоматическая загрузка видео
     */
    val autoDownloadVideosFlow: StateFlow<Boolean>
    fun saveAutoDownloadVideos(value: Boolean)

    /**
     * Автоматическая загрузка документов
     */
    val autoDownloadDocumentsFlow: StateFlow<Boolean>
    fun saveAutoDownloadDocuments(value: Boolean)

    /**
     * Загружать медиа только через Wi-Fi
     */
    val downloadMediaOnlyWifiFlow: StateFlow<Boolean>
    fun saveDownloadMediaOnlyWifi(value: Boolean)

    /**
     * Качество загружаемых изображений (0 - низкое, 1 - среднее, 2 - высокое)
     */
    val imageQualityFlow: StateFlow<Int>
    fun saveImageQuality(value: Int)

    /**
     * Путь для сохранения загруженных файлов
     */
    val downloadPathFlow: StateFlow<String>
    fun saveDownloadPath(value: String)
}

/**
 * Реализация интерфейса StorageSettings
 */
internal class StorageSettingsImpl(settings: Settings) : BaseSettings(settings), StorageSettings {

    private val autoClearCacheDays = createIntSetting(
        key = "STORAGE_AUTO_CLEAR_CACHE_DAYS",
        defaultValue = 30
    )

    override val autoClearCacheDaysFlow: StateFlow<Int> get() = autoClearCacheDays.flow
    override fun saveAutoClearCacheDays(value: Int) = autoClearCacheDays.save(value)

    private val maxCacheSize = createIntSetting(
        key = "STORAGE_MAX_CACHE_SIZE",
        defaultValue = 500 // 500 МБ
    )

    override val maxCacheSizeFlow: StateFlow<Int> get() = maxCacheSize.flow
    override fun saveMaxCacheSize(value: Int) = maxCacheSize.save(value)

    private val saveSearchHistory = createBooleanSetting(
        key = "STORAGE_SAVE_SEARCH_HISTORY",
        defaultValue = true
    )

    override val saveSearchHistoryFlow: StateFlow<Boolean> get() = saveSearchHistory.flow
    override fun saveSaveSearchHistory(value: Boolean) = saveSearchHistory.save(value)

    private val autoDownloadImages = createBooleanSetting(
        key = "STORAGE_AUTO_DOWNLOAD_IMAGES",
        defaultValue = true
    )

    override val autoDownloadImagesFlow: StateFlow<Boolean> get() = autoDownloadImages.flow
    override fun saveAutoDownloadImages(value: Boolean) = autoDownloadImages.save(value)

    private val autoDownloadVideos = createBooleanSetting(
        key = "STORAGE_AUTO_DOWNLOAD_VIDEOS",
        defaultValue = false
    )

    override val autoDownloadVideosFlow: StateFlow<Boolean> get() = autoDownloadVideos.flow
    override fun saveAutoDownloadVideos(value: Boolean) = autoDownloadVideos.save(value)

    private val autoDownloadDocuments = createBooleanSetting(
        key = "STORAGE_AUTO_DOWNLOAD_DOCUMENTS",
        defaultValue = false
    )

    override val autoDownloadDocumentsFlow: StateFlow<Boolean> get() = autoDownloadDocuments.flow
    override fun saveAutoDownloadDocuments(value: Boolean) = autoDownloadDocuments.save(value)

    private val downloadMediaOnlyWifi = createBooleanSetting(
        key = "STORAGE_DOWNLOAD_MEDIA_ONLY_WIFI",
        defaultValue = true
    )

    override val downloadMediaOnlyWifiFlow: StateFlow<Boolean> get() = downloadMediaOnlyWifi.flow
    override fun saveDownloadMediaOnlyWifi(value: Boolean) = downloadMediaOnlyWifi.save(value)

    private val imageQuality = createIntSetting(
        key = "STORAGE_IMAGE_QUALITY",
        defaultValue = 1 // Среднее качество
    )

    override val imageQualityFlow: StateFlow<Int> get() = imageQuality.flow
    override fun saveImageQuality(value: Int) = imageQuality.save(value)

    private val downloadPath = createStringSetting(
        key = "STORAGE_DOWNLOAD_PATH",
        defaultValue = "Downloads"
    )

    override val downloadPathFlow: StateFlow<String> get() = downloadPath.flow
    override fun saveDownloadPath(value: String) = downloadPath.save(value)
}
