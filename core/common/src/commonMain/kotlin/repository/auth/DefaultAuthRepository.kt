package repository.auth

import kotlinx.coroutines.delay
import org.kodein.di.DI
import org.kodein.di.DIAware

class DefaultAuthRepository(
    override val di: DI
) : AuthRepository, DIAware {

    // В реальном приложении здесь будет работа с API или локальным хранилищем
    private var currentUser: User? = null
    private var isLoggedIn = false

    override suspend fun login(username: String, password: String): Boolean {
        // Имитация задержки сети
        delay(1000)

        // Простая проверка для демонстрации
        if (username.isNotBlank() && password.isNotBlank()) {
            currentUser = User(
                id = "user-${System.currentTimeMillis()}",
                username = username,
                email = "$username@example.com"
            )
            isLoggedIn = true
            return true
        }
        return false
    }

    override suspend fun register(username: String, email: String, password: String): Boolean {
        // Имитация задержки сети
        delay(1000)

        // Простая проверка для демонстрации
        if (username.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
            currentUser = User(
                id = "user-${System.currentTimeMillis()}",
                username = username,
                email = email
            )
            isLoggedIn = true
            return true
        }
        return false
    }

    override suspend fun logout() {
        // Имитация задержки сети
        delay(500)

        currentUser = null
        isLoggedIn = false
    }

    override suspend fun isAuthenticated(): Boolean {
        return isLoggedIn
    }

    override suspend fun getCurrentUser(): User? {
        return currentUser
    }

    override suspend fun updateProfile(username: String, email: String): Boolean {
        // Имитация задержки сети
        delay(1000)

        if (!isLoggedIn || currentUser == null) {
            return false
        }

        if (username.isNotBlank() && email.isNotBlank()) {
            currentUser = currentUser?.copy(
                username = username,
                email = email
            )
            return true
        }
        return false
    }
}
