package api.impl

import api.AuthApi
import api.dto.*
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import settings.MultiplatformSettings

/**
 * Реализация API для авторизации
 */
class AuthApiImpl(
    private val client: HttpClient,
    private val baseUrl: String, // Устаревший параметр, используется для совместимости
    private val json: Json,
    private val appSettings: MultiplatformSettings
) : AuthApi {

    // Получаем базовый URL из настроек
    private fun getBaseUrl(): String {
        return runBlocking { appSettings.network.serverUrlFlow.first() }
    }

    override suspend fun login(request: LoginRequest): HttpResponse {
        Napier.d("AuthApiImpl: login request with email=${request.email}")
        val url = "${getBaseUrl()}/auth/login"
        return client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(request))
        }
    }

    override suspend fun register(request: RegisterRequest): HttpResponse {
        Napier.d("AuthApiImpl: register request with email=${request.email}")
        val url = "${getBaseUrl()}/auth/register"
        return client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(request))
        }
    }

    override suspend fun logout(): HttpResponse {
        Napier.d("AuthApiImpl: logout request")
        val url = "${getBaseUrl()}/auth/logout"
        return client.post(url) {
            contentType(ContentType.Application.Json)
        }
    }

    override suspend fun getCurrentUser(): HttpResponse {
        Napier.d("AuthApiImpl: getCurrentUser request")
        val url = "${getBaseUrl()}/auth/me"
        return client.get(url) {
            contentType(ContentType.Application.Json)
        }
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): HttpResponse {
        Napier.d("AuthApiImpl: updateProfile request with username=${request.username}")
        val url = "${getBaseUrl()}/auth/profile"
        return client.put(url) {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(request))
        }
    }
}
