package repository

import kotlinx.coroutines.flow.StateFlow
import model.DomainResult
import model.UserDomain
import model.auth.AuthResponseDomain
import model.auth.AuthStateDomain
import model.auth.ServerStatusDomain

/**
 * Interface for authentication repository
 */
interface AuthRepository {
    /**
     * Current authentication state
     */
    val authState: StateFlow<AuthStateDomain>

    /**
     * Register a new user
     * @param username the username
     * @param email the email address
     * @param password the password
     * @return operation result with authentication data
     */
    suspend fun register(
        username: String,
        email: String,
        password: String
    ): DomainResult<AuthResponseDomain>

    /**
     * Login to the system
     * @param email the email address
     * @param password the password
     * @return operation result with authentication data
     */
    suspend fun login(
        email: String,
        password: String
    ): DomainResult<AuthResponseDomain>

    /**
     * Logout from the system
     * @return operation result
     */
    suspend fun logout(): DomainResult<Unit>

    /**
     * Get current user information
     * @return current user or null if user is not authenticated
     */
    suspend fun getCurrentUser(): DomainResult<UserDomain>

    /**
     * Check if user is authenticated
     * @return true if user is authenticated, false otherwise
     */
    suspend fun isAuthenticated(): Boolean

    /**
     * Update user profile
     * @param username new username
     * @return operation result with user data
     */
    suspend fun updateProfile(username: String): DomainResult<UserDomain>

    /**
     * Check server status
     * @return operation result with server status data
     */
    suspend fun checkServerStatus(): DomainResult<ServerStatusDomain>

    /**
     * Refresh token if needed
     * @return true if token was successfully refreshed or doesn't need refreshing, false otherwise
     */
    suspend fun refreshTokenIfNeeded(): Boolean
}
