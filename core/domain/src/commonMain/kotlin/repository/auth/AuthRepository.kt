package repository.auth

import model.AuthResult
import model.User

/**
 * Репозиторий для работы с аутентификацией
 * Это интерфейс в domain слое, реализации находятся в data слое
 */
interface AuthRepository {
    /**
     * Регистрация нового пользователя
     * @param username Имя пользователя
     * @param email Email пользователя
     * @param password Пароль пользователя
     * @return Результат операции регистрации
     */
    suspend fun register(
        email: String,
        password: String,
        username: String
    ): AuthResult

    /**
     * Авторизация пользователя
     * @param email Email пользователя
     * @param password Пароль пользователя
     * @return Результат операции авторизации
     */
    suspend fun login(email: String, password: String): AuthResult

    /**
     * Выход из системы
     * @return Результат операции выхода
     */
    suspend fun logout(): AuthResult

    /**
     * Получение информации о текущем пользователе
     * @return Текущий пользователь или null, если пользователь не авторизован
     */
    suspend fun getCurrentUser(): User?

    /**
     * Проверяет, аутентифицирован ли пользователь
     * @return true, если пользователь аутентифицирован, false в противном случае
     */
    suspend fun isAuthenticated(): Boolean

    /**
     * Обновляет профиль пользователя
     * @param username Новое имя пользователя
     * @return Результат операции обновления профиля
     */
    suspend fun updateProfile(username: String): AuthResult

    /**
     * Проверяет статус сервера
     * @return Результат операции проверки статуса
     */
    suspend fun checkServerStatus(): AuthResult
}
