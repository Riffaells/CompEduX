package example

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import model.auth.AuthResult
import usecase.auth.AuthUseCases

/**
 * Пример использования AuthUseCases в компоненте или ViewModel
 */
class AuthExample(
    private val authUseCases: AuthUseCases,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    // Состояние аутентификации
    private var isLoggedIn = false
    private var currentUser: String? = null
    private var error: String? = null

    /**
     * Пример входа в систему
     */
    fun login(username: String, password: String, onResult: (Boolean, String?) -> Unit) {
        coroutineScope.launch {
            when (val result = authUseCases.login(username, password)) {
                is AuthResult.Success -> {
                    isLoggedIn = true
                    currentUser = username
                    error = null
                    onResult(true, null)
                }
                is AuthResult.Error -> {
                    isLoggedIn = false
                    currentUser = null
                    error = result.error.message
                    onResult(false, error)
                }
                is AuthResult.Loading -> {
                    // Обработка состояния загрузки
                }
            }
        }
    }

    /**
     * Пример регистрации
     */
    fun register(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        coroutineScope.launch {
            when (val result = authUseCases.register(username, email, password, confirmPassword)) {
                is AuthResult.Success -> {
                    isLoggedIn = true
                    currentUser = username
                    error = null
                    onResult(true, null)
                }
                is AuthResult.Error -> {
                    isLoggedIn = false
                    currentUser = null
                    error = result.error.message
                    onResult(false, error)
                }
                is AuthResult.Loading -> {
                    // Обработка состояния загрузки
                }
            }
        }
    }

    /**
     * Пример выхода из системы
     */
    fun logout(onResult: (Boolean, String?) -> Unit) {
        coroutineScope.launch {
            when (val result = authUseCases.logout()) {
                is AuthResult.Success -> {
                    isLoggedIn = false
                    currentUser = null
                    error = null
                    onResult(true, null)
                }
                is AuthResult.Error -> {
                    error = result.error.message
                    onResult(false, error)
                }
                is AuthResult.Loading -> {
                    // Обработка состояния загрузки
                }
            }
        }
    }

    /**
     * Пример получения информации о текущем пользователе
     */
    fun getCurrentUser(onResult: (String?, String?) -> Unit) {
        coroutineScope.launch {
            when (val result = authUseCases.getCurrentUser()) {
                is AuthResult.Success -> {
                    val user = result.data
                    currentUser = user.username
                    error = null
                    onResult(currentUser, null)
                }
                is AuthResult.Error -> {
                    error = result.error.message
                    onResult(null, error)
                }
                is AuthResult.Loading -> {
                    // Обработка состояния загрузки
                }
            }
        }
    }

    /**
     * Пример проверки статуса сервера
     */
    fun checkServerStatus(onResult: (Boolean, String?) -> Unit) {
        coroutineScope.launch {
            when (val result = authUseCases.checkServerStatus()) {
                is AuthResult.Success -> {
                    val status = result.data
                    onResult(status.status == "OK", null)
                }
                is AuthResult.Error -> {
                    error = result.error.message
                    onResult(false, error)
                }
                is AuthResult.Loading -> {
                    // Обработка состояния загрузки
                }
            }
        }
    }
}
