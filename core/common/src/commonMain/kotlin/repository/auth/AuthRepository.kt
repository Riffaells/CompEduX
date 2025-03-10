package repository.auth

interface AuthRepository {
    suspend fun login(username: String, password: String): Boolean
    suspend fun register(username: String, email: String, password: String): Boolean
    suspend fun logout()
    suspend fun isAuthenticated(): Boolean
    suspend fun getCurrentUser(): User?
    suspend fun updateProfile(username: String, email: String): Boolean
}

data class User(
    val id: String,
    val username: String,
    val email: String
)
