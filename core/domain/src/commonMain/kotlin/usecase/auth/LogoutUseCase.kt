package usecase.auth

import model.DomainResult
import repository.auth.AuthRepository

/**
 * Use Case для выхода из системы
 */
class LogoutUseCase(private val authRepository: AuthRepository) {
    /**
     * Выполнение выхода из системы
     * @return результат операции
     */
    suspend operator fun invoke(): DomainResult<Unit> {
        return authRepository.logout()
    }
}
