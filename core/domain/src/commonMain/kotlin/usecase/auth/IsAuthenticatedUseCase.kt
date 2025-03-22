package usecase.auth

import repository.auth.AuthRepository

/**
 * UseCase для проверки состояния аутентификации
 */
class IsAuthenticatedUseCase(private val authRepository: AuthRepository) {
    /**
     * Проверка состояния аутентификации
     *
     * @return true, если пользователь авторизован
     */
    suspend operator fun invoke(): Boolean {
        return authRepository.isAuthenticated()
    }
}
