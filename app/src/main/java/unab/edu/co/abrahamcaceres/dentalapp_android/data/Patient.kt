package unab.edu.co.abrahamcaceres.dentalapp_android.data

data class Patient(
    val id: String,
    val name: String,
    val age: Int,
    val phone: String,
    val email: String,
    val avatar: String,
    val lastVisit: String,
    val medicalHistory: List<String>,
    val treatments: List<TreatmentRecord>
)

data class TreatmentRecord(
    val id: String,
    val treatment: String,
    val date: String,
    val status: String, // "Completado" | "En Progreso"
    val notes: String,
    val colorIndicator: Long = 0xFF2563EB // left border color
)

data class SimulationResult(
    val treatmentName: String,
    val description: String,
    val expectedDuration: String,
    val estimatedCost: String,
    val beforeImageUrl: String,
    val afterImageUrl: String
)
