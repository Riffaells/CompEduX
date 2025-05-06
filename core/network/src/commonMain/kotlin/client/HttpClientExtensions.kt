package client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.io.IOException
import logging.Logger
import model.DomainError
import model.DomainResult
import model.auth.NetworkErrorResponse

/**
 * Extension function for HttpClient that handles error cases and converts them to DomainResult
 * This makes API implementation code much cleaner by centralizing error handling
 */
suspend inline fun <reified T> HttpClient.safeSend(
    crossinline requestBuilder: HttpRequestBuilder.() -> Unit,
    logger: Logger,
    errorTransformer: (HttpResponse) -> DomainError = { response ->
        DomainError.fromServerCode(
            serverCode = response.status.value,
            message = "error_http_${response.status.value}",
            details = null
        )
    }
): DomainResult<T> {
    return try {
        val response = request {
            requestBuilder()
        }

        // Log successful request
        logger.d("Request ${response.request.method.value} ${response.request.url} completed with status: ${response.status.value}")

        if (response.status.isSuccess()) {
            // Parse successful response
            val body = response.body<T>()
            DomainResult.Success(body)
        } else {
            // Handle error response with status code but valid body
            logger.w("Request failed with status: ${response.status.value}")
            DomainResult.Error(errorTransformer(response))
        }
    } catch (e: ClientRequestException) {
        // Client errors (4xx)
        logger.e("Client error during request: ${e.response.status.value}", e)
        DomainResult.Error(
            errorTransformer(e.response)
        )

        // Server errors (5xx)
        logger.e("Server error during request: ${e.response.status.value}", e)
        DomainResult.Error(
            DomainError.serverError(
                message = "error_server_unavailable",
                details = e.message
            )
        )
    } catch (e: HttpRequestTimeoutException) {
        // Timeout errors
        logger.e("Request timeout: ${e.message}", e)
        DomainResult.Error(
            DomainError.networkError(
                message = "error_timeout",
                details = e.message
            )
        )
    } catch (e: IOException) {
        // Network connectivity issues
        logger.e("Network error: ${e.message}", e)
        DomainResult.Error(
            DomainError.networkError(
                message = "error_network_connectivity",
                details = e.message
            )
        )
    } catch (e: Exception) {
        // Any other unexpected errors
        logger.e("Unexpected error during request: ${e.message}", e)
        DomainResult.Error(
            DomainError.unknownError(
                message = "error_unknown",
                details = e.message
            )
        )
    }
}

/**
 * Extension function for handling cases where API returns an error response object
 * that needs to be parsed into a domain error
 */
suspend inline fun <reified T, reified E> HttpClient.safeSendWithErrorBody(
    crossinline requestBuilder: HttpRequestBuilder.() -> Unit,
    logger: Logger,
    crossinline errorBodyToDomainError: (E) -> DomainError
): DomainResult<T> {
    return try {
        val response = request {
            requestBuilder()
        }

        // Log successful request
        logger.d("Request ${response.request.method.value} ${response.request.url} completed with status: ${response.status.value}")

        if (response.status.isSuccess()) {
            // Parse successful response
            val body = response.body<T>()
            DomainResult.Success(body)
        } else {
            // Parse error body and convert to domain error
            try {
                val errorBody = response.body<E>()
                logger.w("Request failed with status: ${response.status.value}")
                DomainResult.Error(errorBodyToDomainError(errorBody))
            } catch (e: Exception) {
                // Special case for NetworkErrorResponse if deserializing fails for common formats
                if (E::class == NetworkErrorResponse::class) {
                    try {
                        // Try alternative format with just "detail" field
                        val errorText = response.bodyAsText()
                        logger.w("Parsing alternative error format: $errorText")

                        // Create simple error response
                        val errorResponse = NetworkErrorResponse(
                            code = response.status.value,
                            detail = errorText.takeIf { it.isNotBlank() }
                        )
                        val error = errorBodyToDomainError(errorResponse as E)
                        return DomainResult.Error(error)
                    } catch (e2: Exception) {
                        logger.e("Failed to parse alternative error format", e2)
                    }
                }

                // Failed to parse error body
                logger.e("Failed to parse error body", e)
                DomainResult.Error(
                    DomainError.fromServerCode(
                        serverCode = response.status.value,
                        message = "error_http_${response.status.value}",
                        details = e.message
                    )
                )
            }
        }
    } catch (e: Exception) {
        // Handle all exceptions similarly to safeSend
        logger.e("Error during request: ${e.message}", e)
        when (e) {
            is ClientRequestException -> {
                try {
                    val errorBody = e.response.body<E>()
                    DomainResult.Error(errorBodyToDomainError(errorBody))
                } catch (parseException: Exception) {
                    // Special case for NetworkErrorResponse if deserializing fails for common formats
                    if (E::class == NetworkErrorResponse::class) {
                        try {
                            // Try alternative format with just "detail" field
                            val errorText = e.response.bodyAsText()
                            logger.w("Parsing alternative error format for exception: $errorText")

                            // Create simple error response
                            val errorResponse = NetworkErrorResponse(
                                code = e.response.status.value,
                                detail = errorText.takeIf { it.isNotBlank() }
                            )
                            val error = errorBodyToDomainError(errorResponse as E)
                            return DomainResult.Error(error)
                        } catch (e2: Exception) {
                            logger.e("Failed to parse alternative error format for exception", e2)
                        }
                    }

                    DomainResult.Error(
                        DomainError.fromServerCode(
                            serverCode = e.response.status.value,
                            message = "error_http_${e.response.status.value}",
                            details = null
                        )
                    )
                }
            }

            is ServerResponseException -> DomainResult.Error(
                DomainError.serverError(
                    message = "error_server_unavailable",
                    details = e.message
                )
            )

            is HttpRequestTimeoutException -> DomainResult.Error(
                DomainError.networkError(
                    message = "error_timeout",
                    details = e.message
                )
            )

            is IOException -> DomainResult.Error(
                DomainError.networkError(
                    message = "error_network_connectivity",
                    details = e.message
                )
            )

            else -> DomainResult.Error(
                DomainError.unknownError(
                    message = "error_unknown",
                    details = e.message
                )
            )
        }
    }
}
