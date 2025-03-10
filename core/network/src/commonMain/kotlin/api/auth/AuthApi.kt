package api.auth

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
    suspend fun register(request: RegisterRequest): AuthResult<AuthResponse>

    /**
     * Авторизация пользователя
     * @param request Данные для авторизации
     * @return Результат операции с данными аутентификации
     */
    suspend fun login(request: LoginRequest): AuthResult<AuthResponse>

    /**
     * Обновление токена доступа
     * @param request Запрос на обновление токена
     * @return Результат операции с новыми данными аутентификации
     */
    suspend fun refreshToken(request: RefreshTokenRequest): AuthResult<AuthResponse>

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
}
