package repository.impl

import api.AuthApi
import api.dto.*
import io.github.aakira.napier.Napier
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.reflect.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import model.AuthResult
import model.ErrorCode
import model.User
import repository.auth.AuthRepository
import repository.mapper.AuthMapper
import repository.mapper.ErrorMapper
import settings.MultiplatformSettings

/**
 * Реализация репозитория для работы с аутентификацией
 */
class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val appSettings: MultiplatformSettings,
    private val json: Json
) : AuthRepository {

    private var currentUser: User? = null

    override suspend fun login(email: String, password: String): AuthResult = withContext(Dispatchers.Default) {
        try {
            Napier.d("AuthRepositoryImpl: login attempt with email=$email")

            // Возвращаем результат загрузки
            AuthMapper.createLoadingResult()

            // Выполняем запрос к API
            val response = authApi.login(LoginRequest(email, password))

            return@withContext processAuthResponse(response)
        } catch (e: Exception) {
            Napier.e("AuthRepositoryImpl: login error", e)
            val appError = ErrorMapper.mapThrowableToAppError(e)
            AuthMapper.createErrorResult(appError)
        }
    }

    override suspend fun register(email: String, password: String, username: String): AuthResult = withContext(Dispatchers.Default) {
        try {
            Napier.d("AuthRepositoryImpl: register attempt with email=$email, username=$username")

            // Возвращаем результат загрузки
            AuthMapper.createLoadingResult()

            // Выполняем запрос к API
            val response = authApi.register(RegisterRequest(email, password, username))

            return@withContext processAuthResponse(response)
        } catch (e: Exception) {
            Napier.e("AuthRepositoryImpl: register error", e)
            val appError = ErrorMapper.mapThrowableToAppError(e)
            AuthMapper.createErrorResult(appError)
        }
    }

    override suspend fun logout(): AuthResult = withContext(Dispatchers.Default) {
        try {
            Napier.d("AuthRepositoryImpl: logout attempt")

            // Возвращаем результат загрузки
            AuthMapper.createLoadingResult()

            // Выполняем запрос к API
            val response = authApi.logout()

            // Очищаем данные токена
            appSettings.security.clearAuthToken()
            currentUser = null

            return@withContext if (response.status.isSuccess()) {
                AuthMapper.createUnauthenticatedResult()
            } else {
                processErrorResponse(response)
            }
        } catch (e: Exception) {
            Napier.e("AuthRepositoryImpl: logout error", e)

            // Очищаем данные токена при ошибке
            appSettings.security.clearAuthToken()
            currentUser = null

            AuthMapper.createUnauthenticatedResult()
        }
    }

    override suspend fun getCurrentUser(): User? = withContext(Dispatchers.Default) {
        // Если есть кэшированный пользователь, возвращаем его
        if (currentUser != null) {
            return@withContext currentUser
        }

        // Если нет токена, то возвращаем null
        if (!appSettings.security.hasAuthToken()) {
            return@withContext null
        }

        try {
            Napier.d("AuthRepositoryImpl: getCurrentUser attempt")

            // Выполняем запрос к API
            val response = authApi.getCurrentUser()

            if (response.status.isSuccess()) {
                val userResponse = response.body<UserResponse>()
                val user = AuthMapper.mapUserResponseToUser(userResponse)

                // Сохраняем пользователя в кэше
                currentUser = user

                return@withContext user
            } else {
                // Если ошибка авторизации, очищаем токен
                if (response.status == HttpStatusCode.Unauthorized) {
                    appSettings.security.clearAuthToken()
                }

                Napier.e("AuthRepositoryImpl: getCurrentUser error - status ${response.status}")
                return@withContext null
            }
        } catch (e: Exception) {
            Napier.e("AuthRepositoryImpl: getCurrentUser error", e)
            return@withContext null
        }
    }

    override suspend fun isAuthenticated(): Boolean = withContext(Dispatchers.Default) {
        val hasToken = appSettings.security.hasAuthToken()

        Napier.d("AuthRepositoryImpl: isAuthenticated check: $hasToken")

        return@withContext hasToken
    }

    override suspend fun updateProfile(username: String): AuthResult = withContext(Dispatchers.Default) {
        try {
            Napier.d("AuthRepositoryImpl: updateProfile attempt with username=$username")

            // Возвращаем результат загрузки
            AuthMapper.createLoadingResult()

            // Проверяем авторизацию
            if (!appSettings.security.hasAuthToken()) {
                return@withContext AuthMapper.createUnauthenticatedResult()
            }

            // Выполняем запрос к API
            val response = authApi.updateProfile(UpdateProfileRequest(username))

            if (response.status.isSuccess()) {
                val userResponse = response.body<UserResponse>()
                val user = AuthMapper.mapUserResponseToUser(userResponse)

                // Обновляем пользователя в кэше
                currentUser = user

                return@withContext AuthMapper.createSuccessResult(user, appSettings.security.getAuthToken()!!)
            } else {
                return@withContext processErrorResponse(response)
            }
        } catch (e: Exception) {
            Napier.e("AuthRepositoryImpl: updateProfile error", e)
            val appError = ErrorMapper.mapThrowableToAppError(e)
            AuthMapper.createErrorResult(appError)
        }
    }

    /**
     * Обрабатывает ответ авторизации/регистрации
     */
    private suspend fun processAuthResponse(response: HttpResponse): AuthResult {
        return if (response.status.isSuccess()) {
            val authResponse = response.body<AuthResponse>()

            // Сохраняем токен
            appSettings.security.saveAuthToken(authResponse.token)

            // Преобразуем DTO в доменную модель
            val user = AuthMapper.mapUserResponseToUser(authResponse.user)

            // Сохраняем пользователя в кэше
            currentUser = user

            AuthMapper.createSuccessResult(user, authResponse.token)
        } else {
            processErrorResponse(response)
        }
    }

    /**
     * Обрабатывает ответ с ошибкой
     */
    private suspend fun processErrorResponse(response: HttpResponse): AuthResult {
        val appError = ErrorMapper.mapHttpResponseToAppError(response)
        return AuthMapper.createErrorResult(appError)
    }
}
