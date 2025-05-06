# Authentication Module

This module provides a complete authentication solution for the CompEduX application, implemented using the Decompose
library for navigation and MVIKotlin for state management.

## Architecture

The authentication module follows a component-based architecture with clear separation of concerns:

- **AuthComponent**: Main component responsible for navigation between login, registration, and profile screens
- **AuthStore**: Central store for managing authentication state using MVIKotlin
- **NavigationExecutor**: Utility for safe navigation operations across threads

## Components

### AuthComponent

The main entry point for the authentication flow. It manages navigation between three sub-components:

- **LoginComponent**: Handles user login
- **RegisterComponent**: Handles user registration
- **ProfileComponent**: Displays user profile and provides logout functionality

### State Management

Authentication state is managed through `AuthStore`, which provides a reactive state flow that components can observe.
The state includes:

- **isAuthenticated**: Boolean indicating if the user is currently authenticated
- **user**: User information when authenticated
- **isLoading**: Loading state during async operations
- **error**: Error information if authentication fails

## Navigation Flow

The authentication flow works as follows:

1. User starts at the login screen
2. User can navigate to registration or complete login
3. Upon successful authentication, the user is automatically redirected to the profile screen
4. From the profile, the user can log out and return to the login screen

## Thread Safety

This module includes special handling to ensure all navigation operations are performed on the main thread:

- `NavigationExecutor` ensures navigation calls are executed safely
- Coroutine scopes are bound to component lifecycles for automatic cancellation
- IO operations are performed on background threads
- UI updates and navigation are performed on the main thread

## Usage Example

```kotlin
// Create the auth component
val authComponent = AuthComponent(
    componentContext = componentContext,
    onBack = { /* Handle back navigation */ }
)

// Subscribe to authentication state
authComponent.state.collect { state ->
    if (state.isAuthenticated) {
        // User is authenticated
    } else {
        // User is not authenticated
    }
}
```

## Implementation Details

### Thread Management

The module carefully manages threading to prevent `NotOnMainThreadException` errors:

1. All navigation operations are executed synchronously in the calling thread
2. The coroutine scope is tied to the component's lifecycle
3. Background operations use `rDispatchers.io`
4. UI updates and navigation use `rDispatchers.main`
5. `MainThreadWorker` is replaced with direct navigation to avoid dependency issues

### Error Handling

Comprehensive error handling is implemented throughout the module:

- Navigation errors are caught and logged
- Authentication errors are captured in state
- Coroutines use structured concurrency with proper error handling

## Configuration

Authentication endpoints and behavior can be configured through the `AuthUseCases` interface, which is injected into the
`AuthComponent`.
