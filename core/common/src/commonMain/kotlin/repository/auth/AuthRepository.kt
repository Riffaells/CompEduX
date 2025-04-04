package repository.auth

import model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): User
    suspend fun register(email: String, password: String): User
    suspend fun getUser(): User
    suspend fun updateProfile(username: String)
    suspend fun logout()
    suspend fun isAuthenticated(): Boolean
}
