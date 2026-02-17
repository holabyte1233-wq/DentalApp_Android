package unab.edu.co.abrahamcaceres.dentalapp_android.data.repository

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import unab.edu.co.abrahamcaceres.dentalapp_android.domain.repository.AuthRepository

class AuthRepositoryImpl @Inject constructor(
    private val auth: Auth
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

    override suspend fun signUp(name: String, email: String, password: String): Result<Boolean> {
        return try {
            val result = auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("full_name", name)
                }
            }
            // Supabase does NOT throw for existing email - it returns success with empty identities
            if (result != null && result.identities.isNullOrEmpty()) {
                return Result.failure(Exception("User already exists"))
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
}
