package usecase.auth

import model.AuthResult
import model.auth.AuthResponseDomain
import repository.auth.AuthRepository

/**
 * UseCase для авторизации пользователя
 */
class LoginUseCase(private val authRepository: AuthRepository) {
    /**
     * Выполнение авторизации
     *
     * @param email Email пользователя
     * @param password Пароль пользователя
     * @return Результат операции авторизации
     */
    suspend operator fun invoke(email: String, password: String): AuthResult<AuthResponseDomain> {
        return authRepository.login(email, password)
    }
}
