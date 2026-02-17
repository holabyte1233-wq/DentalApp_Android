package unab.edu.co.abrahamcaceres.dentalapp_android.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import unab.edu.co.abrahamcaceres.dentalapp_android.R
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.RoyalBlue
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.SystemGray6
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.TextSecondary
import java.io.File
import java.io.FileOutputStream

private const val HOLD_DURATION_MS = 2000L

@Composable
fun NuevaSimulacionScreen(
    modifier: Modifier = Modifier,
    isGeminiActive: Boolean = false,
    onCancel: () -> Unit,
    onStartProcessing: (name: String, age: String, photoUri: Uri, manualDescription: String) -> Unit
) {
    val context = LocalContext.current
    var patientName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var manualDescription by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var ageError by remember { mutableStateOf<String?>(null) }
    var photoError by remember { mutableStateOf<String?>(null) }

    fun validateFields(): Boolean {
        var valid = true
        nameError = if (patientName.isBlank()) {
            valid = false
            "El nombre es obligatorio"
        } else null
        val ageNum = age.toIntOrNull()
        ageError = when {
            age.isBlank() -> {
                valid = false
                "La edad es obligatoria"
            }
            ageNum == null || ageNum !in 1..120 -> {
                valid = false
                "Edad debe estar entre 1 y 120"
            }
            else -> null
        }
        photoError = if (photoUri == null) {
            valid = false
            "Se requiere una fotografía dental"
        } else null
        return valid
    }

    val allFieldsValid = patientName.isNotBlank() &&
        age.toIntOrNull() in 1..120 &&
        photoUri != null
    var progress by remember { mutableStateOf(0f) }
    val progressAnim = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // Camera URI for TakePicture
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    fun createTempPhotoUri(): Uri {
        val photoFile = File.createTempFile("dental_photo_", ".jpg", context.cacheDir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) { photoUri = uri; photoError = null } }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success -> if (success) { photoUri = cameraUri; photoError = null } }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createTempPhotoUri()
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    fun launchCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val uri = createTempPhotoUri()
            cameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun useSampleImage() {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_sample_dental)
            ?: return
        val bitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
        android.graphics.Canvas(bitmap).let { canvas ->
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }
        val file = File(context.cacheDir, "sample_dental.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        photoUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        photoError = null
    }

    LaunchedEffect(progressAnim.value) {
        progress = progressAnim.value
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
            TextButton(onClick = onCancel) {
                Text("Cancelar", color = RoyalBlue)
            }
            Text(
                text = "Capturar Sonrisa",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Nombre del Paciente card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Nombre del Paciente",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = patientName,
                    onValueChange = {
                        patientName = it
                        nameError = null
                    },
                    placeholder = { Text("Ingrese nombre completo", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SystemGray6,
                        unfocusedContainerColor = SystemGray6,
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        errorBorderColor = MaterialTheme.colorScheme.error
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Edad card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Edad",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = age,
                    onValueChange = {
                        age = it.filter { c -> c.isDigit() }.take(3)
                        ageError = null
                    },
                    placeholder = { Text("Ingrese edad (1-120)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = ageError != null,
                    supportingText = ageError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SystemGray6,
                        unfocusedContainerColor = SystemGray6,
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        errorBorderColor = MaterialTheme.colorScheme.error
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Manual description (when Gemini not active)
            if (!isGeminiActive) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Descripción / Notas (opcional)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Cuando la IA no está activa, puedes añadir la descripción del tratamiento manualmente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = manualDescription,
                        onValueChange = { manualDescription = it },
                        placeholder = { Text("Ej: Diseño de sonrisa con carillas, blanqueamiento B1...", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SystemGray6,
                            unfocusedContainerColor = SystemGray6,
                            focusedBorderColor = MaterialTheme.colorScheme.outline,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Fotografía Dental card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Fotografía Dental",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (photoError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = photoError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 2.dp,
                            color = TextSecondary.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri != null) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Selecciona una opción abajo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { launchCamera() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp), tint = RoyalBlue)
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Cámara", color = RoyalBlue)
                    }
                    TextButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp), tint = RoyalBlue)
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Galería", color = RoyalBlue)
                    }
                    TextButton(
                        onClick = { useSampleImage() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp), tint = RoyalBlue)
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Demo", color = RoyalBlue)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Mantén presionado para iniciar",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Large circular button (press and hold 2s)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(128.dp)
                    .pointerInput(allFieldsValid) {
                        detectTapGestures(
                            onPress = {
                                if (!allFieldsValid) {
                                    validateFields()
                                    return@detectTapGestures
                                }
                                val job = scope.launch {
                                    progressAnim.snapTo(0f)
                                    progressAnim.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(HOLD_DURATION_MS.toInt())
                                    )
                                    if (progressAnim.value >= 1f) {
                                        photoUri?.let { uri ->
                                            onStartProcessing(patientName, age, uri, manualDescription)
                                        }
                                    }
                                }
                                tryAwaitRelease()
                                job.cancel()
                                scope.launch { progressAnim.snapTo(0f) }
                            }
                        )
                    }
            ) {
                val buttonEnabled = allFieldsValid
                Canvas(Modifier.fillMaxSize()) {
                    val strokeWidth = 6.dp.toPx()
                    drawArc(
                        color = if (buttonEnabled) RoyalBlue else Color.Gray,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .background(if (buttonEnabled) Color.Black else Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Iniciar procesamiento",
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
            }

            if (!allFieldsValid) {
                Spacer(modifier = Modifier.height(8.dp))
                val missing = mutableListOf<String>().apply {
                    if (patientName.isBlank()) add("Nombre")
                    if (age.isBlank() || age.toIntOrNull() !in 1..120) add("Edad (1-120)")
                    if (photoUri == null) add("Fotografía")
                }
                Text(
                    text = if (missing.isEmpty()) "" else "Completa: ${missing.joinToString(", ")}",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
