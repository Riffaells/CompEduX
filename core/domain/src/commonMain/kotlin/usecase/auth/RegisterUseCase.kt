package usecase.auth

import model.AuthResult
import model.auth.AuthResponseData
import repository.auth.AuthRepository

/**
 * UseCase для регистрации пользователя
 */
class RegisterUseCase(private val authRepository: AuthRepository) {
    /**
     * Выполнение регистрации
     *
     * @param email Email пользователя
     * @param password Пароль пользователя
     * @param username Имя пользователя
     * @return Результат операции регистрации
     */
    suspend operator fun invoke(email: String, password: String, username: String): AuthResult<AuthResponseData> {
        return authRepository.register(email, password, username)
    }
}
