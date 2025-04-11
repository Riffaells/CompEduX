package usecase.auth

/**
 * Контейнер для всех Use Cases, связанных с аутентификацией
 * Позволяет получать доступ ко всем Use Cases через один объект
 */
data class AuthUseCases(
    val login: LoginUseCase,
    val register: RegisterUseCase,
    val logout: LogoutUseCase,
    val getCurrentUser: GetCurrentUserUseCase,
    val isAuthenticated: IsAuthenticatedUseCase,
    val checkServerStatus: CheckServerStatusUseCase
)
