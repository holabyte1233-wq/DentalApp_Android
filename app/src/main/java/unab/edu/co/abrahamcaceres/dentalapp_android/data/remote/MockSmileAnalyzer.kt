package unab.edu.co.abrahamcaceres.dentalapp_android.data.remote

import android.graphics.Bitmap
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

/**
 * Mock implementation when GEMINI_API_KEY is not configured.
 * No network calls, no GenerativeModel - avoids crashes on navigation.
 */
class MockSmileAnalyzer @Inject constructor() : SmileAnalyzer {
    override suspend fun analyzeSmile(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        delay(300) // Simulate brief processing
        "Diseño de sonrisa simulado (modo demo). La foto fue procesada correctamente. Configura GEMINI_API_KEY en local.properties para usar análisis con IA."
    }
}
