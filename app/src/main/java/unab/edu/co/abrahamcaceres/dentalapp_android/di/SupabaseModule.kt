package unab.edu.co.abrahamcaceres.dentalapp_android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton
import unab.edu.co.abrahamcaceres.dentalapp_android.BuildConfig

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Auth) {
                host = "login-callback"
                scheme = "io.supabase.dentalapp"
            }
            install(Storage)
            install(Postgrest)
        }
    }

    @Provides
    @Singleton
    fun provideAuth(supabaseClient: SupabaseClient): Auth =
        supabaseClient.auth
}
