package unab.edu.co.abrahamcaceres.dentalapp_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import unab.edu.co.abrahamcaceres.dentalapp_android.data.Patient
import unab.edu.co.abrahamcaceres.dentalapp_android.data.TreatmentRecord
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.DestructiveRed
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.RoyalBlue
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.StatusGreen
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.StatusOrange
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.SystemGray6
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PatientDetailsScreen(
    patient: Patient?,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNewSimulation: () -> Unit
) {
    if (patient == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Paciente no encontrado")
        }
        return
    }

    var isEditing by remember { mutableStateOf(false) }
    var name by remember(patient.id) { mutableStateOf(patient.name) }
    var age by remember(patient.id) { mutableStateOf(patient.age.toString()) }
    var email by remember(patient.id) { mutableStateOf(patient.email) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var ageError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var saveSucceeded by remember { mutableStateOf<Boolean?>(null) }

    fun validate(): Boolean {
        nameError = if (name.isBlank()) "Este campo es obligatorio" else null
        ageError = if (age.isBlank()) "Este campo es obligatorio" else null
        emailError = when {
            email.isBlank() -> "Este campo es obligatorio"
            !email.contains("@") -> "Introduce un email válido (debe contener @)"
            else -> null
        }
        return nameError == null && ageError == null && emailError == null
    }

    fun onConfirmSave() {
        isEditing = false
        showConfirmDialog = false
        saveSucceeded = true
    }

    LaunchedEffect(saveSucceeded) {
        when (saveSucceeded) {
            true -> {
                snackbarHostState.showSnackbar(
                    message = "✅ Datos guardados correctamente",
                    duration = SnackbarDuration.Short
                )
                saveSucceeded = null
            }
            false -> {
                snackbarHostState.showSnackbar(
                    message = "❌ Error al guardar (comprueba la conexión)",
                    duration = SnackbarDuration.Long
                )
                saveSucceeded = null
            }
            null -> {}
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("¿Confirmar datos?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Nombre: $name")
                    Text("Email: $email")
                    Text("Edad: $age años")
                }
            },
            confirmButton = {
                TextButton(onClick = { onConfirmSave() }) {
                    Text("Confirmar", color = RoyalBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Corregir", color = TextSecondary)
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SystemGray6,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(SystemGray6)
    ) {
        // Header: Atrás | Editar / Cancelar | Guardar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp), tint = RoyalBlue)
                Spacer(modifier = Modifier.size(4.dp))
                Text("Atrás", color = RoyalBlue)
            }
            if (!isEditing) {
                TextButton(onClick = { isEditing = true }) {
                    Text("Editar", color = DestructiveRed)
                }
            } else {
                Row {
                    TextButton(onClick = { isEditing = false; name = patient.name; age = patient.age.toString(); email = patient.email; nameError = null; ageError = null; emailError = null }) {
                        Text("Cancelar", color = TextSecondary)
                    }
                    TextButton(
                        onClick = {
                            if (!validate()) return@TextButton
                            showConfirmDialog = true
                        }
                    ) {
                        Text("Guardar", color = RoyalBlue)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Patient info card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(24.dp)
            ) {
                AsyncImage(
                    model = patient.avatar,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (isEditing) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; nameError = null },
                        label = { Text("Nombre") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextSecondary) },
                        isError = nameError != null,
                        supportingText = nameError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SystemGray6,
                            unfocusedContainerColor = SystemGray6,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            errorSupportingTextColor = MaterialTheme.colorScheme.error
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it.filter { c -> c.isDigit() }.take(3); ageError = null },
                        label = { Text("Edad") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextSecondary) },
                        isError = ageError != null,
                        supportingText = ageError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SystemGray6,
                            unfocusedContainerColor = SystemGray6,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            errorSupportingTextColor = MaterialTheme.colorScheme.error
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; emailError = null },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextSecondary) },
                        isError = emailError != null,
                        supportingText = emailError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SystemGray6,
                            unfocusedContainerColor = SystemGray6,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            errorSupportingTextColor = MaterialTheme.colorScheme.error
                        )
                    )
                } else {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${age} años",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(patient.phone, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Mail, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(email, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.size(8.dp))
                    val lastVisitFormatted = try {
                        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale("es", "ES")).parse(patient.lastVisit)
                        parsed?.let { SimpleDateFormat("d MMM yyyy", Locale("es", "ES")).format(it) } ?: patient.lastVisit
                    } catch (_: Exception) {
                        patient.lastVisit
                    }
                    Text("Última visita: $lastVisitFormatted", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Medical history card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(20.dp)
            ) {
                Text(
                    text = "Historial Médico",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                patient.medicalHistory.forEach { item ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("• ", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text(item, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Treatment records card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(20.dp)
            ) {
                Text(
                    text = "Tratamientos",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                patient.treatments.forEach { record ->
                    TreatmentRecordRow(record = record)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Nueva Simulación button - black, full width, gradient backdrop
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, SystemGray6, SystemGray6)))
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            androidx.compose.material3.Button(
                onClick = onNewSimulation,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(24.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                )
            ) {
                Text("Nueva Simulación", color = Color.White)
            }
        }
    }
    }
}

@Composable
private fun TreatmentRecordRow(record: TreatmentRecord) {
    val borderColor = Color(record.colorIndicator)
    val statusColor = when (record.status) {
        "Completado" -> StatusGreen
        "En Progreso" -> StatusOrange
        else -> TextSecondary
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp)
            .border(3.dp, borderColor, RoundedCornerShape(4.dp))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.treatment,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = record.date,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = record.notes,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        androidx.compose.material3.Surface(
            color = statusColor.copy(alpha = 0.2f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = record.status,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = statusColor
            )
        }
    }
}
