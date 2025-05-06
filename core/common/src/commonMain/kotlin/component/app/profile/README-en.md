# Profile Module

This module manages user profile functionality within the CompEduX application, providing profile viewing, editing, and
related actions.

## Architecture

The profile module follows the MVI (Model-View-Intent) architecture pattern using MVIKotlin and integrates with
Decompose for navigation. Key components include:

- **ProfileComponent**: Main component managing profile screens and navigation
- **ProfileStore**: State management and business logic for profile operations
- **Navigation**: Screen navigation using Decompose's Stack API

## Components

### ProfileComponent

The `ProfileComponent` serves as the entry point for profile-related functionality:

- Profile information display and editing
- Navigation between profile-related screens
- User session management (logout functionality)
- Loading and saving user profile data

Key features:

- Lifecycle-scoped coroutines for safe async operations
- Thread-safe navigation using MainThreadWorker
- Error handling for profile operations and navigation

### ProfileStore

The `ProfileStore` implements MVIKotlin's `Store` interface and handles:

- User profile state (loading, success, error)
- Profile data (user information, preferences, settings)
- Business logic for profile operations (update, refresh, logout)

State transitions:

1. Initial state → Loading state
2. Loading state → Profile data loaded/Error state
3. Edit state → Save operation → Updated profile state

## Navigation Flow

The profile module supports the following navigation flows:

1. **Main Profile Flow**:
    - Profile main screen → Profile edit (on edit action)
    - Profile main screen → Login (on logout action)

2. **Edit Profile Flow**:
    - Edit screen → Profile main screen (on save or cancel)

## Thread Safety

The module implements several strategies to ensure thread safety:

1. **MainThreadWorker**: Ensures navigation operations execute on the main thread
2. **Lifecycle-bound coroutines**: Coroutine scopes tied to component lifecycle
3. **Thread switching**: Background operations on IO threads, UI operations on main thread
4. **Error handling**: Captures and logs all threading-related exceptions

## Usage Example

```kotlin
// Create the profile component
val profileComponent = ProfileComponent(
    componentContext = componentContext,
    navigateToAuth = { /* Navigate to auth */ },
    rDispatchers = rDispatchers
)

// Subscribe to child stack changes
profileComponent.childStack.subscribe { stack ->
    // Update UI based on active child
    val activeChild = stack.active.instance
    // Render appropriate screen
}

// Handle edit profile action
fun onEditProfileClicked() {
    profileComponent.navigateToEdit()
}

// Handle logout action
fun onLogoutClicked() {
    profileComponent.logout()
}
```

## Implementation Details

### Thread Management

Thread management follows best practices:

1. UI events are processed on the main thread
2. Heavy operations (API calls, data processing) run on IO threads
3. State updates are dispatched back to the main thread
4. Navigation is always executed on the main thread using MainThreadWorker

### Error Handling

The module handles errors at multiple levels:

1. **Store level**: Captures business logic errors, updates state with error information
2. **Component level**: Handles navigation errors, provides user feedback
3. **Coroutine level**: Uses exception handlers to prevent app crashes

### User Data Management

The profile module handles user data with:

1. **Data persistence**: Saving profile changes to local storage or remote servers
2. **Data validation**: Ensuring profile updates meet required formats
3. **Session management**: Handling user logout and session expiration

## Integration

The profile module integrates with:

- **Authentication module**: For logout and session verification
- **Storage module**: For storing user profile data
- **Network module**: For synchronizing profile changes with the server
- **Settings module**: For user preferences and application settings
