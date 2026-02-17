package unab.edu.co.abrahamcaceres.dentalapp_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import unab.edu.co.abrahamcaceres.dentalapp_android.data.Patient
import unab.edu.co.abrahamcaceres.dentalapp_android.presentation.dashboard.DashboardUiState
import unab.edu.co.abrahamcaceres.dentalapp_android.presentation.dashboard.DashboardViewModel
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.components.ClockDisplay
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.components.GlassmorphicHeader
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.SystemGray6
import java.text.SimpleDateFormat
import java.util.Locale
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit,
    onPatientClick: (String) -> Unit,
    onNewDesign: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isWide = LocalConfiguration.current.screenWidthDp >= 600

    Column(modifier = modifier.fillMaxSize().background(SystemGray6)) {
        GlassmorphicHeader(title = "DentalTech", onLogout = { viewModel.signOut(onLogout) })

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                ClockDisplay(fontSize = 56.sp, lightWeight = true)
            }

            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator()
                    }
                }
                is DashboardUiState.Error -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.TextButton(
                                onClick = { viewModel.loadPatients() }
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                is DashboardUiState.Success -> {
                    if (state.patients.isEmpty()) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay pacientes registrados",
                                color = TextSecondary
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(state.patients) { patient ->
                                PatientCard(
                                    patient = patient,
                                    onClick = { onPatientClick(patient.id) },
                                    onRecordsClick = { /* open records */ }
                                )
                            }
                        }
                    }
                }
            }
        }

        // + Nuevo Diseño button - full width mobile, bottom-right on desktop; gradient backdrop
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, SystemGray6, SystemGray6)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 20.dp),
            contentAlignment = if (isWide) Alignment.BottomEnd else Alignment.Center
        ) {
            androidx.compose.material3.Button(
                onClick = onNewDesign,
                modifier = if (isWide) Modifier else Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("+ Nuevo Diseño", color = Color.White)
            }
        }
    }
}

@Composable
private fun PatientCard(
    patient: Patient,
    onClick: () -> Unit,
    onRecordsClick: () -> Unit
) {
    val lastVisitFormatted = try {
        val esLocale = Locale.forLanguageTag("es-ES")
        val parsed = SimpleDateFormat("yyyy-MM-dd", esLocale).parse(patient.lastVisit)
        parsed?.let { SimpleDateFormat("d MMM yyyy", esLocale).format(it) } ?: patient.lastVisit
    } catch (_: Exception) {
        patient.lastVisit
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.98f else 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = patient.fotoUrl?.takeIf { it.isNotBlank() } ?: patient.avatar.ifBlank { null },
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = patient.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Última visita: $lastVisitFormatted",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        IconButton(
            onClick = { onRecordsClick() },
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(SystemGray6)
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = "Expediente",
                modifier = Modifier.size(20.dp),
                tint = TextSecondary
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = TextSecondary
        )
    }
}
