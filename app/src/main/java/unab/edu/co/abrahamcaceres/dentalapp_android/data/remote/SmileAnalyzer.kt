package unab.edu.co.abrahamcaceres.dentalapp_android.data.remote

import android.graphics.Bitmap

/**
 * Abstraction for smile/photo analysis. Allows mock implementation when AI is not configured.
 */
interface SmileAnalyzer {
    suspend fun analyzeSmile(bitmap: Bitmap): String
}
