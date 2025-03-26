package usecase.auth

import model.AuthResult
import repository.auth.AuthRepository

/**
 * UseCase для выхода из системы
 */
class LogoutUseCase(private val authRepository: AuthRepository) {
    /**
     * Выполнение выхода из системы
     *
     * @return Результат операции выхода
     */
    suspend operator fun invoke(): AuthResult<Unit> {
        return authRepository.logout()
    }
}
