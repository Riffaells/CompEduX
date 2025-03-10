package usecase.auth

import model.auth.AuthResult
import repository.auth.AuthRepository

/**
 * Use case для авторизации пользователя
 */
class LoginUseCase(
    private val authRepository: AuthRepository
) {
    /**
     * Выполняет авторизацию пользователя
     * @param username Имя пользователя
     * @param password Пароль пользователя
     * @return Результат операции
     */
    suspend operator fun invoke(username: String, password: String): AuthResult<Unit> {
        // Проверка входных данных
        if (username.isBlank()) {
            return AuthResult.Error(model.auth.AuthError("validation_error", "Имя пользователя не может быть пустым"))
        }

        if (password.isBlank()) {
            return AuthResult.Error(model.auth.AuthError("validation_error", "Пароль не может быть пустым"))
        }

        return authRepository.login(username, password)
    }
}
