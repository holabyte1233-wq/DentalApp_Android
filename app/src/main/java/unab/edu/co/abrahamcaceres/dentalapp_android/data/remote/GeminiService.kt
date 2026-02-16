package unab.edu.co.abrahamcaceres.dentalapp_android.data.remote

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService @Inject constructor(
    private val generativeModel: GenerativeModel
) {

    suspend fun generateSmile(originalBitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val prompt = "Actúa como un odontólogo estético experto. Analiza la imagen dental proporcionada. Genera una imagen nueva aplicando carillas de porcelana, corrigiendo alineación y blanqueando a tono B1, manteniendo la naturalidad de la encía. Retorna solo la imagen generada"

        val inputContent = content {
            image(originalBitmap)
            text(prompt)
        }

        val response = generativeModel.generateContent(inputContent)

        response.text ?: ""
    }
}
