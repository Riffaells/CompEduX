package base

import config.NetworkConfig
import io.ktor.client.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.io.IOException
import logging.Logger
import model.DomainError
import model.DomainResult
import kotlin.math.pow
import kotlin.random.Random

/**
 * Base class for network API implementations with common functionality
 */
abstract class BaseNetworkApi(
    protected val client: HttpClient,
    protected val networkConfig: NetworkConfig,
    protected val logger: Logger
) {
    /**
     * Maximum number of retry attempts for network operations
     */
    protected val maxRetries = 3

    /**
     * Base delay in milliseconds for retry backoff strategy
     */
    protected val baseRetryDelayMs = 1000L

    /**
     * Jitter factor for random jitter in retry backoff strategy
     */
    protected val jitterFactor = 0.25

    /**
     * Gets the full API URL from configuration
     * @return complete API base URL as string
     */
    protected suspend fun getApiUrl(): String {
        return networkConfig.getFullApiUrl()
    }

    /**
     * Processes API exceptions and converts them to appropriate DomainError
     *
     * @param e The exception to process
     * @param errorTag A tag to identify the error context in logs
     * @return DomainResult.Error with appropriate error details
     */
    protected fun processApiException(e: Exception, errorTag: String): DomainResult.Error {
        logger.e("$errorTag error", e)
        return when (e) {
            is ClientRequestException -> DomainResult.Error(
                DomainError.fromServerCode(
                    serverCode = e.response.status.value,
                    message = e.message,
                    details = null
                )
            )

            is ServerResponseException -> DomainResult.Error(
                DomainError.serverError(
                    message = "error_server_unavailable",
                    details = e.message
                )
            )

            is IOException, is ConnectTimeoutException, is SocketTimeoutException -> DomainResult.Error(
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

    /**
     * Executes a network operation with retry mechanism for handling transient errors.
     * Uses exponential backoff strategy with random jitter to avoid thundering herd problem.
     *
     * @param T The expected return type of the operation.
     * @param operation The suspend function to execute with retry capability.
     * @return The result of the operation if successful.
     */
    protected suspend inline fun <T> executeWithRetry(
        crossinline operation: suspend () -> DomainResult<T>
    ): DomainResult<T> {
        var attempt = 0
        var lastException: Exception? = null

        while (attempt < maxRetries) {
            try {
                // Execute the operation and return result if successful
                return operation()
            } catch (e: CancellationException) {
                // Don't retry if the coroutine was cancelled - return error instead of throwing
                return DomainResult.Error(
                    DomainError.unknownError(
                        message = "error_operation_cancelled",
                        details = e.message
                    )
                )
            } catch (e: ClientRequestException) {
                // Don't retry for client errors (4xx) as they are typically not transient
                if (e.response.status.value !in 408..499) {
                    logger.e("Non-retryable client error: ${e.message}", e)
                    return DomainResult.Error(
                        DomainError.fromServerCode(
                            serverCode = e.response.status.value,
                            message = e.message,
                            details = null
                        )
                    )
                }
                lastException = e
                logger.w("Client error detected. Will retry.", e)
            } catch (e: ServerResponseException) {
                // Server errors (5xx) may be transient, so we'll retry
                lastException = e
                logger.w("Server error detected. Will retry.", e)
            } catch (e: HttpRequestTimeoutException) {
                // Timeout errors are typically transient, so we'll retry
                lastException = e
                logger.w("Request timeout detected. Will retry.", e)
            } catch (e: IOException) {
                // I/O errors (network issues) are typically transient, so we'll retry
                lastException = e
                logger.w("Network connectivity issue detected: ${e.message}. Will retry.", e)
            } catch (e: ConnectTimeoutException) {
                // Connection timeout, likely connectivity issue
                lastException = e
                logger.w("Connection timeout detected. Will retry.", e)
            } catch (e: SocketTimeoutException) {
                // Socket timeout, likely connectivity issue
                lastException = e
                logger.w("Socket timeout detected. Will retry.", e)
            } catch (e: Exception) {
                // For any other unexpected exceptions
                logger.e("Unexpected error during network operation: ${e.message}", e)
                return DomainResult.Error(
                    DomainError.unknownError(
                        message = "error_unknown",
                        details = e.message
                    )
                )
            }

            // Log the retry attempt
            attempt++
            if (attempt < maxRetries) {
                // Calculate delay with exponential backoff and jitter
                val jitter = Random.nextDouble(-jitterFactor, jitterFactor)
                val delayWithJitter =
                    (baseRetryDelayMs * (2.0.pow(attempt.toDouble())) + (baseRetryDelayMs * jitter)).toLong()
                logger.d("Retry attempt $attempt/$maxRetries after $delayWithJitter ms")
                delay(delayWithJitter)
            } else {
                logger.e("All retry attempts failed", lastException)
            }
        }

        // If we've exhausted all retries, create appropriate error
        val error = when (lastException) {
            is IOException, is ConnectTimeoutException, is SocketTimeoutException, is HttpRequestTimeoutException -> {
                logger.e("Network connectivity issue persisted after all retries", lastException)
                DomainError.networkError(
                    message = "error_network_connectivity",
                    details = lastException?.message
                )
            }

            is ServerResponseException -> {
                logger.e("Server error persisted after all retries", lastException)
                DomainError.serverError(
                    message = "error_server_unavailable",
                    details = lastException.message
                )
            }

            is ClientRequestException -> {
                logger.e("Client error persisted after all retries", lastException)
                DomainError.fromServerCode(
                    serverCode = lastException.response.status.value,
                    message = lastException.message,
                    details = null
                )
            }

            else -> {
                logger.e("Unknown error type persisted after all retries", lastException)
                DomainError.unknownError(
                    message = "error_unknown_network",
                    details = lastException?.message
                )
            }
        }

        // Return error result
        return DomainResult.Error(error)
    }
}
