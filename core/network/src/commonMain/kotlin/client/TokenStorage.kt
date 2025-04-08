package client

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Интерфейс для хранения токенов аутентификации
 */
interface TokenStorage {
    /**
     * Токен доступа
     */
    val accessToken: StateFlow<String?>

    /**
     * Токен обновления
     */
    val refreshToken: StateFlow<String?>

    /**
     * Тип токена
     */
    val tokenType: StateFlow<String?>

    /**
     * Проверка, авторизован ли пользователь
     */
    val isAuthorized: StateFlow<Boolean>

    /**
     * Сохранение токенов
     */
    fun saveTokens(accessToken: String, refreshToken: String, tokenType: String)

    /**
     * Очистка токенов при выходе
     */
    fun clearTokens()

    /**
     * Получение авторизационного заголовка для запросов
     */
    fun getAuthorizationHeader(): String?
}

/**
 * Реализация хранилища токенов в памяти
 * Для продакшн кода рекомендуется использовать более безопасное хранилище
 */
class InMemoryTokenStorage : TokenStorage {

    private val _accessToken = MutableStateFlow<String?>(null)
    override val accessToken = _accessToken.asStateFlow()

    private val _refreshToken = MutableStateFlow<String?>(null)
    override val refreshToken = _refreshToken.asStateFlow()

    private val _tokenType = MutableStateFlow<String?>("Bearer")
    override val tokenType = _tokenType.asStateFlow()

    private val _isAuthorized = MutableStateFlow(false)
    override val isAuthorized = _isAuthorized.asStateFlow()

    override fun saveTokens(accessToken: String, refreshToken: String, tokenType: String) {
        _accessToken.value = accessToken
        _refreshToken.value = refreshToken
        _tokenType.value = tokenType
        _isAuthorized.value = true
    }

    override fun clearTokens() {
        _accessToken.value = null
        _refreshToken.value = null
        _isAuthorized.value = false
    }

    override fun getAuthorizationHeader(): String? {
        val accessToken = _accessToken.value ?: return null
        val tokenType = _tokenType.value ?: "Bearer"

        return "$tokenType $accessToken"
    }
}
