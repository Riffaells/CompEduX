package usecase.auth

import model.auth.AuthResult
import repository.auth.AuthRepository

/**
 * Use case для выхода из системы
 */
class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    /**
     * Выполняет выход из системы
     * @return Результат операции
     */
    suspend operator fun invoke(): AuthResult<Unit> {
        return authRepository.logout()
    }
}
