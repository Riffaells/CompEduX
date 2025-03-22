package usecase.auth

import model.User
import repository.auth.AuthRepository

/**
 * UseCase для получения информации о текущем пользователе
 */
class GetCurrentUserUseCase(private val authRepository: AuthRepository) {
    /**
     * Получение информации о текущем пользователе
     *
     * @return Текущий пользователь или null, если пользователь не авторизован
     */
    suspend operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }
}
