# CompEduX - Educational Platform

CompEduX is a multiplatform educational application built with Kotlin Multiplatform. It provides a unified codebase for Android, iOS, and web applications, sharing business logic while maintaining native UI experiences.

## Project Structure

The project follows Clean Architecture principles and is organized into several modules:

- **core:domain** - Contains business logic, entities, and interfaces
- **core:utils** - Utility classes, logging, and common functionality
- **core:network** - API client, network communication, and data transfer objects
- **core:data** - Repositories implementation and data sources
- **app** - Platform-specific applications (Android, iOS, Web)

## Key Features

- **Multiplatform Support**: Single codebase runs on multiple platforms
- **Clean Architecture**: Separation of concerns with dependency inversion
- **Dependency Injection**: Uses Kodein for DI across all modules
- **Reactive Programming**: Flow-based reactive architecture
- **Cross-Platform Networking**: HTTP client with Ktor
- **Logging System**: Unified logging across all platforms

## Technologies

- **Kotlin Multiplatform** - For cross-platform development
- **Kodein** - Dependency injection framework
- **Ktor** - HTTP client for API communication
- **Kotlinx Serialization** - JSON serialization/deserialization
- **Kotlinx Coroutines** - Asynchronous programming
- **Kotlinx DateTime** - Date/time handling
- **Napier** - Logging framework

## Getting Started

### Prerequisites

- JDK 11 or higher
- Kotlin 1.8 or higher
- Android Studio/IntelliJ IDEA

### Setup

1. Clone the repository:
```
git clone https://github.com/yourusername/CompEduX.git
```

2. Open the project in Android Studio or IntelliJ IDEA

3. Build the project:
```
./gradlew build
```

### Running

- **Android**: Run the Android app configuration
- **iOS**: Build the iOS framework and run in Xcode
- **Web**: Build and serve the JS application

## Architecture

### Module Dependencies

```
app (UI layer)
 │
 ├─ core:data (Repository implementations)
 │     │
 │     ├─ core:network (API client)
 │     │      │
 │     │      └─ core:utils (Utilities)
 │     │
 │     └─ core:domain (Business logic)
 │            │
 │            └─ core:utils (Utilities)
 │
 └─ core:utils (Utilities)
```

## Contributing

1. Fork the project
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.


`./gradlew runHot`