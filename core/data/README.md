# Data Module

Модуль отвечает за работу с данными приложения, реализуя интерфейсы репозиториев из модуля domain и взаимодействуя с network и persistence слоями.

## Структура

```
core/data/
├── src/
│   └── commonMain/
│       └── kotlin/
│           ├── model/             # Модели данных
│           │   └── auth/          # Модели для аутентификации
│           │       └── DataAuthModels.kt  # Модели запросов/ответов для аутентификации
│           │
│           ├── repository/        # Реализации репозиториев
│           │   ├── auth/          # Репозитории для аутентификации
│           │   │   └── AuthRepositoryImpl.kt  # Реализация репозитория аутентификации
│           │   │
│           │   └── mapper/        # Мапперы для преобразования между слоями
│           │       └── AuthMapper.kt # Маппер для аутентификации
│           │
│           ├── datasource/        # Источники данных
│           │   ├── local/         # Локальные источники данных
│           │   │   └── UserLocalDataSource.kt
│           │   │
│           │   └── remote/        # Удаленные источники данных
│           │       └── UserRemoteDataSource.kt
│           │
│           └── di/                # Зависимости и инъекции
│               └── DataModule.kt  # Модуль для предоставления зависимостей
```

## Основные компоненты

### Реализации репозиториев (repository/)

Реализации интерфейсов из domain слоя:

```kotlin
// repository/auth/AuthRepositoryImpl.kt
class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val settings: MultiplatformSettings
) : AuthRepository {

    private var currentUser: User? = null
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState

    override suspend fun login(email: String, password: String): AuthResult<AuthResponseData> {
        // Реализация логина
    }

    override suspend fun register(email: String, password: String, username: String): AuthResult<AuthResponseData> {
        // Реализация регистрации
    }

    // Другие методы...
}
```

### Мапперы (repository/mapper/)

Классы для преобразования данных между слоями:

```kotlin
// repository/mapper/AuthMapper.kt
object AuthMapper {

    fun mapDataUserToDomainUser(user: User): User {
        // Преобразование пользователя
    }

    fun <T> createErrorResult(error: AppError): AuthResult<T> {
        return AuthResult.Error(error)
    }

    fun <T> transformLoading(): AuthResult<T> {
        @Suppress("UNCHECKED_CAST")
        return AuthResult.Loading as AuthResult<T>
    }

    // Другие методы маппинга...
}
```

### Модели данных (model/)

Модели для сериализации/десериализации и запросов к API:

```kotlin
// model/auth/DataAuthModels.kt
@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

// Другие модели...
```

### Источники данных (datasource/)

Классы для работы с разными источниками данных:

```kotlin
// datasource/remote/UserRemoteDataSource.kt
class UserRemoteDataSource(
    private val authApi: AuthApi
) {
    suspend fun getUser(): User? {
        // Получение пользователя из API
    }

    // Другие методы...
}

// datasource/local/UserLocalDataSource.kt
class UserLocalDataSource(
    private val settings: MultiplatformSettings
) {
    fun saveUser(user: User) {
        // Сохранение пользователя локально
    }

    // Другие методы...
}
```
