package unab.edu.co.abrahamcaceres.dentalapp_android.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import unab.edu.co.abrahamcaceres.dentalapp_android.presentation.smile.SaveDesignState
import unab.edu.co.abrahamcaceres.dentalapp_android.presentation.smile.SmileDesignViewModel
import unab.edu.co.abrahamcaceres.dentalapp_android.presentation.smile.SmileUiState
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.AccentBlue
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.SystemGray6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmileDesignScreen(
    patientId: String?,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    viewModel: SmileDesignViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState(initial = SmileUiState.Idle)
    val saveState by viewModel.saveState.collectAsState(initial = SaveDesignState.Idle)

    val snackbarHostState = remember { SnackbarHostState() }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }

    val isLoading = uiState is SmileUiState.Loading || saveState is SaveDesignState.Loading

    LaunchedEffect(saveState) {
        when (val state = saveState) {
            is SaveDesignState.Success -> {
                snackbarHostState.showSnackbar(
                    message = "✅ Diseño guardado correctamente",
                    duration = SnackbarDuration.Short
                )
                viewModel.clearSaveState()
            }
            is SaveDesignState.Error -> {
                snackbarHostState.showSnackbar(
                    message = "❌ Error al guardar: ${state.message}",
                    duration = SnackbarDuration.Long
                )
                viewModel.clearSaveState()
            }
            else -> {}
        }
    }

    if (showSaveConfirmDialog && uiState is SmileUiState.Success) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmDialog = false },
            title = { Text("¿Confirmar Diseño?") },
            text = {
                Text(
                    "Se guardará esta imagen en el historial del paciente. Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val success = uiState as SmileUiState.Success
                        val pid = patientId ?: return@TextButton
                        viewModel.saveDesign(pid, success.generatedImage)
                        showSaveConfirmDialog = false
                    }
                ) {
                    Text("Confirmar", color = AccentBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveConfirmDialog = false }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Diseño de Sonrisa") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = AccentBlue
                        )
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SystemGray6
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (val state = uiState) {
                    is SmileUiState.Idle -> {
                        Text(
                            text = "Procesa una imagen para generar el diseño.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    is SmileUiState.Success -> {
                        AsyncImage(
                            model = state.generatedImage,
                            contentDescription = "Diseño generado",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .padding(vertical = 8.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        androidx.compose.material3.Button(
                            onClick = { showSaveConfirmDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !isLoading && !patientId.isNullOrBlank(),
                            shape = RoundedCornerShape(24.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color.Black
                            )
                        ) {
                            Text("Guardar en Expediente", color = Color.White)
                        }
                    }
                    is SmileUiState.Error -> {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    is SmileUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Generando diseño…",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }
            }
        }
    }
}
