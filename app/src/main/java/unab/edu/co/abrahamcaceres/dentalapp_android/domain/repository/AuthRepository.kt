package unab.edu.co.abrahamcaceres.dentalapp_android.domain.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Boolean>
    suspend fun signUp(email: String, password: String, name: String, phone: String, avatarBytes: ByteArray?): Result<Boolean>
    suspend fun signOut()
    suspend fun hasActiveSession(): Boolean
    suspend fun getCurrentUserEmail(): String?
    suspend fun getCurrentUserName(): String?
    suspend fun getCurrentUserPhone(): String?
    suspend fun getAvatarUrl(): String?
    suspend fun updateProfile(name: String, phone: String)
    suspend fun updateAvatar(avatarBytes: ByteArray)
}
