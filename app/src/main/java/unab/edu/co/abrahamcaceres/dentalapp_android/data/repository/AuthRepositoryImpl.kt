package unab.edu.co.abrahamcaceres.dentalapp_android.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import javax.inject.Inject
import unab.edu.co.abrahamcaceres.dentalapp_android.domain.repository.AuthRepository

class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
