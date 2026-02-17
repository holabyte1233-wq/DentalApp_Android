package unab.edu.co.abrahamcaceres.dentalapp_android.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import unab.edu.co.abrahamcaceres.dentalapp_android.data.Patient
import unab.edu.co.abrahamcaceres.dentalapp_android.domain.repository.PatientRepository

@Serializable
private data class PatientIdRow(val id: String)

class PatientRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val auth: Auth
) : PatientRepository {

    private suspend fun currentUserId(): String =
        auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User not logged in")

    override suspend fun checkPatientExists(email: String, cedula: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val userId = currentUserId()
                val list = supabaseClient.from("patients").select(Columns.list("id")) {
                    filter {
                        eq("doctor_id", userId)
                        or {
                            eq("email", email)
                            eq("cedula", cedula)
                        }
                    }
                    limit(count = 1)
                }.decodeList<PatientIdRow>()
                list.isNotEmpty()
            } catch (e: Exception) {
                throw e
            }
        }

    override suspend fun getPatients(): List<Patient> =
        withContext(Dispatchers.IO) {
            val userId = currentUserId()
            supabaseClient.from("patients").select {
                filter { eq("doctor_id", userId) }
                order(column = "created_at", order = Order.DESCENDING)
            }.decodeList<Patient>()
        }

    override suspend fun getPatientById(patientId: String): Patient? =
        withContext(Dispatchers.IO) {
            try {
                val userId = currentUserId()
                supabaseClient.from("patients").select {
                    filter {
                        eq("id", patientId)
                        eq("doctor_id", userId)
                    }
                    limit(count = 1)
                }.decodeList<Patient>().firstOrNull()
            } catch (_: Exception) {
                null
            }
        }
}
