# Модуль Network

Модуль network предоставляет кроссплатформенную реализацию сетевого взаимодействия для CompEduX. Он обрабатывает HTTP-запросы, аутентификацию, обработку ошибок и платформенно-специфические конфигурации.

## Структура

Модуль организован в несколько пакетов:

- **client** - Реализация и конфигурация HTTP-клиента
- **di** - Модули внедрения зависимостей для сетевых компонентов
- **error** - Обработка и преобразование ошибок
- **interceptor** - Перехватчики HTTP-запросов/ответов
- **platform** - Платформенно-специфические реализации
- **logging** - Утилиты для логирования сетевых операций

## Ключевые компоненты

### HTTP-клиент

Модуль использует Ktor для выполнения HTTP-запросов:

- `HttpClientFactory` - Создает и конфигурирует HTTP-клиенты
- `CoreHttpClient` - Базовая реализация операций HTTP-клиента
- `Platform` - Платформенно-специфическая информация для заголовков запросов

### Перехватчики

Перехватчики запросов/ответов для общих операций:

- `AuthInterceptor` - Добавляет токены аутентификации к запросам
- `NetworkStatusInterceptor` - Отслеживает состояние сетевого подключения
- `LoggingInterceptor` - Логирует запросы и ответы

### Обработка ошибок

Комплексная система обработки ошибок:

- `NetworkError` - Представляет сетевые ошибки
- `NetworkErrorMapper` - Преобразует HTTP-ошибки в доменные ошибки

## Конфигурация

Модуль network поддерживает различные конфигурации:

```kotlin
// Пример конфигурации HTTP-клиента
val httpClient = HttpClientFactory(
    baseUrl = "https://api.example.com",
    tokenRepository = tokenRepository,
    networkStatusProvider = networkStatusProvider,
    loggingEnabled = true
).create()
```

## Платформенно-специфические реализации

Модуль использует шаблон `expect/actual` Kotlin Multiplatform для предоставления платформенно-специфических реализаций:

- Реализации для Android
- Реализации для iOS
- Реализации для JavaScript/Wasm

## Логирование

Сетевое логирование настраиваемое:

- `NetworkLogger` - Интерфейс для сетевого логирования
- `DefaultNetworkLogger` - Стандартная реализация сетевого логирования

## Использование

Модуль network в основном используется модулем data для реализации API-сервисов:

```kotlin
// Пример использования HTTP-клиента
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

## Зависимости

- **Ktor** - HTTP-клиент
- **kotlinx.serialization** - Сериализация/десериализация JSON
- **core:domain** - Для доменных интерфейсов
- **core:utils** - Для утилит и логирования
