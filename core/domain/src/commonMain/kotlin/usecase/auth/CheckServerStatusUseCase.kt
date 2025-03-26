package usecase.auth

import model.AuthResult
import model.auth.ServerStatusResponse
import repository.auth.AuthRepository

/**
 * UseCase для проверки статуса сервера
 */
class CheckServerStatusUseCase(private val authRepository: AuthRepository) {
    /**
     * Выполнение проверки статуса сервера
     *
     * @return Результат операции проверки статуса
     */
    suspend operator fun invoke(): AuthResult<ServerStatusResponse> {
        return authRepository.checkServerStatus()
    }
}
