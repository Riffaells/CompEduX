package usecase.auth

import model.DomainResult
import model.UserDomain
import repository.AuthRepository

/**
 * Use Case для получения текущего пользователя
 */
class GetCurrentUserUseCase(private val authRepository: AuthRepository) {
    /**
     * Выполнение получения текущего пользователя
     * @return результат операции с данными пользователя
     */
    suspend operator fun invoke(): DomainResult<UserDomain> {
        return authRepository.getCurrentUser()
    }
}
