package unab.edu.co.abrahamcaceres.dentalapp_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlin.math.roundToInt
import unab.edu.co.abrahamcaceres.dentalapp_android.data.SimulationResult
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.RoyalBlue
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.SystemGray6
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
    result: SimulationResult?,
    patientName: String? = null,
    errorMessage: String? = null,
    onBack: () -> Unit,
    onShare: (previewData: SharePreviewData) -> Unit,
    onSaveResult: (finalDescription: String) -> Unit,
    onDiscard: () -> Unit
) {
    if (result == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(SystemGray6),
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
                Text("Volver", color = RoyalBlue)
            }
        }
        return
    }

    val data = result
    var editableDescription by remember(data) { mutableStateOf(data.description) }
    var showSharePreview by remember { mutableStateOf(false) }

    if (showSharePreview) {
        SharePreviewDialog(
            previewData = SharePreviewData(
                patientName = patientName,
                treatmentName = data.treatmentName,
                description = editableDescription,
                beforeImageUrl = data.beforeImageUrl,
                afterImageUrl = data.afterImageUrl,
                expectedDuration = data.expectedDuration,
                estimatedCost = data.estimatedCost
            ),
            onDismiss = { showSharePreview = false },
            onShare = {
                onShare(SharePreviewData(
                    patientName = patientName,
                    treatmentName = data.treatmentName,
                    description = editableDescription,
                    beforeImageUrl = data.beforeImageUrl,
                    afterImageUrl = data.afterImageUrl,
                    expectedDuration = data.expectedDuration,
                    estimatedCost = data.estimatedCost
                ))
                showSharePreview = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SystemGray6)
    ) {
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
            TextButton(onClick = { showSharePreview = true }) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(20.dp), tint = RoyalBlue)
                Spacer(modifier = Modifier.size(4.dp))
                Text("Compartir", color = RoyalBlue)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Resultado de Simulación",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
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

        // Before/After slider
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            BeforeAfterSlider(
                beforeImageUrl = data.beforeImageUrl,
                afterImageUrl = data.afterImageUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Desliza para comparar",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recommendation card - editable description for doctor approval
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(20.dp)
        ) {
            Text(
                text = data.treatmentName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Descripción (el doctor puede editar antes de guardar)",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = editableDescription,
                onValueChange = { editableDescription = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = RoyalBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Duración estimada: ${data.expectedDuration}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = "Coste estimado: ${data.estimatedCost}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            androidx.compose.material3.Button(
                onClick = { onSaveResult(editableDescription) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(24.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                )
            ) {
                Text("Guardar Resultado", color = Color.White)
            }
            androidx.compose.material3.Button(
                onClick = onDiscard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(24.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE5E5EA)
                )
            ) {
                Text("Descartar", color = TextSecondary)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun BeforeAfterSlider(
    beforeImageUrl: String,
    afterImageUrl: String,
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember { mutableStateOf(0.5f) }
    var widthPx by remember { mutableStateOf(0f) }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .onSizeChanged { widthPx = it.width.toFloat() }
    ) {
        // After (right) image - full
        AsyncImage(
            model = afterImageUrl,
            contentDescription = "Después",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Before (left) image - clipped
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(sliderPosition)
                    .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
            ) {
                AsyncImage(
                    model = beforeImageUrl,
                    contentDescription = "Antes",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        // Labels: Antes (top-left), Después (top-right), white/90 backdrop
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Antes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Después", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        // Draggable handle: 48.dp circle, two vertical lines inside
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(48.dp)
                .offset {
                    val halfHandle = with(density) { 24.dp.roundToPx() }
                    IntOffset((widthPx * sliderPosition - halfHandle).roundToInt(), 0)
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (widthPx > 0) {
                            val newPos = (sliderPosition + dragAmount / widthPx).coerceIn(0.1f, 0.9f)
                            sliderPosition = newPos
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(modifier = Modifier.width(1.dp).height(16.dp).background(TextSecondary))
                    Box(modifier = Modifier.width(1.dp).height(16.dp).background(TextSecondary))
                }
            }
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
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = RoyalBlue)
                Spacer(modifier = Modifier.size(4.dp))
                Text("Compartir", color = RoyalBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = TextSecondary)
            }
        }
    )
}
