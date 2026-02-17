package unab.edu.co.abrahamcaceres.dentalapp_android.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Patient(
    val id: String,
    @SerialName("full_name")
    val name: String,
    val age: Int = 0,
    val phone: String = "",
    val email: String = "",
    @SerialName("foto_url")
    val fotoUrl: String? = null,
    @SerialName("doctor_id")
    val doctorId: String? = null,
    @Transient
    val avatar: String = "",
    @Transient
    val lastVisit: String = "",
    @Transient
    val medicalHistory: List<String> = emptyList(),
    @Transient
    val treatments: List<TreatmentRecord> = emptyList()
)

@Serializable
data class TreatmentRecord(
    val id: String,
    val treatment: String,
    val date: String,
    val status: String, // "Completado" | "En Progreso"
    val notes: String,
    val colorIndicator: Long = 0xFF2563EB // left border color
)

@Serializable
data class SimulationResult(
    val treatmentName: String,
    val description: String,
    val expectedDuration: String,
    val estimatedCost: String,
    val beforeImageUrl: String,
    val afterImageUrl: String
)
