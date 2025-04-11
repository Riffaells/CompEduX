# Utils Module

The utils module provides common utilities and functionality shared across all other modules in the CompEduX application.

## Components

### Platform Utilities

Platform-specific detection and information:

- `Platform` - Provides platform-specific details with `expect/actual` implementation
- `PlatformInfo` - Helper class with properties like `isAndroid`, `isWasm`, etc.

```kotlin
// Usage example
if (PlatformInfo.isWasm) {
    // Web-specific code
} else if (PlatformInfo.isAndroid) {
    // Android-specific code
}
```

### Logging System

Cross-platform logging capabilities:

- `Logger` - Core interface for logging with different levels
- `NapierLogger` - Implementation using Napier library
- `LoggingProvider` - Factory for creating loggers

```kotlin
// Get a logger
val logger = LoggingProvider.getLogger("Network")
// Or for a class
val logger = LoggingProvider.getLogger<MyClass>()

// Log at different levels
logger.d("Debug message")
logger.i("Info message")
logger.e("Error message", exception)
```

### Date & Time Utilities

Extensions for working with date and time:

- Formatting dates and times
- Duration calculations
- Date comparison utilities

```kotlin
// Format a date
val date = LocalDate(2023, 5, 15)
val formatted = date.format() // "15.05.2023"

// Format time duration
val elapsed = 3723456L // ms
val timeFormat = elapsed.formatAsTime() // "01:02:03.456"
```

### String Utilities

Text processing and validation:

- Email and username validation
- Text truncation and formatting
- Number formatting

```kotlin
// Validate email
val isValid = "user@example.com".isValidEmail() // true

// Truncate text
val shortened = "This is a long text".truncate(10) // "This is a..."
```

### BuildConfig

Auto-generated configuration constants:

- `APP_NAME` - Application name from libs.versions.toml
- `APP_VERSION` - Application version from libs.versions.toml
- `APP_PROJECT` - Project identifier from libs.versions.toml
- `APP_PACKAGE` - Application package from libs.versions.toml
- `BUILD_TIMESTAMP` - Build timestamp in milliseconds
- `BUILD_DATE` - Human-readable build date
- `DEBUG` - Flag indicating if the build is in debug mode

```kotlin
// Access application version
val version = BuildConfig.APP_VERSION

// Get build timestamp
val buildTime = BuildConfig.BUILD_TIMESTAMP

// Check if running in debug mode
if (BuildConfig.DEBUG) {
    // Debug-only code
}
```

## Supported Platforms

The module works across all supported platforms:

- JVM (Desktop)
- Android
- iOS
- WASM (Browser)

## Integration

Utils module is a dependency for other modules and doesn't depend on any business logic:

```
app
 │
 ├─ feature modules
 │     │
 │     └─ core modules
 │           │
 │           └─ core:utils
 │
 └─ core:utils
```
