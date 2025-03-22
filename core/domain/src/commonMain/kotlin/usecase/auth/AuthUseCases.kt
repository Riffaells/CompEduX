package usecase.auth

/**
 * Контейнер для всех use cases, связанных с аутентификацией.
 * Объединяет все use cases в один класс для удобства использования.
 */
data class AuthUseCases(
    val login: LoginUseCase,
    val register: RegisterUseCase,
    val logout: LogoutUseCase,
    val getCurrentUser: GetCurrentUserUseCase,
    val isAuthenticated: IsAuthenticatedUseCase,
    val updateProfile: UpdateProfileUseCase,
    val checkServerStatus: CheckServerStatusUseCase
)
