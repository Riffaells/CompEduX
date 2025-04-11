# Domain Module

The domain module is the core of the Clean Architecture implementation in CompEduX. It contains business logic, entity models, and interfaces that define the business rules of the application.

## Structure

The module is organized into several packages:

- **api** - Interfaces for external API communication
  - **auth** - Authentication-related API interfaces (AuthApi, NetworkAuthApi)
- **config** - Configuration interfaces for various services
- **di** - Dependency injection modules for domain layer
- **model** - Domain entities that represent business objects
- **repository** - Repository interfaces for data access
- **usecase** - Use cases that implement business logic

## Key Components

### Models

The module defines several domain models representing business entities:

- `UserDomain` - User information
- `AuthResponseDomain` - Authentication response with tokens
- `DomainResult<T>` - Generic wrapper for operation results with success/error handling

### Result Handling

The `DomainResult<T>` class is a generic wrapper for operation results:

```kotlin
sealed class DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>()
    data class Error(val error: DomainError) : DomainResult<Nothing>()
    data object Loading : DomainResult<Nothing>()

    // Helper methods for working with results
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    // Extension functions for easier handling
    fun <R> map(transform: (T) -> R): DomainResult<R> { ... }
    inline fun onSuccess(action: (T) -> Unit): DomainResult<T> { ... }
    inline fun onError(action: (DomainError) -> Unit): DomainResult<T> { ... }
    inline fun onLoading(action: () -> Unit): DomainResult<T> { ... }
}
```

This approach allows for consistent error handling across the application layers.

### Repositories

Repository interfaces define data access contracts:

- `AuthRepository` - Authentication operations (login, register, etc.)
- `TokenRepository` - Management of authentication tokens

### Error Handling

Comprehensive error handling framework:

- `DomainError` - Encapsulates error information with helper methods:
  ```kotlin
  data class DomainError(
      val code: ErrorCode,
      val message: String,
      val details: String? = null
  ) {
      // Check if error is authentication-related
      fun isAuthError(): Boolean { ... }

      // Factory methods
      companion object {
          fun networkError(...): DomainError { ... }
          fun serverError(...): DomainError { ... }
          fun authError(...): DomainError { ... }
          fun validationError(...): DomainError { ... }
          // More factory methods...
      }
  }
  ```
- `ErrorCode` - Enumeration of possible error types (NETWORK_ERROR, UNAUTHORIZED, etc.)

## Architecture

The domain module follows these architectural principles:

1. **Dependencies Rule**: Domain doesn't depend on any other modules
2. **Interface Segregation**: Interfaces are defined for all external dependencies
3. **Use-Case Driven**: Business logic is organized around use cases
4. **Pure Kotlin**: Uses only Kotlin Multiplatform compatible code

## API Layer

The domain module defines two main API interfaces:

1. **NetworkAuthApi**: Low-level interface for direct API communication, requires manual token management
2. **AuthApi**: High-level interface for authentication that abstracts token management

This separation allows for different levels of abstraction in the application.

## Usage

The domain module is used by the data module to implement repositories and by UI components to interact with business logic:

```kotlin
// Example of a use case implementation
class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): DomainResult<AuthResponseDomain> {
        // Validation logic
        if (email.isBlank()) {
            return DomainResult.Error(DomainError.validationError("Email cannot be empty"))
        }

        // Forward to repository
        return authRepository.login(email, password)
    }
}

// Example of UI interaction
val loginResult = loginUseCase(email, password)
loginResult.onSuccess { authResponse ->
    // Handle successful login
}.onError { error ->
    // Handle error
}
```

## Dependencies

The domain module has minimal dependencies:

- **kotlinx-coroutines-core** - For coroutines and Flow
- **core:utils** - For utilities and logging

## Testing

The domain module is designed to be easily testable:

- Repository interfaces can be mocked
- Use cases can be tested in isolation
- Domain models can be easily instantiated for testing
- DomainResult makes error handling predictable in tests
