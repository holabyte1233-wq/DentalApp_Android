package unab.edu.co.abrahamcaceres.dentalapp_android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import unab.edu.co.abrahamcaceres.dentalapp_android.data.remote.MockSmileAnalyzer
import unab.edu.co.abrahamcaceres.dentalapp_android.data.remote.SmileAnalyzer

/**
 * Binds MockSmileAnalyzer as SmileAnalyzer so the app works without GEMINI_API_KEY.
 * No GenerativeModel is created - avoids crashes when navigating to New Design.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SmileAnalyzerModule {

    @Binds
    @Singleton
    abstract fun bindSmileAnalyzer(impl: MockSmileAnalyzer): SmileAnalyzer
}
