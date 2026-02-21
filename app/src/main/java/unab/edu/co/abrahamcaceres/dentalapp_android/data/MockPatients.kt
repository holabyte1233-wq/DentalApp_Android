package unab.edu.co.abrahamcaceres.dentalapp_android.data

val mockPatients = listOf(
    Patient(
        id = "1",
        name = "Ana García López",
        age = 32,
        phone = "+34 612 345 678",
        email = "ana.garcia@email.com",
        avatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200",
        lastVisit = "2026-01-15",
        medicalHistory = listOf(
            "Alergia a la penicilina",
            "Ortodoncia previa (2020-2022)",
            "Limpieza dental regular cada 6 meses"
        ),
        treatments = listOf(
            TreatmentRecord(
                id = "t1",
                treatment = "Blanqueamiento Dental",
                date = "2026-01-15",
                status = "Completado",
                notes = "Tratamiento de 3 sesiones, resultados excelentes",
                colorIndicator = 0xFF34C759,
                beforeImageUrl = "https://images.unsplash.com/photo-1606811841689-23dfddce3e95?w=400",
                afterImageUrl = "https://images.unsplash.com/photo-1629909613654-28e377c37b09?w=400"
            ),
            TreatmentRecord(
                id = "t2",
                treatment = "Revisión General",
                date = "2025-12-10",
                status = "Completado",
                notes = "Estado general bueno, sin caries",
                colorIndicator = 0xFF34C759
            )
        )
    ),
    Patient(
        id = "2",
        name = "Carlos Rodríguez Martín",
        age = 45,
        phone = "+34 623 456 789",
        email = "carlos.rodriguez@email.com",
        avatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
        lastVisit = "2026-02-01",
        medicalHistory = listOf(
            "Hipertensión controlada",
            "Implante dental en molar inferior derecho (2023)",
            "Sin alergias conocidas"
        ),
        treatments = listOf(
            TreatmentRecord(
                id = "t3",
                treatment = "Implante Dental",
                date = "2026-02-01",
                status = "En Progreso",
                notes = "Primera fase completada, pendiente corona",
                colorIndicator = 0xFFFF9500
            ),
            TreatmentRecord(
                id = "t4",
                treatment = "Limpieza Profunda",
                date = "2026-01-20",
                status = "Completado",
                notes = "Eliminación de sarro y pulido dental",
                colorIndicator = 0xFF34C759
            )
        )
    ),
    Patient(
        id = "3",
        name = "María Sánchez Torres",
        age = 28,
        phone = "+34 634 567 890",
        email = "maria.sanchez@email.com",
        avatar = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200",
        lastVisit = "2026-02-05",
        medicalHistory = listOf(
            "Sin condiciones médicas relevantes",
            "Uso de retenedores nocturnos",
            "Sensibilidad dental leve"
        ),
        treatments = listOf(
            TreatmentRecord(
                id = "t5",
                treatment = "Carillas Dentales",
                date = "2026-02-05",
                status = "En Progreso",
                notes = "Moldes tomados, pendiente colocación",
                colorIndicator = 0xFFFF9500
            )
        )
    ),
    Patient(
        id = "4",
        name = "Javier López Fernández",
        age = 52,
        phone = "+34 645 678 901",
        email = "javier.lopez@email.com",
        avatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
        lastVisit = "2025-12-28",
        medicalHistory = listOf(
            "Diabetes tipo 2 controlada",
            "Enfermedad periodontal tratada",
            "Alergia a la lidocaína (usar articaína)"
        ),
        treatments = listOf(
            TreatmentRecord(
                id = "t6",
                treatment = "Mantenimiento Periodontal",
                date = "2025-12-28",
                status = "Completado",
                notes = "Control trimestral, encías en buen estado",
                colorIndicator = 0xFF34C759
            )
        )
    ),
    Patient(
        id = "5",
        name = "Laura Martínez Ruiz",
        age = 35,
        phone = "+34 656 789 012",
        email = "laura.martinez@email.com",
        avatar = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200",
        lastVisit = "2026-02-08",
        medicalHistory = listOf(
            "Embarazo actual (segundo trimestre)",
            "Sin alergias",
            "Historia de gingivitis"
        ),
        treatments = listOf(
            TreatmentRecord(
                id = "t7",
                treatment = "Limpieza Prenatal",
                date = "2026-02-08",
                status = "Completado",
                notes = "Limpieza suave adaptada al embarazo",
                colorIndicator = 0xFF34C759
            )
        )
    )
)

// Default simulation result for demo - Figma image URLs
val defaultSimulationResult = SimulationResult(
    treatmentName = "Blanqueamiento Dental Premium",
    description = "Tratamiento con tecnología LED y gel profesional para un blanqueamiento seguro y duradero. Recomendado para conseguir hasta 8 tonos más blancos.",
    expectedDuration = "3-4 sesiones de 45 min",
    estimatedCost = "€ 350 - € 450",
    beforeImageUrl = "https://images.unsplash.com/photo-1663182234283-28941e7612da?w=1080",
    afterImageUrl = "https://images.unsplash.com/photo-1675526607070-f5cbd71dde92?w=1080"
)
