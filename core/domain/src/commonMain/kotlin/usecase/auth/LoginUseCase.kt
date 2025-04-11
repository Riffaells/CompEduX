package usecase.auth

import model.DomainResult
import model.auth.AuthResponseDomain
import model.auth.LoginRequestDomain
import repository.AuthRepository

/**
 * Use Case для авторизации пользователя
 */
class LoginUseCase(private val authRepository: AuthRepository) {
    /**
     * Выполнение авторизации
     * @param email электронная почта
     * @param password пароль
     * @return результат операции с данными аутентификации
     */
    suspend operator fun invoke(email: String, password: String): DomainResult<AuthResponseDomain> {
        // Валидация входных данных
        if (email.isBlank()) {
            return DomainResult.Error(model.DomainError.validationError("Email не может быть пустым"))
        }

        if (password.isBlank()) {
            return DomainResult.Error(model.DomainError.validationError("Пароль не может быть пустым"))
        }

        // Делегирование выполнения репозиторию
        return authRepository.login(email, password)
    }
}
