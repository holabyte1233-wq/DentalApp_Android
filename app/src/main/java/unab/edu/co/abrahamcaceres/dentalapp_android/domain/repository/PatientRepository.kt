package unab.edu.co.abrahamcaceres.dentalapp_android.domain.repository

import unab.edu.co.abrahamcaceres.dentalapp_android.data.Patient

interface PatientRepository {
    /** Retorna true si ya existe un paciente con ese email o esa cédula. */
    suspend fun checkPatientExists(email: String, cedula: String): Boolean

    /** Retorna la lista de pacientes registrados del doctor autenticado. */
    suspend fun getPatients(): List<Patient>

    /** Busca un paciente por su ID. */
    suspend fun getPatientById(patientId: String): Patient?
}
