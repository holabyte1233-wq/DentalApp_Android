package unab.edu.co.abrahamcaceres.dentalapp_android.domain.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Boolean>
}
