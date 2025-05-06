# Network Module

The network module provides a cross-platform networking implementation for CompEduX. It handles HTTP requests,
authentication, error handling, and platform-specific configurations.

## Structure

The module is organized into several packages:

- **client** - HTTP client implementation and configuration
- **di** - Dependency injection modules for network components
- **error** - Error handling and mapping
- **interceptor** - HTTP request/response interceptors
- **platform** - Platform-specific implementations
- **logging** - Network logging utilities

## Key Components

### HTTP Client

The module uses Ktor for making HTTP requests:

- `HttpClientFactory` - Creates and configures HTTP clients
- `CoreHttpClient` - Base implementation of HTTP client operations
- `Platform` - Platform-specific information for header decoration

### Interceptors

Request/response interceptors for common operations:

- `AuthInterceptor` - Adds authentication tokens to requests
- `NetworkStatusInterceptor` - Monitors network connectivity
- `LoggingInterceptor` - Logs requests and responses

### Error Handling

Comprehensive error handling system:

- `NetworkError` - Represents network-related errors
- `NetworkErrorMapper` - Maps HTTP errors to domain errors

## Configuration

The network module supports various configurations:

```kotlin
// Example of HTTP client configuration
val httpClient = HttpClientFactory(
    baseUrl = "https://api.example.com",
    tokenRepository = tokenRepository,
    networkStatusProvider = networkStatusProvider,
    loggingEnabled = true
).create()
```

## Platform-Specific Implementations

The module uses Kotlin Multiplatform's `expect/actual` pattern to provide platform-specific implementations:

- Android-specific implementations
- iOS-specific implementations
- JavaScript/Wasm-specific implementations

## Logging

Network logging is configurable:

- `NetworkLogger` - Interface for network logging
- `DefaultNetworkLogger` - Default implementation of network logging

## Usage

The network module is primarily used by the data module to implement API services:

```kotlin
// Example of using the HTTP client
class ApiService(private val httpClient: CoreHttpClient) {
    suspend fun getUser(): UserDto {
        return httpClient.get("users/me")
    }

    suspend fun login(email: String, password: String): AuthResponseDto {
        return httpClient.post(
            endpoint = "auth/login",
            body = LoginRequest(email, password)
        )
    }
}
```

## Dependencies

- **Ktor** - HTTP client
- **kotlinx.serialization** - JSON serialization/deserialization
- **core:domain** - For domain interfaces
- **core:utils** - For utilities and logging
