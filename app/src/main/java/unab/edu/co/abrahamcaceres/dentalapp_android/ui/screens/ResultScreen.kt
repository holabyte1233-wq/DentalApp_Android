package unab.edu.co.abrahamcaceres.dentalapp_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.AccentBlue
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.TextSecondary
import androidx.compose.ui.graphics.Color

data class SharePreviewData(
    val patientName: String?,
    val treatmentName: String,
    val description: String,
    val beforeImageUrl: String,
    val afterImageUrl: String,
    val expectedDuration: String,
    val estimatedCost: String
)

@Composable
fun ResultScreen(
    modifier: Modifier = Modifier,
    originalPhotoUrl: String,
    generatedPhotoUrl: String,
    patientName: String? = null,
    treatmentName: String = "Diseño de Sonrisa IA",
    description: String = "",
    expectedDuration: String = "2-3 sesiones",
    estimatedCost: String = "Consultar con el doctor",
    errorMessage: String? = null,
    isSaving: Boolean = false,
    isSaveSuccess: Boolean = false,
    saveError: String? = null,
    onBack: () -> Unit,
    onShare: (previewData: SharePreviewData) -> Unit,
    onSaveResult: (finalDescription: String) -> Unit,
    onDiscard: () -> Unit,
    onDismissSaveError: () -> Unit = {}
) {
    val hasValidImages = originalPhotoUrl.isNotBlank() && generatedPhotoUrl.isNotBlank()
    val darkBg = Color.Black
    val darkSurface = Color(0xFF1C1C1E)

    if (!hasValidImages) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(darkBg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = errorMessage ?: "No se pudo generar el resultado",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onBack) {
                Text("Volver", color = Color.White)
            }
        }
        return
    }

    var editableDescription by remember(description) { mutableStateOf(description) }
    var showSharePreview by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saveError) {
        saveError?.let { msg ->
            snackbarHostState.showSnackbar(
                message = "No se pudo guardar: $msg",
                duration = SnackbarDuration.Long
            )
            onDismissSaveError()
        }
    }
    LaunchedEffect(isSaveSuccess) {
        if (isSaveSuccess) {
            snackbarHostState.showSnackbar(
                message = "Diseño guardado correctamente. Redirigiendo...",
                duration = SnackbarDuration.Short
            )
        }
    }

    if (showSharePreview) {
        SharePreviewDialog(
            previewData = SharePreviewData(
                patientName = patientName,
                treatmentName = treatmentName,
                description = editableDescription,
                beforeImageUrl = originalPhotoUrl,
                afterImageUrl = generatedPhotoUrl,
                expectedDuration = expectedDuration,
                estimatedCost = estimatedCost
            ),
            onDismiss = { showSharePreview = false },
            onShare = {
                onShare(SharePreviewData(
                    patientName = patientName,
                    treatmentName = treatmentName,
                    description = editableDescription,
                    beforeImageUrl = originalPhotoUrl,
                    afterImageUrl = generatedPhotoUrl,
                    expectedDuration = expectedDuration,
                    estimatedCost = estimatedCost
                ))
                showSharePreview = false
            }
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = darkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(paddingValues)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                Spacer(modifier = Modifier.size(4.dp))
                Text("Atrás", color = Color.White)
            }
            TextButton(onClick = { showSharePreview = true }) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                Spacer(modifier = Modifier.size(4.dp))
                Text("Compartir", color = Color.White)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Resultado de Simulación",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            if (!patientName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = patientName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
        }

        // Antes / Después - buttons to switch view
        BeforeAfterButtons(
            beforeImageUrl = originalPhotoUrl,
            afterImageUrl = generatedPhotoUrl,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            androidx.compose.material3.Button(
                onClick = { if (!isSaving) onSaveResult(editableDescription) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
                enabled = !isSaving,
                shape = RoundedCornerShape(24.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = darkSurface,
                    disabledContainerColor = Color.Black.copy(alpha = 0.5f)
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar Resultado", color = Color.White)
                }
            }
            androidx.compose.material3.Button(
                onClick = onDiscard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = darkSurface
                )
            ) {
                Text("Descartar", color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
    }
}

@Composable
private fun BeforeAfterButtons(
    beforeImageUrl: String,
    afterImageUrl: String,
    modifier: Modifier = Modifier
) {
    var showAntes by remember { mutableStateOf(true) }
    val darkSurface = Color(0xFF1C1C1E)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = { showAntes = true },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (showAntes) Color.White
                        else darkSurface
                    )
            ) {
                Text(
                    "Antes",
                    color = if (showAntes) Color.Black else Color.White,
                    fontWeight = if (showAntes) FontWeight.Bold else FontWeight.Normal
                )
            }
            TextButton(
                onClick = { showAntes = false },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (!showAntes) Color.White
                        else darkSurface
                    )
            ) {
                Text(
                    "Después",
                    color = if (!showAntes) Color.Black else Color.White,
                    fontWeight = if (!showAntes) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(darkSurface)
        ) {
            AsyncImage(
                model = if (showAntes) beforeImageUrl else afterImageUrl,
                contentDescription = if (showAntes) "Antes" else "Después",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun SharePreviewDialog(
    previewData: SharePreviewData,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Vista previa para compartir") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                previewData.patientName?.let { name ->
                    Text(
                        text = "Paciente: $name",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = previewData.treatmentName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = previewData.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(
                        model = previewData.beforeImageUrl,
                        contentDescription = "Antes",
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    AsyncImage(
                        model = previewData.afterImageUrl,
                        contentDescription = "Después",
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Text(
                    text = "Duración: ${previewData.expectedDuration} · Coste: ${previewData.estimatedCost}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onShare) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = AccentBlue)
                Spacer(modifier = Modifier.size(4.dp))
                Text("Compartir", color = AccentBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = TextSecondary)
            }
        }
    )
}
