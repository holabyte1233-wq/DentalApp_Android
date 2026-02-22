package unab.edu.co.abrahamcaceres.dentalapp_android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton
import unab.edu.co.abrahamcaceres.dentalapp_android.data.remote.MockSmileAnalyzer
import unab.edu.co.abrahamcaceres.dentalapp_android.data.remote.SmileAnalyzer
import unab.edu.co.abrahamcaceres.dentalapp_android.data.remote.GeminiService

/**
 * Provides SmileAnalyzer: GeminiService when GEMINI_API_KEY is configured, MockSmileAnalyzer otherwise.
 */
@Module
@InstallIn(SingletonComponent::class)
object SmileAnalyzerModule {

    @Provides
    @Singleton
    fun provideSmileAnalyzer(
        mock: MockSmileAnalyzer,
        gemini: GeminiService,
        @Named("gemini_active") isGeminiActive: Boolean
    ): SmileAnalyzer = if (isGeminiActive) gemini else mock
}
