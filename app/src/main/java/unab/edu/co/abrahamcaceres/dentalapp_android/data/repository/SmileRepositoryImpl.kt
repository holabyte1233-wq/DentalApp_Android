package unab.edu.co.abrahamcaceres.dentalapp_android.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import unab.edu.co.abrahamcaceres.dentalapp_android.data.Patient

@Serializable
private data class DesignInsert(
    @SerialName("patient_id") val patientId: String,
    @SerialName("doctor_id") val doctorId: String,
    @SerialName("image_url") val imageUrl: String,
    val description: String? = null
)

class SmileRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val auth: Auth
) {

    suspend fun uploadOriginalPhoto(photoBytes: ByteArray, patientId: String): String =
        withContext(Dispatchers.IO) {
            val userId = auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("User not logged in")

            val filePath = "$userId/$patientId/original.jpg"

            val bucket = supabaseClient.storage.from("dental-images")

            bucket.upload(
                path = filePath,
                data = photoBytes
            ) {
                upsert = true
            }

            bucket.publicUrl(filePath)
        }

    suspend fun savePatientProfile(patient: Patient, photoUrl: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUserOrNull()?.id
                    ?: throw IllegalStateException("User not logged in")
                val patientToSave = patient.copy(fotoUrl = photoUrl, doctorId = userId)

                supabaseClient.from("patients").upsert(patientToSave) {
                    onConflict = "id"
                }

                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /** Guarda el diseño generado en el expediente del paciente (tabla diseños). */
    suspend fun saveDesignToExpediente(
        patientId: String,
        designImageUrl: String,
        description: String? = null
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUserOrNull()?.id
                    ?: throw IllegalStateException("User not logged in")
                val insertData = DesignInsert(
                    patientId = patientId,
                    doctorId = userId,
                    imageUrl = designImageUrl,
                    description = description?.takeIf { it.isNotBlank() }
                )
                supabaseClient.from("diseños").insert(insertData)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
