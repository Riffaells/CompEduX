package usecase.auth

import model.auth.AuthResult
import model.auth.ServerStatusResponse
import repository.auth.AuthRepository

/**
 * Use case для проверки статуса сервера
 */
class CheckServerStatusUseCase(
    private val authRepository: AuthRepository
) {
    /**
     * Проверяет статус сервера
     * @return Результат операции с данными о статусе сервера
     */
    suspend operator fun invoke(): AuthResult<ServerStatusResponse> {
        return authRepository.checkServerStatus()
    }
}
