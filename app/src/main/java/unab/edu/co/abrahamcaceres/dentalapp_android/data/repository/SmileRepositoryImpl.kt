package unab.edu.co.abrahamcaceres.dentalapp_android.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import unab.edu.co.abrahamcaceres.dentalapp_android.data.Patient

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
                val patientToSave = patient.copy(fotoUrl = photoUrl)

                supabaseClient.from("patients").insert(patientToSave)

                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /** Guarda el diseño generado en el expediente del paciente (tabla diseños). */
    suspend fun saveDesignToExpediente(patientId: String, designImageUrl: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUserOrNull()?.id
                    ?: throw IllegalStateException("User not logged in")
                supabaseClient.from("diseños").insert(
                    mapOf(
                        "patient_id" to patientId,
                        "doctor_id" to userId,
                        "image_url" to designImageUrl
                    )
                )
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
