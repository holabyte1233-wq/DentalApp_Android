package unab.edu.co.abrahamcaceres.dentalapp_android.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.AccentBlue
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.SystemGray6
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.TextSecondary

private const val HOLD_DURATION_MS = 2000L

@Composable
fun SimulatorScreen(
    patientName: String?,
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onCaptureComplete: () -> Unit
) {
    var progress by remember { mutableStateOf(0f) }
    val progressAnim = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(progressAnim.value) {
        progress = progressAnim.value
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SystemGray6)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancelar", color = AccentBlue)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Capturar Sonrisa",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Mantén presionado para capturar",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(48.dp))

            // Camera button with progress ring (128.dp = w-32 h-32)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(128.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                val job = scope.launch {
                                    progressAnim.snapTo(0f)
                                    progressAnim.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(HOLD_DURATION_MS.toInt())
                                    )
                                    if (progressAnim.value >= 1f) {
                                        onCaptureComplete()
                                    }
                                }
                                tryAwaitRelease()
                                isPressed = false
                                job.cancel()
                                scope.launch {
                                    progressAnim.snapTo(0f)
                                }
                            }
                        )
                    }
            ) {
                // Progress ring
                Canvas(Modifier.fillMaxSize()) {
                    val strokeWidth = 6.dp.toPx()
                    drawArc(
                        color = AccentBlue,
                        startAngle = -90f, // start from top
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .background(androidx.compose.ui.graphics.Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Capturar",
                        modifier = Modifier.size(48.dp),
                        tint = androidx.compose.ui.graphics.Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!patientName.isNullOrBlank()) {
                Text(
                    text = patientName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
