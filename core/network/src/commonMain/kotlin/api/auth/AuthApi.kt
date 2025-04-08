package api.auth

import api.dto.NetworkAuthResponseDto
import api.model.AuthResultNetwork
import model.User
import model.auth.*

/**
 * Интерфейс для работы с API аутентификации
 */
interface AuthApi {
    /**
     * Регистрация нового пользователя
     * @param request Данные для регистрации
     * @return Результат операции с данными аутентификации
     */
    suspend fun register(request: RegisterRequest): AuthResultNetwork<NetworkAuthResponseDto>

    /**
     * Авторизация пользователя
     * @param request Данные для авторизации
     * @return Результат операции с данными аутентификации
     */
    suspend fun login(request: LoginRequest): AuthResultNetwork<NetworkAuthResponseDto>

    /**
     * Обновление токена доступа
     * @param request Запрос на обновление токена
     * @return Результат операции с новыми данными аутентификации
     */
    suspend fun refreshToken(request: RefreshTokenRequest): AuthResultNetwork<NetworkAuthResponseDto>

    /**
     * Получение информации о текущем пользователе
     * @param token Токен доступа
     * @return Результат операции с данными пользователя
     */
    suspend fun getCurrentUser(token: String): AuthResultNetwork<User>

    /**
     * Выход из системы (инвалидация токена)
     * @param token Токен доступа
     * @return Результат операции
     */
    suspend fun logout(token: String): AuthResultNetwork<Unit>

    /**
     * Проверка статуса сервера
     * @return Результат операции с данными о статусе сервера
     */
    suspend fun checkServerStatus(): AuthResultNetwork<ServerStatusResponse>

    /**
     * Обновление профиля пользователя
     * @param token Токен доступа
     * @param username Новое имя пользователя
     * @return Результат операции с обновленными данными пользователя
     */
    suspend fun updateProfile(token: String, username: String): AuthResultNetwork<User>
}
