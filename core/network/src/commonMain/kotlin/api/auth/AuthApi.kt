package api.auth

import api.dto.AuthResponseDto
import model.AuthResult
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
    suspend fun register(request: RegisterRequest): AuthResult<AuthResponseDto>

    /**
     * Авторизация пользователя
     * @param request Данные для авторизации
     * @return Результат операции с данными аутентификации
     */
    suspend fun login(request: LoginRequest): AuthResult<AuthResponseDto>

    /**
     * Обновление токена доступа
     * @param request Запрос на обновление токена
     * @return Результат операции с новыми данными аутентификации
     */
    suspend fun refreshToken(request: RefreshTokenRequest): AuthResult<AuthResponseDto>

    /**
     * Получение информации о текущем пользователе
     * @param token Токен доступа
     * @return Результат операции с данными пользователя
     */
    suspend fun getCurrentUser(token: String): AuthResult<User>

    /**
     * Выход из системы (инвалидация токена)
     * @param token Токен доступа
     * @return Результат операции
     */
    suspend fun logout(token: String): AuthResult<Unit>

    /**
     * Проверка статуса сервера
     * @return Результат операции с данными о статусе сервера
     */
    suspend fun checkServerStatus(): AuthResult<ServerStatusResponse>

    /**
     * Обновление профиля пользователя
     * @param token Токен доступа
     * @param username Новое имя пользователя
     * @return Результат операции с обновленными данными пользователя
     */
    suspend fun updateProfile(token: String, username: String): AuthResult<User>
}
