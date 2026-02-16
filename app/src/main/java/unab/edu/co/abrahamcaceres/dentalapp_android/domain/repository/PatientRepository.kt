package unab.edu.co.abrahamcaceres.dentalapp_android.domain.repository

interface PatientRepository {
    /** Retorna true si ya existe un paciente con ese email o esa cédula. */
    suspend fun checkPatientExists(email: String, cedula: String): Boolean
}
