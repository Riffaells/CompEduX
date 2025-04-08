package api

import api.model.AuthResultNetwork
import model.User
import model.auth.AuthResponseDomain
import model.auth.LoginRequest
import model.auth.RefreshTokenRequest
import model.auth.RegisterRequest
import model.auth.ServerStatusResponse

/**
 * Интерфейс для API аутентификации, который используется в адаптере
 * для абстрагирования от конкретной реализации сетевого клиента
 */
interface NetworkAuthApi {
    /**
     * Регистрация нового пользователя
     * @param request данные для регистрации
     * @return результат операции с данными аутентификации
     */
    suspend fun register(request: RegisterRequest): AuthResultNetwork<AuthResponseDomain>

    /**
     * Вход в систему
     * @param request данные для входа
     * @return результат операции с данными аутентификации
     */
    suspend fun login(request: LoginRequest): AuthResultNetwork<AuthResponseDomain>

    /**
     * Обновление токена доступа
     * @param request запрос на обновление токена
     * @return результат операции с новыми данными аутентификации
     */
    suspend fun refreshToken(request: RefreshTokenRequest): AuthResultNetwork<AuthResponseDomain>

    /**
     * Получение данных текущего пользователя
     * @param token токен доступа
     * @return результат операции с данными пользователя
     */
    suspend fun getCurrentUser(token: String): AuthResultNetwork<User>

    /**
     * Выход из системы
     * @param token токен доступа
     * @return результат операции
     */
    suspend fun logout(token: String): AuthResultNetwork<Unit>

    /**
     * Проверка статуса сервера
     * @return результат операции со статусом сервера
     */
    suspend fun checkServerStatus(): AuthResultNetwork<ServerStatusResponse>

    /**
     * Обновление профиля пользователя
     * @param token токен доступа
     * @param username новое имя пользователя
     * @return результат операции с обновленными данными пользователя
     */
    suspend fun updateProfile(token: String, username: String): AuthResultNetwork<User>
}
