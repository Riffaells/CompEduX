# Utilities Module

This module provides various utility classes and functions used throughout the CompEduX application.

## NavigationExecutor

The `NavigationExecutor` is a utility class designed to solve threading issues when working with Decompose navigation. It ensures that all navigation operations are executed on the main thread and provides error handling and logging capabilities.

### Key Features

- Safe execution of navigation operations
- Thread-safe navigation between screens
- Error handling for navigation operations
- Asynchronous execution of background operations followed by navigation
- Logging of navigation operations and errors

### Usage

```kotlin
// Create a NavigationExecutor
val navigationExecutor = NavigationExecutor(
    navigation = navigation,
    scope = coroutineScope(rDispatchers.main),
    mainDispatcher = rDispatchers.main,
    logger = { message -> Logger("Navigation").i(message) }
)

// Navigate to a screen
navigationExecutor.navigateTo(Config.Home)

// Push a new screen onto the stack
navigationExecutor.push(Config.Details)

// Navigate back
navigationExecutor.pop()

// Execute a background operation and then navigate
navigationExecutor.executeAsync(
    backgroundOperation = { repository.fetchData() },
    onSuccess = { data ->
        // Navigate based on the result
        navigationExecutor.navigateTo(Config.DataView)
    }
)
```

### Thread Safety

The `NavigationExecutor` ensures thread safety by:

1. **Synchronous Execution**: Navigation operations are executed synchronously in the calling thread
2. **Lifecycle-bound Coroutines**: Uses coroutine scopes bound to component lifecycles
3. **Thread Switching**: Ensures background operations run on IO threads and UI operations on main thread
4. **Error Handling**: Catches and logs all navigation-related exceptions

## RDispatchers

The `RDispatchers` interface provides platform-specific dispatchers for coroutines:

- **main**: Dispatcher for UI operations, equivalent to Dispatchers.Main
- **io**: Dispatcher for IO operations, equivalent to Dispatchers.IO
- **default**: Dispatcher for CPU-intensive operations, equivalent to Dispatchers.Default
- **unconfined**: Dispatcher without thread constraints, equivalent to Dispatchers.Unconfined

### Platform-specific Implementations

The interface has different implementations for each platform:

- **Android**: Uses MainThreadDispatcher for UI operations
- **JVM**: Uses Dispatchers.Main configured with Swing dispatcher
- **iOS**: Uses platform-specific dispatchers
- **Web**: Uses appropriate web-specific dispatchers

## Best Practices

When working with these utilities, follow these best practices:

1. Always use `NavigationExecutor` for navigation operations in components
2. Bind coroutine scopes to component lifecycles using `coroutineScope` from Essenty
3. Use `rDispatchers.io` for background operations and `rDispatchers.main` for UI updates
4. Ensure all navigation operations originate from the main thread
5. Handle errors appropriately in both navigation and asynchronous operations

## Thread-related Issues

If you encounter `NotOnMainThreadException` or similar threading issues:

1. Check that all navigation operations are executed on the main thread
2. Verify that `rDispatchers.main` is correctly configured for the platform
3. Ensure coroutine scopes are properly created and bound to component lifecycles
4. Consider using the synchronous navigation methods of `NavigationExecutor` directly from UI event handlers
