package usecase.auth

import model.auth.AuthResult
import model.auth.User
import repository.auth.AuthRepository

/**
 * Use case для получения информации о текущем пользователе
 */
class GetCurrentUserUseCase(
    private val authRepository: AuthRepository
) {
    /**
     * Получает информацию о текущем пользователе
     * @return Результат операции с данными пользователя
     */
    suspend operator fun invoke(): AuthResult<User> {
        // Проверяем, аутентифицирован ли пользователь
        if (!authRepository.isAuthenticated()) {
            return model.auth.AuthResult.Error(
                model.auth.AuthError("auth_error", "Пользователь не авторизован")
            )
        }

        return authRepository.getCurrentUser()
    }
}
