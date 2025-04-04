# Domain Module

Модуль содержит бизнес-логику и доменные модели приложения. Является центральным слоем архитектуры, не зависит от других модулей.

## Структура

```
core/domain/
├── src/
│   └── commonMain/
│       ├── kotlin/
│       │   ├── model/           # Доменные модели
│       │   │   ├── auth/        # Модели аутентификации
│       │   │   │   ├── User.kt      # Модель пользователя
│       │   │   │   ├── AppError.kt  # Модель ошибки
│       │   │   │   ├── ErrorCode.kt # Коды ошибок
│       │   │   │   └── AuthResult.kt # Результат операций аутентификации
│       │   │   │
│       │   │   ├── repository/      # Интерфейсы репозиториев
│       │   │   │   └── auth/        # Репозитории для аутентификации
│       │   │   │       ├── AuthRepository.kt # Интерфейс репозитория аутентификации
│       │   │   │       └── AuthState.kt      # Состояние аутентификации
│       │   │   │
│       │   │   └── usecase/         # Сценарии использования (бизнес-логика)
│       │   │   │   └── auth/        # Сценарии для аутентификации
│       │   │   │   │   ├── LoginUseCase.kt
│       │   │   │   │   ├── RegisterUseCase.kt
│       │   │   │   │   └── LogoutUseCase.kt
│       │   │   │   │
│       │   │   └── resources/
```

## Основные компоненты

### Модели (model/)
Доменные объекты, представляющие бизнес-сущности приложения.
Не содержат аннотаций сериализации и логики преобразования данных.

Пример:
```kotlin
// model/auth/AuthResponseData.kt
data class AuthResponseData(
    val userId: String,
    val username: String,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "bearer",
    val expiresIn: Int = 0
)
```

### Результаты (model/AuthResult.kt)
Sealed класс для представления результатов операций:
```kotlin
sealed class AuthResult<T> {
    data class Success<T>(
        val data: T,
        val user: User? = null,
        val tokens: TokenPair? = null
    ) : AuthResult<T>()

    data class Error<T>(val error: AppError) : AuthResult<T>()

    data object Loading : AuthResult<Nothing>()
}
```

### Репозитории (repository/)
Интерфейсы для работы с данными. Реализации находятся в data модуле.

Пример:
```kotlin
// repository/auth/AuthRepository.kt
interface AuthRepository {
    val authState: StateFlow<AuthState>

    suspend fun login(email: String, password: String): AuthResult<AuthResponseData>
    suspend fun register(email: String, password: String, username: String): AuthResult<AuthResponseData>
    suspend fun logout(): AuthResult<Unit>
    // ...
}
```

### Use Cases (usecase/)
Классы, реализующие бизнес-логику приложения.
Каждый use case отвечает за одну конкретную операцию.

Пример:
```kotlin
// usecase/auth/LoginUseCase.kt
class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): AuthResult<AuthResponseData> {
        return authRepository.login(email, password)
    }
}
```
