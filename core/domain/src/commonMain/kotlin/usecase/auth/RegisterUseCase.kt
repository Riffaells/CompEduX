package usecase.auth

import model.DomainResult
import model.auth.AuthResponseDomain
import repository.AuthRepository

/**
 * Use Case для регистрации пользователя
 */
class RegisterUseCase(private val authRepository: AuthRepository) {
    /**
     * Выполнение регистрации
     * @param username имя пользователя
     * @param email электронная почта
     * @param password пароль
     * @return результат операции с данными аутентификации
     */
    suspend operator fun invoke(
        username: String,
        email: String,
        password: String
    ): DomainResult<AuthResponseDomain> {
        // Валидация входных данных
        if (username.isBlank()) {
            return DomainResult.Error(model.DomainError.validationError("Имя пользователя не может быть пустым"))
        }

        if (email.isBlank()) {
            return DomainResult.Error(model.DomainError.validationError("Email не может быть пустым"))
        }

        if (password.isBlank()) {
            return DomainResult.Error(model.DomainError.validationError("Пароль не может быть пустым"))
        }

        // Проверка соответствия email формату
        if (!email.contains('@')) {
            return DomainResult.Error(model.DomainError.validationError("Некорректный формат email"))
        }

        // Проверка длины пароля
        if (password.length < 6) {
            return DomainResult.Error(model.DomainError.validationError("Пароль должен содержать минимум 6 символов"))
        }

        // Делегирование выполнения репозиторию
        return authRepository.register(username, email, password)
    }
}
