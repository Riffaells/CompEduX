package usecase.auth

import model.AuthResult
import repository.AuthRepository

/**
 * UseCase для обновления профиля пользователя
 */
class UpdateProfileUseCase(private val authRepository: AuthRepository) {
    /**
     * Выполнение обновления профиля
     *
     * @param username Новое имя пользователя
     * @return Результат операции обновления
     */
    suspend operator fun invoke(username: String): AuthResult {
        return authRepository.updateProfile(username)
    }
}
