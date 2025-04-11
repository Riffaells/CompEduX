# Data Module

The data module is responsible for the application's data operations, implementing repository interfaces from the domain module and interacting with network and persistence layers.

## Structure

```
core/data/
├── src/
│   └── commonMain/
│       └── kotlin/
│           ├── api/               # API adapters
│           │   └── auth/          # Authentication API adapters
│           │       └── DataAuthApiAdapter.kt  # Adapter for authentication API
│           │
│           ├── model/             # Data models
│           │   └── auth/          # Authentication models
│           │       └── DataAuthModels.kt  # Request/response models for authentication
│           │
│           ├── repository/        # Repository implementations
│           │   ├── auth/          # Authentication repositories
│           │   │   ├── AuthRepositoryImpl.kt  # Authentication repository implementation
│           │   │   └── TokenRepositoryImpl.kt # Token management implementation
│           │   │
│           │   └── mapper/        # Mappers for transforming between layers
│           │       └── AuthMapper.kt # Mapper for authentication
│           │
│           ├── datasource/        # Data sources
│           │   ├── local/         # Local data sources
│           │   │   └── UserLocalDataSource.kt
│           │   │
│           │   └── remote/        # Remote data sources
│           │       └── UserRemoteDataSource.kt
│           │
│           └── di/                # Dependencies and injections
│               └── DataModule.kt  # Module for providing dependencies
```

## Core Components

### Repository Implementations (repository/)

Implementations of interfaces from the domain layer:

```kotlin
// repository/auth/AuthRepositoryImpl.kt
class AuthRepositoryImpl(
    private val networkAuthApi: NetworkAuthApi,
    private val tokenRepository: TokenRepository,
    private val logger: Logger
) : AuthRepository {

    // Authentication state management
    private val _authState = MutableStateFlow<AuthStateDomain>(AuthStateDomain.Unauthenticated)
    override val authState: StateFlow<AuthStateDomain> = _authState.asStateFlow()

    override suspend fun login(
        email: String,
        password: String
    ): DomainResult<AuthResponseDomain> {
        // Login implementation
        val request = LoginRequestDomain(email, password)
        val result = networkAuthApi.login(request)

        // Process result
        if (result is DomainResult.Success) {
            saveTokens(result.data)
            getCurrentUser()
        }

        return result
    }

    override suspend fun refreshTokenIfNeeded(): Boolean {
        // Token refresh logic with error handling
        val refreshToken = tokenRepository.getRefreshToken() ?: return false

        val result = networkAuthApi.refreshToken(RefreshTokenRequestDomain(refreshToken))
        return when (result) {
            is DomainResult.Success -> {
                saveTokens(result.data)
                true
            }
            else -> false
        }
    }

    // Other methods...
}
```

### API Adapters (api/auth/)

Classes that adapt between domain interfaces and network API:

```kotlin
// api/auth/DataAuthApiAdapter.kt
class DataAuthApiAdapter(
    private val networkAuthApi: NetworkAuthApi,
    private val tokenRepository: TokenRepository,
    private val logger: Logger
) : AuthApi {

    override suspend fun login(
        email: String,
        password: String
    ): DomainResult<AuthResponseDomain> = withContext(Dispatchers.Default) {
        logger.d("DataAuthApiAdapter: login($email, ***)")

        val request = LoginRequestDomain(email, password)
        networkAuthApi.login(request)
    }

    override suspend fun getCurrentUser(): DomainResult<UserDomain> = withContext(Dispatchers.Default) {
        val token = tokenRepository.getAccessToken() ?: return@withContext
            DomainResult.Error(DomainError.authError("No access token found"))

        networkAuthApi.getCurrentUser(token)
    }

    // Other methods...
}
```

### Error Handling

The data layer uses `DomainResult` from the domain layer for consistent error handling:

```kotlin
// Error handling in repositories
override suspend fun getCurrentUser(): DomainResult<UserDomain> {
    // Get the access token
    val accessToken = tokenRepository.getAccessToken()

    if (accessToken == null) {
        logger.w("Cannot get user: No access token")
        _authState.value = AuthStateDomain.Unauthenticated
        return DomainResult.Error(DomainError.authError("Not authorized"))
    }

    // Make the request
    val result = networkAuthApi.getCurrentUser(accessToken)

    // Update the authentication state based on the result
    when (result) {
        is DomainResult.Success -> {
            _authState.value = AuthStateDomain.Authenticated(result.data)
        }
        is DomainResult.Error -> {
            // If the error is authentication-related, try to refresh the token
            if (result.error.isAuthError() && refreshTokenIfNeeded()) {
                return getCurrentUser()
            } else {
                _authState.value = AuthStateDomain.Unauthenticated
            }
        }
        is DomainResult.Loading -> {
            // Do nothing during loading
        }
    }

    return result
}
```

### Dependency Injection (di/)

The data module defines a Kodein DI module for providing dependencies:

```kotlin
// di/DataModule.kt
val dataModule = DI.Module("dataModule") {
    // Repositories and adapters for authentication
    bind<TokenRepository>() with singleton {
        val multiplatformSettings = instance<MultiplatformSettings>()
        val logger = instance<LoggingProvider>().getLogger("TokenRepository")
        TokenRepositoryImpl(multiplatformSettings.security, logger)
    }

    bind<AuthRepository>() with singleton {
        val networkAuthApi = instance<NetworkAuthApi>()
        val tokenRepository = instance<TokenRepository>()
        val logger = instance<LoggingProvider>().getLogger("AuthRepository")
        AuthRepositoryImpl(networkAuthApi, tokenRepository, logger)
    }

    bind<AuthApi>() with singleton {
        val networkAuthApi = instance<NetworkAuthApi>()
        val tokenRepository = instance<TokenRepository>()
        val logger = instance<LoggingProvider>().getLogger("AuthApi")
        DataAuthApiAdapter(networkAuthApi, tokenRepository, logger)
    }

    // Module initialization logging
    val dataLogger = instance<LoggingProvider>().getLogger("DataModule")
    dataLogger.i("Data module initialized")
}
```

## API Layer Abstraction

The data module provides two important implementations:

1. **AuthRepositoryImpl**: Implements the domain's `AuthRepository` interface, using `NetworkAuthApi` for API calls and handling token management internally
2. **DataAuthApiAdapter**: Implements the domain's `AuthApi` interface, providing a higher-level API abstraction on top of `NetworkAuthApi`

This structure allows domain layer components to work with different abstraction levels according to their needs.

## Cross-Platform Compatibility

The data module ensures cross-platform compatibility by:

- Using Kotlin Multiplatform for all code
- Relying on `Dispatchers.Default` instead of platform-specific dispatchers
- Using platform-independent error handling via `DomainResult`
- Abstracting platform-specific storage through `MultiplatformSettings`

## Conclusion

The data module serves as a bridge between the domain layer and the external data sources, implementing the business rules defined in the domain while handling the technical details of data retrieval and storage.
