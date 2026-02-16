package unab.edu.co.abrahamcaceres.dentalapp_android.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import unab.edu.co.abrahamcaceres.dentalapp_android.domain.repository.PatientRepository

@Serializable
private data class PatientIdRow(val id: String)

class PatientRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : PatientRepository {

    override suspend fun checkPatientExists(email: String, cedula: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val list = supabaseClient.from("pacientes").select(Columns.list("id")) {
                    filter {
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
}
