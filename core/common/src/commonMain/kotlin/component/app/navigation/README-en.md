# Navigation Utilities

This module provides utilities for managing navigation in the CompEduX application, ensuring thread-safe navigation operations and consistent behavior across components.

## Components

### NavigationExecutor

The `NavigationExecutor` is a utility class that handles navigation operations safely, ensuring they are executed on the main thread. It provides methods for:

- Navigation to specific destinations
- Stack operations (push, pop, replace)
- Safe execution of navigation actions

Key features:
- Thread safety using Decompose's `MainThreadWorker`
- Error handling for navigation exceptions
- Consistent API for all navigation operations

## Usage

### Basic Navigation

```kotlin
// Create a NavigationExecutor instance
val navigationExecutor = NavigationExecutor()

// Navigate to a specific destination
navigationExecutor.navigateTo(stack, Config.Profile)

// Push a new destination onto the stack
navigationExecutor.push(stack, Config.Settings)

// Pop the current destination
navigationExecutor.pop(stack)

// Replace the current destination
navigationExecutor.replace(stack, Config.Home)
```

### With Components

```kotlin
// In a component class
class MyComponent(
    componentContext: ComponentContext,
    private val rDispatchers: RDispatchers
) : ComponentContext by componentContext {

    private val navigationExecutor = NavigationExecutor()

    private val stack = childStack(
        source = StackNavigation<Config>(),
        initialConfiguration = Config.Initial,
        handleBackButton = true,
        childFactory = ::createChild
    )

    fun navigateToDetails() {
        navigationExecutor.navigateTo(stack.navigation, Config.Details)
    }

    fun goBack() {
        navigationExecutor.pop(stack.navigation)
    }
}
```

## Thread Safety

The navigation utilities address threading issues by:

1. Ensuring all navigation operations are executed on the main thread
2. Using Decompose's `MainThreadWorker` to schedule navigation actions
3. Capturing and handling exceptions that occur during navigation
4. Providing a consistent API that prevents threading-related errors

## Error Handling

The `NavigationExecutor` implements robust error handling:

- Wraps all navigation operations in try-catch blocks
- Logs detailed error information when navigation fails
- Provides descriptive error messages for debugging
- Prevents application crashes due to navigation errors

## Implementation Details

### MainThreadWorker

The `NavigationExecutor` uses Decompose's `MainThreadWorker` to ensure all navigation operations run on the main thread:

```kotlin
MainThreadWorker().submitTask {
    // Navigation code executed safely on the main thread
    navigation.navigateTo(configuration)
}
```

### Safe Execution

The `execute` method provides a generic way to safely execute any navigation operation:

```kotlin
fun <T> execute(navigation: Navigation<T>, operation: Navigation<T>.() -> Unit) {
    MainThreadWorker().submitTask {
        try {
            navigation.operation()
        } catch (e: Exception) {
            Logger("Navigation").e("Navigation operation failed: ${e.message}")
        }
    }
}
```

## Best Practices

1. **Always use NavigationExecutor**: Never call navigation methods directly
2. **Component-level navigation**: Keep navigation logic within components
3. **State-driven navigation**: Base navigation decisions on state changes
4. **Error logging**: Pay attention to navigation error logs for debugging
5. **Testing**: Test navigation flows with different thread conditions
