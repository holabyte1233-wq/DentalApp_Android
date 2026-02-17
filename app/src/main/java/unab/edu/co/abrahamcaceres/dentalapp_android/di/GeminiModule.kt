package unab.edu.co.abrahamcaceres.dentalapp_android.di

import com.google.ai.client.generativeai.GenerativeModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton
import unab.edu.co.abrahamcaceres.dentalapp_android.BuildConfig

@Module
@InstallIn(SingletonComponent::class)
object GeminiModule {

    @Provides
    @Singleton
    @Named("gemini_active")
    fun provideIsGeminiActive(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotBlank() &&
            !key.contains("pega-tu-clave", ignoreCase = true) &&
            !key.contains("your-api-key", ignoreCase = true)
    }

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        val key = BuildConfig.GEMINI_API_KEY
        val safeKey = if (key.isBlank() || key.contains("pega-tu-clave", ignoreCase = true) || key.contains("your-api-key", ignoreCase = true)) {
            "demo-key-placeholder"
        } else {
            key
        }
        return GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = safeKey
        )
    }
}
