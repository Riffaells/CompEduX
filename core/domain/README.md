# Domain Module

The domain module is the core of the Clean Architecture implementation in CompEduX. It contains business logic, entity models, and interfaces that define the business rules of the application.

## Structure

The module is organized into several packages:

- **api** - Interfaces for external API communication
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

### Repositories

Repository interfaces define data access contracts:

- `AuthRepository` - Authentication operations (login, register, etc.)
- `TokenRepository` - Management of authentication tokens

### Error Handling

Comprehensive error handling framework:

- `DomainError` - Encapsulates error information
- `ErrorCode` - Enumeration of possible error types

## Architecture

The domain module follows these architectural principles:

1. **Dependencies Rule**: Domain doesn't depend on any other modules
2. **Interface Segregation**: Interfaces are defined for all external dependencies
3. **Use-Case Driven**: Business logic is organized around use cases
4. **Pure Kotlin**: Uses only Kotlin Multiplatform compatible code

## Usage

The domain module is used by the data module to implement repositories and by UI components to interact with business logic:

```kotlin
// Example of a use case implementation
class LoginUseCaseImpl(
    private val authRepository: AuthRepository
) : LoginUseCase {
    override suspend fun execute(email: String, password: String): DomainResult<AuthResponseDomain> {
        return authRepository.login(email, password)
    }
}

// Example of UI interaction
val loginResult = loginUseCase.execute(email, password)
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
