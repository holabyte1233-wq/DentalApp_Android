package unab.edu.co.abrahamcaceres.dentalapp_android.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import unab.edu.co.abrahamcaceres.dentalapp_android.data.Patient
import unab.edu.co.abrahamcaceres.dentalapp_android.data.TreatmentRecord
import unab.edu.co.abrahamcaceres.dentalapp_android.domain.repository.PatientRepository
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.AccentBlue
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.SystemGray6
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.TextSecondary
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

private fun shareText(context: android.content.Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir"))
}

private suspend fun exportImageAsJpg(context: android.content.Context, imageUrl: String) {
    withContext(Dispatchers.IO) {
        try {
            val bytes = URL(imageUrl).openStream().use { it.readBytes() }
            val exportDir = File(context.cacheDir, "exports")
            exportDir.mkdirs()
            val file = File(exportDir, "simulacion_${System.currentTimeMillis()}.jpg")
            file.writeBytes(bytes)
            withContext(Dispatchers.Main) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Exportar imagen JPG"))
            }
        } catch (_: Exception) { }
    }
}

sealed interface PatientDetailState {
    data object Loading : PatientDetailState
    data class Loaded(val patient: Patient) : PatientDetailState
    data class NotFound(val message: String = "Paciente no encontrado") : PatientDetailState
}

@HiltViewModel
class PatientDetailViewModel @Inject constructor(
    private val patientRepository: PatientRepository
) : ViewModel() {
    private val _state = MutableStateFlow<PatientDetailState>(PatientDetailState.Loading)
    val state: StateFlow<PatientDetailState> = _state.asStateFlow()

    fun loadPatient(patientId: String) {
        viewModelScope.launch {
            _state.value = PatientDetailState.Loading
            val patient = patientRepository.getPatientById(patientId)
            _state.value = if (patient != null) {
                PatientDetailState.Loaded(patient)
            } else {
                PatientDetailState.NotFound()
            }
        }
    }
}

@Composable
fun PatientDetailsScreen(
    patientId: String?,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNewSimulation: () -> Unit,
    viewModel: PatientDetailViewModel = hiltViewModel()
) {
    val detailState by viewModel.state.collectAsState()

    LaunchedEffect(patientId) {
        if (patientId != null) {
            viewModel.loadPatient(patientId)
        }
    }

    when (val s = detailState) {
        is PatientDetailState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }
        is PatientDetailState.NotFound -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(s.message)
            }
            return
        }
        is PatientDetailState.Loaded -> {}
    }

    val patient = (detailState as PatientDetailState.Loaded).patient

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SystemGray6
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(SystemGray6)
        ) {
            // Header: Atrás
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp), tint = AccentBlue)
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Atrás", color = AccentBlue)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // Patient card: name, image, last view
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = patient.fotoUrl?.takeIf { it.isNotBlank() } ?: patient.avatar.ifBlank { null },
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = patient.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp), tint = TextSecondary)
                                Spacer(modifier = Modifier.size(6.dp))
                                val lastVisitFormatted = try {
                                    val esLocale = Locale.forLanguageTag("es-ES")
                                    val parsed = SimpleDateFormat("yyyy-MM-dd", esLocale).parse(patient.lastVisit)
                                    parsed?.let { SimpleDateFormat("d MMM yyyy", esLocale).format(it) } ?: patient.lastVisit
                                } catch (_: Exception) {
                                    patient.lastVisit
                                }
                                Text(
                                    "Última visita: $lastVisitFormatted",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Before/After images from treatments
                    val context = LocalContext.current
                    val scope = rememberCoroutineScope()
                    val treatmentsWithImages = patient.treatments.filter {
                        !it.beforeImageUrl.isNullOrBlank() || !it.afterImageUrl.isNullOrBlank()
                    }
                    for (record in treatmentsWithImages) {
                        Spacer(modifier = Modifier.height(16.dp))
                        TreatmentImagesCard(
                            record = record,
                            patientName = patient.name,
                            onExportJpg = { url -> scope.launch { exportImageAsJpg(context, url) } },
                            onExportInfo = { shareText(context, it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Nueva Simulación button
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
private fun TreatmentImagesCard(
    record: TreatmentRecord,
    patientName: String,
    onExportJpg: (String) -> Unit,
    onExportInfo: (String) -> Unit
) {
    Text(
        text = record.treatment,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        record.beforeImageUrl?.let { url ->
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Antes",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                AsyncImage(
                    model = url,
                    contentDescription = "Antes",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
        record.afterImageUrl?.let { url ->
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Después",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                AsyncImage(
                    model = url,
                    contentDescription = "Después",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        record.afterImageUrl?.let { imageUrl ->
            IconButton(onClick = { onExportJpg(imageUrl) }) {
                Icon(Icons.Default.Download, contentDescription = "Exportar JPG", tint = AccentBlue)
            }
        }
        IconButton(
            onClick = {
                val info = "Paciente: $patientName\nTratamiento: ${record.treatment}\nFecha: ${record.date}\nEstado: ${record.status}\nNotas: ${record.notes}"
                onExportInfo(info)
            }
        ) {
            Icon(Icons.Default.Share, contentDescription = "Exportar info", tint = AccentBlue)
        }
    }
}
