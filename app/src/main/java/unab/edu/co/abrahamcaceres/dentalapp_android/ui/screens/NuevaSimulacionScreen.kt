package unab.edu.co.abrahamcaceres.dentalapp_android.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.components.CameraPreview
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.components.CameraProcedureSelector
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.components.ProcedureTab
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.AccentBlue
import java.io.File
import java.util.concurrent.Executors

private const val HOLD_DURATION_MS = 1800L

@Composable
fun NuevaSimulacionScreen(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onStartProcessing: (name: String, age: String, photoUri: Uri) -> Unit
) {
    val context = LocalContext.current
    var patientName by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var useFrontCamera by remember { mutableStateOf(false) }
    val imageCaptureState = remember { mutableStateOf<ImageCapture?>(null) }

    fun validateName(): Boolean {
        nameError = if (patientName.isBlank()) "El nombre es obligatorio" else null
        return nameError == null
    }

    val canSubmit = patientName.isNotBlank() && photoUri != null
    val scope = rememberCoroutineScope()
    val progressAnim = remember { Animatable(0f) }
    val procedureTabs = listOf(
        ProcedureTab(Icons.Default.CameraAlt, "Diseño Sonrisa", showNotification = false, enabled = true),
        ProcedureTab(Icons.Default.LightMode, "Blanqueamiento", showNotification = true, enabled = false),
        ProcedureTab(Icons.Default.Face, "Carillas", showNotification = true, enabled = false)
    )
    var selectedProcedureIndex by remember { mutableStateOf(0) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) { photoUri = uri } }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* permission handled in camera preview */ }

    fun takePicture() {
        val capture = imageCaptureState.value ?: return
        val photoFile = File.createTempFile("dental_photo_", ".jpg", context.cacheDir)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        capture.takePicture(
            outputOptions,
            Executors.newSingleThreadExecutor(),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    scope.launch(Dispatchers.Main) {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            photoFile
                        )
                        photoUri = uri
                    }
                }
                override fun onError(exc: ImageCaptureException) {}
            }
        )
    }

    val darkTextMuted = Color(0xFF8E8E93)

    if (photoUri == null) {
        // Camera-first mode: full screen camera with Cal AI style controls
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    useFrontCamera = useFrontCamera,
                    onImageCaptureReady = { imageCaptureState.value = it }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Permiso de cámara requerido", color = Color.White)
                }
                LaunchedEffect(Unit) {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Capturar Sonrisa",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.size(44.dp))
            }

            // Bottom area: procedures bar above capture button (reference layout)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                // Camera procedure selector with glassmorphism and sliding pill
                CameraProcedureSelector(
                    procedures = procedureTabs,
                    selectedIndex = selectedProcedureIndex,
                    onTabSelected = { if (procedureTabs[it].enabled) selectedProcedureIndex = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                )

                // Bottom row: Gallery (left) | Capture (center) | Camera switch (right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gallery - left of capture button
                    IconButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = "Galería",
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                    }

                    // Capture: simple tap to take picture (no ring progress)
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clickable(enabled = hasPermission) { takePicture() },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Color.White,
                                style = Stroke(width = 4.dp.toPx())
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {}
                    }

                    // Camera switch
                    IconButton(
                        onClick = { useFrontCamera = !useFrontCamera },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            Icons.Default.Cameraswitch,
                            contentDescription = "Cambiar cámara",
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    } else {
        // Photo + name: enter patient name or retake, hold to submit
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Photo as background (subtle)
            AsyncImage(
                model = photoUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { photoUri = null },
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Text("Volver a tomar", color = Color.White)
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(text = "Capturar Sonrisa", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    }
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Nombre del Paciente",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = patientName,
                                onValueChange = { patientName = it; nameError = null },
                                placeholder = { Text("Ingrese nombre", color = darkTextMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = nameError != null,
                                supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                    focusedBorderColor = darkTextMuted,
                                    unfocusedBorderColor = darkTextMuted.copy(alpha = 0.5f),
                                    errorBorderColor = MaterialTheme.colorScheme.error,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color.White
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.size(48.dp))
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .pointerInput(canSubmit) {
                                    if (!canSubmit) return@pointerInput
                                    detectTapGestures(
                                        onPress = {
                                            validateName()
                                            if (nameError == null) {
                                                val job = scope.launch {
                                                    progressAnim.snapTo(0f)
                                                    progressAnim.animateTo(
                                                        1f,
                                                        animationSpec = tween(HOLD_DURATION_MS.toInt())
                                                    )
                                                    if (progressAnim.value >= 0.99f) {
                                                        photoUri?.let { uri ->
                                                            onStartProcessing(patientName, "", uri)
                                                        }
                                                    }
                                                }
                                                tryAwaitRelease()
                                                job.cancel()
                                                scope.launch { progressAnim.snapTo(0f) }
                                            }
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(Modifier.fillMaxSize()) {
                                val strokeWidth = 4.dp.toPx()
                                drawCircle(color = Color.White, style = Stroke(width = strokeWidth))
                                if (progressAnim.value > 0f) {
                                    drawArc(
                                        color = AccentBlue,
                                        startAngle = -90f,
                                        sweepAngle = 360f * progressAnim.value,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(if (canSubmit) Color.White else Color.White.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Enviar",
                                    modifier = Modifier.size(32.dp),
                                    tint = Color.Black
                                )
                            }
                        }
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                    if (nameError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = nameError!!,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
