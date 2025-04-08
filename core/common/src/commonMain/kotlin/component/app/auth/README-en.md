# Authentication Module

The Authentication Module provides user authentication functionality for the CompEduX application. It handles login, registration, and session management using a thread-safe, reactive architecture.

## Architecture

The authentication module follows the MVI (Model-View-Intent) architecture pattern using MVIKotlin and integrates with the Decompose navigation framework. The architecture consists of:

- **Component**: Controls navigation and coordinates UI interactions
- **Store**: Manages state, processes intents, and handles business logic
- **UI**: Renders the current state and sends user actions as intents

## Components

### AuthComponent

The `AuthComponent` is the main coordinator for authentication flows. It:

- Manages navigation between login, registration, and profile screens
- Creates and initializes the authentication store
- Handles transitions between authentication states
- Ensures thread safety during navigation operations

Key features:
- Thread-safe navigation using `MainThreadWorker`
- Lifecycle-aware coroutine management
- State-driven navigation flow
- Error handling for authentication and navigation failures

### AuthStore

The `AuthStore` manages the authentication state and business logic. It:

- Processes authentication intents (login, register, logout)
- Validates user credentials
- Updates authentication state
- Notifies components about state changes

Key features:
- Reactive state management with MVIKotlin
- Thread-safe execution of intents
- Input validation and error handling
- Authentication state persistence

## Navigation Flow

The authentication module supports the following navigation flows:

1. **Login Flow**:
   - Initial entry point
   - Login → Profile (on successful login)
   - Login → Register (on register button click)

2. **Registration Flow**:
   - Register → Login (on successful registration or back)
   - Register → Profile (on successful registration and auto-login)

3. **Logout Flow**:
   - Profile → Login (on logout)

## Thread Safety

The authentication module implements several techniques to ensure thread safety:

1. **MainThreadWorker**: All navigation operations use Decompose's `MainThreadWorker`
2. **Coroutine Dispatchers**: Async operations use appropriate dispatchers
3. **State Flow**: Thread-safe state management with Kotlin Flows
4. **NavigationExecutor**: Centralized navigation management

## Usage Example

```kotlin
// Creating and using the authentication component
val authComponent = AuthComponent(
    componentContext = componentContext,
    rDispatchers = rDispatchers,
    onAuthenticated = { /* Handle successful authentication */ }
)

// Observing authentication state
val authState = authComponent.authStore.state
    .onEach { state ->
        // React to state changes
        if (state.isAuthenticated) {
            // User is authenticated
        }
    }
    .launchIn(scope)
```

## Implementation Details

### Authentication States

The authentication module manages these states:

- **Initial**: First-time app launch, no stored credentials
- **Authenticating**: Authentication in progress
- **Authenticated**: User successfully logged in
- **Unauthenticated**: User logged out or authentication failed
- **Error**: Authentication error with error message

### User Credentials Management

The module handles:
- Username/password validation
- Secure storage of authentication tokens
- Session management
- Automatic token refresh
- Logout and session clearing

## Error Handling

The authentication module implements comprehensive error handling:

- Input validation errors (invalid email, weak password)
- Network errors during authentication
- Server authentication failures
- Thread-related navigation errors
- Session expiration errors

## Integration with Other Modules

The authentication module integrates with:

1. **Profile Module**: Transitions to profile after successful authentication
2. **API Module**: Communicates with authentication endpoints
3. **Storage Module**: Persists authentication tokens and session data
4. **Navigation Module**: Coordinates app-wide navigation based on auth state
