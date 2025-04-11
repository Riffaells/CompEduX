package usecase.auth

import model.DomainResult
import model.auth.ServerStatusResponseDomain
import repository.auth.AuthRepository

/**
 * Use Case для проверки статуса сервера
 */
class CheckServerStatusUseCase(private val authRepository: AuthRepository) {
    /**
     * Выполнение проверки статуса сервера
     * @return результат операции с данными о статусе сервера
     */
    suspend operator fun invoke(): DomainResult<ServerStatusResponseDomain> {
        return authRepository.checkServerStatus()
    }
}
