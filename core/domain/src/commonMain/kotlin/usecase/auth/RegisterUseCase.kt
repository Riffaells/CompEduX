package usecase.auth

import model.auth.AuthError
import model.auth.AuthResult
import repository.auth.AuthRepository

/**
 * Use case для регистрации пользователя
 */
class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    /**
     * Выполняет регистрацию пользователя
     * @param username Имя пользователя
     * @param email Email пользователя
     * @param password Пароль пользователя
     * @param confirmPassword Подтверждение пароля
     * @param firstName Имя (опционально)
     * @param lastName Фамилия (опционально)
     * @return Результат операции
     */
    suspend operator fun invoke(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        firstName: String? = null,
        lastName: String? = null
    ): AuthResult<Unit> {
        // Проверка входных данных
        if (username.isBlank()) {
            return AuthResult.Error(AuthError("validation_error", "Имя пользователя не может быть пустым"))
        }

        if (email.isBlank()) {
            return AuthResult.Error(AuthError("validation_error", "Email не может быть пустым"))
        }

        if (!isValidEmail(email)) {
            return AuthResult.Error(AuthError("validation_error", "Некорректный формат email"))
        }

        if (password.isBlank()) {
            return AuthResult.Error(AuthError("validation_error", "Пароль не может быть пустым"))
        }

        if (password.length < 6) {
            return AuthResult.Error(AuthError("validation_error", "Пароль должен содержать не менее 6 символов"))
        }

        if (password != confirmPassword) {
            return AuthResult.Error(AuthError("validation_error", "Пароли не совпадают"))
        }

        return authRepository.register(
            username = username,
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName
        )
    }

    /**
     * Проверяет корректность формата email
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        return email.matches(emailRegex.toRegex())
    }
}
