# Модуль Domain

Модуль domain является ядром реализации Чистой Архитектуры в CompEduX. Он содержит бизнес-логику, модели сущностей и интерфейсы, определяющие бизнес-правила приложения.

## Структура

Модуль организован в несколько пакетов:

- **api** - Интерфейсы для внешнего API-взаимодействия
  - **auth** - API-интерфейсы, связанные с аутентификацией (AuthApi, NetworkAuthApi)
- **config** - Интерфейсы конфигурации для различных сервисов
- **di** - Модули внедрения зависимостей для доменного слоя
- **model** - Доменные сущности, представляющие бизнес-объекты
- **repository** - Интерфейсы репозиториев для доступа к данным
- **usecase** - Сценарии использования, реализующие бизнес-логику

## Ключевые компоненты

### Модели

Модуль определяет несколько доменных моделей, представляющих бизнес-сущности:

- `UserDomain` - Информация о пользователе
- `AuthResponseDomain` - Ответ аутентификации с токенами
- `DomainResult<T>` - Обобщенная обертка для результатов операций с обработкой успеха/ошибки

### Обработка результатов

Класс `DomainResult<T>` - универсальная обертка для результатов операций:

```kotlin
sealed class DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>()
    data class Error(val error: DomainError) : DomainResult<Nothing>()
    data object Loading : DomainResult<Nothing>()

    // Вспомогательные методы для работы с результатами
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    // Функции-расширения для удобной обработки
    fun <R> map(transform: (T) -> R): DomainResult<R> { ... }
    inline fun onSuccess(action: (T) -> Unit): DomainResult<T> { ... }
    inline fun onError(action: (DomainError) -> Unit): DomainResult<T> { ... }
    inline fun onLoading(action: () -> Unit): DomainResult<T> { ... }
}
```

Этот подход обеспечивает единообразную обработку ошибок во всех слоях приложения.

### Репозитории

Интерфейсы репозиториев определяют контракты доступа к данным:

- `AuthRepository` - Операции аутентификации (вход, регистрация и т.д.)
- `TokenRepository` - Управление токенами аутентификации

### Обработка ошибок

Комплексная система обработки ошибок:

- `DomainError` - Инкапсулирует информацию об ошибке с вспомогательными методами:
  ```kotlin
  data class DomainError(
      val code: ErrorCode,
      val message: String,
      val details: String? = null
  ) {
      // Проверка, связана ли ошибка с аутентификацией
      fun isAuthError(): Boolean { ... }

      // Фабричные методы
      companion object {
          fun networkError(...): DomainError { ... }
          fun serverError(...): DomainError { ... }
          fun authError(...): DomainError { ... }
          fun validationError(...): DomainError { ... }
          // Другие фабричные методы...
      }
  }
  ```
- `ErrorCode` - Перечисление возможных типов ошибок (NETWORK_ERROR, UNAUTHORIZED и т.д.)

## Архитектура

Модуль domain следует следующим архитектурным принципам:

1. **Правило зависимостей**: Domain не зависит от других модулей
2. **Разделение интерфейсов**: Интерфейсы определены для всех внешних зависимостей
3. **Ориентация на сценарии использования**: Бизнес-логика организована вокруг сценариев использования
4. **Чистый Kotlin**: Использует только код, совместимый с Kotlin Multiplatform

## Слой API

Модуль domain определяет два основных API-интерфейса:

1. **NetworkAuthApi**: Низкоуровневый интерфейс для прямого взаимодействия с API, требует ручного управления токенами
2. **AuthApi**: Высокоуровневый интерфейс для аутентификации, абстрагирующий управление токенами

Это разделение позволяет использовать разные уровни абстракции в приложении.

## Использование

Модуль domain используется модулем data для реализации репозиториев и компонентами UI для взаимодействия с бизнес-логикой:

```kotlin
// Пример реализации сценария использования
class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): DomainResult<AuthResponseDomain> {
        // Логика валидации
        if (email.isBlank()) {
            return DomainResult.Error(DomainError.validationError("Email не может быть пустым"))
        }

        // Делегирование репозиторию
        return authRepository.login(email, password)
    }
}

// Пример взаимодействия UI
val loginResult = loginUseCase(email, password)
loginResult.onSuccess { authResponse ->
    // Обработка успешного входа
}.onError { error ->
    // Обработка ошибки
}
```

## Зависимости

Модуль domain имеет минимальные зависимости:

- **kotlinx-coroutines-core** - Для корутин и Flow
- **core:utils** - Для утилит и логирования

## Тестирование

Модуль domain разработан для простого тестирования:

- Интерфейсы репозиториев могут быть заменены моками
- Сценарии использования могут быть протестированы изолированно
- Доменные модели могут быть легко созданы для тестирования
- DomainResult делает обработку ошибок предсказуемой в тестах
