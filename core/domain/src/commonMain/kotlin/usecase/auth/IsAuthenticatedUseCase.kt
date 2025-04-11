package usecase.auth

import repository.auth.AuthRepository

/**
 * Use Case для проверки аутентификации пользователя
 */
class IsAuthenticatedUseCase(private val authRepository: AuthRepository) {
    /**
     * Выполнение проверки аутентификации
     * @return true, если пользователь аутентифицирован, false в противном случае
     */
    suspend operator fun invoke(): Boolean {
        return authRepository.isAuthenticated()
    }
}
