package usecase.auth

import model.DomainResult
import model.auth.ServerStatusDomain
import repository.AuthRepository

/**
 * Use Case для проверки статуса сервера
 */
class CheckServerStatusUseCase(private val authRepository: AuthRepository) {
    /**
     * Выполнение проверки статуса сервера
     * @return результат операции с данными о статусе сервера
     */
    suspend operator fun invoke(): DomainResult<ServerStatusDomain> {
        return authRepository.checkServerStatus()
    }
}
