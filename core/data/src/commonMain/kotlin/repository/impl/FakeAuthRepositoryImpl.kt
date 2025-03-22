package repository.impl

import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import model.AppError
import model.AuthResult
import model.ErrorCode
import model.User
import repository.auth.AuthRepository
import repository.mapper.AuthMapper

/**
 * Фейковая реализация репозитория для работы с аутентификацией
 * Используется для тестирования и разработки UI
 */
class FakeAuthRepositoryImpl : AuthRepository {
    private var isAuthenticated = false
    private var currentUser: User? = null

    // Тестовые учетные данные
    private val testUsers = mapOf(
        "test@test.com" to TestUser("password", "Test User"),
        "admin@test.com" to TestUser("admin", "Admin User")
    )

    override suspend fun login(email: String, password: String): AuthResult = withContext(Dispatchers.Default) {
        Napier.d("FakeAuthRepositoryImpl: login attempt with email=$email")

        // Имитируем задержку сетевого запроса
        delay(1000)

        // Проверяем учетные данные
        val testUser = testUsers[email]

        if (testUser != null && testUser.password == password) {
            // Успешная авторизация
            val user = User(
                id = email.hashCode().toString(),
                email = email,
                username = testUser.username
            )
            currentUser = user
            isAuthenticated = true

            Napier.d("FakeAuthRepositoryImpl: login success for $email")
            AuthResult.Success(user, "fake-token-${System.currentTimeMillis()}")
        } else {
            // Ошибка авторизации
            Napier.d("FakeAuthRepositoryImpl: login failed for $email")
            val error = AppError(
                code = ErrorCode.INVALID_CREDENTIALS,
                message = "Неверный email или пароль",
                details = "Email: $email"
            )
            AuthResult.Error(error)
        }
    }

    override suspend fun register(email: String, password: String, username: String): AuthResult = withContext(Dispatchers.Default) {
        Napier.d("FakeAuthRepositoryImpl: register attempt with email=$email, username=$username")

        // Имитируем задержку сетевого запроса
        delay(1500)

        // Проверяем существование пользователя
        if (testUsers.containsKey(email)) {
            Napier.d("FakeAuthRepositoryImpl: register failed - user already exists")
            return@withContext AuthResult.Error(
                AppError(
                    code = ErrorCode.USER_ALREADY_EXISTS,
                    message = "Пользователь с таким email уже существует",
                    details = "Email: $email"
                )
            )
        }

        // Валидация
        if (password.length < 6) {
            return@withContext AuthResult.Error(
                AppError(
                    code = ErrorCode.VALIDATION_ERROR,
                    message = "Пароль должен содержать не менее 6 символов",
                    details = "Password length: ${password.length}"
                )
            )
        }

        if (!email.contains("@")) {
            return@withContext AuthResult.Error(
                AppError(
                    code = ErrorCode.VALIDATION_ERROR,
                    message = "Некорректный формат email",
                    details = "Email: $email"
                )
            )
        }

        // Создаем нового пользователя
        val user = User(
            id = email.hashCode().toString(),
            email = email,
            username = username
        )
        currentUser = user
        isAuthenticated = true

        Napier.d("FakeAuthRepositoryImpl: register success for $email")
        AuthResult.Success(user, "fake-token-${System.currentTimeMillis()}")
    }

    override suspend fun logout(): AuthResult = withContext(Dispatchers.Default) {
        Napier.d("FakeAuthRepositoryImpl: logout attempt")

        // Имитируем задержку сетевого запроса
        delay(500)

        currentUser = null
        isAuthenticated = false

        Napier.d("FakeAuthRepositoryImpl: logout success")
        AuthResult.Success(null, null)
    }

    override suspend fun getCurrentUser(): User? = withContext(Dispatchers.Default) {
        Napier.d("FakeAuthRepositoryImpl: getCurrentUser called, isAuthenticated=$isAuthenticated")

        if (isAuthenticated) {
            return@withContext currentUser
        } else {
            return@withContext null
        }
    }

    override suspend fun isAuthenticated(): Boolean = withContext(Dispatchers.Default) {
        Napier.d("FakeAuthRepositoryImpl: isAuthenticated check: $isAuthenticated")
        return@withContext isAuthenticated
    }

    override suspend fun updateProfile(username: String): AuthResult = withContext(Dispatchers.Default) {
        Napier.d("FakeAuthRepositoryImpl: updateProfile attempt with username=$username")

        // Имитируем задержку сетевого запроса
        delay(1000)

        if (!isAuthenticated || currentUser == null) {
            Napier.d("FakeAuthRepositoryImpl: updateProfile failed - not authenticated")
            return@withContext AuthResult.Success(null, null)
        }

        // Обновляем имя пользователя
        val updatedUser = currentUser!!.copy(username = username)
        currentUser = updatedUser

        Napier.d("FakeAuthRepositoryImpl: updateProfile success")
        AuthResult.Success(updatedUser, "fake-token-${System.currentTimeMillis()}")
    }

    /**
     * Внутренний класс для хранения тестовых учетных данных
     */
    private data class TestUser(val password: String, val username: String)
}
