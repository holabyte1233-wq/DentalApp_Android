package unab.edu.co.abrahamcaceres.dentalapp_android.data.remote

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService @Inject constructor(
    private val generativeModel: GenerativeModel
) : SmileAnalyzer {

    override suspend fun analyzeSmile(originalBitmap: Bitmap): String = generateSmile(originalBitmap)

    suspend fun generateSmile(originalBitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val prompt = """
            Actúa como un odontólogo estético experto. Analiza la imagen dental proporcionada.
            Describe de forma clara y profesional:
            1. Estado actual de la sonrisa (alineación, color, posibles mejoras)
            2. Tratamiento recomendado (ej: blanqueamiento, carillas, ortodoncia)
            3. Resultado esperado tras el tratamiento
            Responde en español, de forma concisa (2-4 párrafos).
        """.trimIndent()

        val inputContent = content {
            image(originalBitmap)
            text(prompt)
        }

        try {
            val response = generativeModel.generateContent(inputContent)

            val feedback = response.promptFeedback
            if (feedback?.blockReason != null) {
                throw GeminiSafetyException(
                    "La IA no pudo procesar la imagen (razón: ${feedback.blockReason}). Intenta con una foto más clara."
                )
            }

            response.text
                ?: throw GeminiSafetyException("La IA no generó una respuesta. Intenta con una foto diferente.")
        } catch (e: GeminiSafetyException) {
            throw e
        } catch (e: Exception) {
            throw GeminiSafetyException(
                "Error al procesar con IA: ${e.message ?: "error desconocido"}. Intenta de nuevo."
            )
        }
    }
}

class GeminiSafetyException(message: String) : Exception(message)
