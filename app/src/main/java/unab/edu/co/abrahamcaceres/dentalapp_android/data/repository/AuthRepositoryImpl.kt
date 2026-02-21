package unab.edu.co.abrahamcaceres.dentalapp_android.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import unab.edu.co.abrahamcaceres.dentalapp_android.domain.repository.AuthRepository

class AuthRepositoryImpl @Inject constructor(
    private val auth: Auth,
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(email: String, password: String, name: String, phone: String, avatarBytes: ByteArray?): Result<Boolean> {
        return try {
            val result = auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("full_name", name)
                    put("phone", phone)
                }
            }
            // Supabase does NOT throw for existing email - it returns success with empty identities
            if (result != null && result.identities.isNullOrEmpty()) {
                return Result.failure(Exception("User already exists"))
            }
            // Upload avatar if we have session and avatar bytes (stored at userId/avatar.jpg)
            auth.currentUserOrNull()?.id?.let { userId ->
                if (avatarBytes != null && avatarBytes.isNotEmpty()) {
                    try {
                        val filePath = "$userId/avatar.jpg"
                        supabaseClient.storage.from("dental-images").upload(filePath, avatarBytes) { upsert = true }
                    } catch (_: Exception) { /* Avatar upload failed, continue */ }
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            val msg = e.message?.lowercase() ?: ""
            val causeMsg = e.cause?.message?.lowercase() ?: ""
            val isDuplicateUser = msg.contains("user already registered") ||
                msg.contains("already been registered") ||
                msg.contains("user already exists") ||
                causeMsg.contains("user already registered") ||
                causeMsg.contains("already been registered") ||
                causeMsg.contains("user already exists")
            val failureException = if (isDuplicateUser) {
                Exception("User already exists")
            } else {
                Exception(e.message ?: "Sign up failed")
            }
            Result.failure(failureException)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun hasActiveSession(): Boolean {
        auth.awaitInitialization()
        return auth.currentSessionOrNull() != null
    }

    override suspend fun getCurrentUserEmail(): String? {
        auth.awaitInitialization()
        return auth.currentUserOrNull()?.email
    }

    override suspend fun getCurrentUserName(): String? {
        auth.awaitInitialization()
        val metadata = auth.currentUserOrNull()?.userMetadata ?: return null
        return metadata["full_name"]?.toString()
    }

    override suspend fun getCurrentUserPhone(): String? {
        auth.awaitInitialization()
        val metadata = auth.currentUserOrNull()?.userMetadata ?: return null
        return metadata["phone"]?.toString()
    }

    override suspend fun getAvatarUrl(): String? {
        auth.awaitInitialization()
        val userId = auth.currentUserOrNull()?.id ?: return null
        return supabaseClient.storage.from("dental-images").publicUrl("$userId/avatar.jpg")
    }

    override suspend fun updateAvatar(avatarBytes: ByteArray) {
        val userId = auth.currentUserOrNull()?.id ?: return
        val filePath = "$userId/avatar.jpg"
        supabaseClient.storage.from("dental-images").upload(filePath, avatarBytes) { upsert = true }
    }

    override suspend fun updateProfile(name: String, phone: String) {
        auth.updateUser {
            data = buildJsonObject {
                put("full_name", name)
                put("phone", phone)
            }
        }
    }
}
