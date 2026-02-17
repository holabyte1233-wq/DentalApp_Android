package unab.edu.co.abrahamcaceres.dentalapp_android.presentation.simulation

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.ByteArrayOutputStream
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Named
import unab.edu.co.abrahamcaceres.dentalapp_android.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import unab.edu.co.abrahamcaceres.dentalapp_android.data.SimulationResult
import unab.edu.co.abrahamcaceres.dentalapp_android.data.remote.SmileAnalyzer
import unab.edu.co.abrahamcaceres.dentalapp_android.data.repository.SmileRepositoryImpl

sealed interface SimulationState {
    data object Idle : SimulationState
    data object Processing : SimulationState
    data class Success(val result: SimulationResult, val patientName: String) : SimulationState
    data class Error(val message: String) : SimulationState
}

@HiltViewModel
class SimulationSharedViewModel @Inject constructor(
    private val smileAnalyzer: SmileAnalyzer,
    private val smileRepository: SmileRepositoryImpl,
    @Named("gemini_active") val isGeminiActive: Boolean,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _state = MutableStateFlow<SimulationState>(SimulationState.Idle)
    val state: StateFlow<SimulationState> = _state.asStateFlow()

    var patientName: String = ""
        private set
    var patientAge: String = ""
        private set
    var photoUri: Uri? = null
        private set
    var manualDescription: String = ""
        private set

    fun setPatientData(name: String, age: String) {
        patientName = name
        patientAge = age
    }

    fun setPhoto(uri: Uri?) {
        photoUri = uri
    }

    fun setManualDescription(description: String) {
        manualDescription = description
    }

    fun startProcessing() {
        val uri = photoUri ?: run {
            _state.value = SimulationState.Error("No se ha seleccionado ninguna foto")
            return
        }

        viewModelScope.launch {
            _state.value = SimulationState.Processing
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    appContext.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    } ?: throw IllegalStateException("No se pudo leer la imagen")
                }

                val description = when {
                    !isGeminiActive && manualDescription.isNotBlank() -> manualDescription
                    else -> smileAnalyzer.analyzeSmile(bitmap)
                }

                val photoBytes = withContext(Dispatchers.Default) {
                    val out = ByteArrayOutputStream()
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, out)
                    out.toByteArray()
                }

                val patientId = java.util.UUID.randomUUID().toString()
                val beforeUrl = try {
                    smileRepository.uploadOriginalPhoto(photoBytes, patientId)
                } catch (_: Exception) {
                    uri.toString()
                }

                val afterUrl = if (isGeminiActive) {
                    beforeUrl
                } else {
                    val resName = appContext.resources.getResourceEntryName(R.drawable.ic_sample_dental_after)
                    "android.resource://${appContext.packageName}/drawable/$resName"
                }

                val result = SimulationResult(
                    treatmentName = "Diseño de Sonrisa IA",
                    description = description,
                    expectedDuration = "2-3 sesiones",
                    estimatedCost = "Consultar con el doctor",
                    beforeImageUrl = beforeUrl,
                    afterImageUrl = afterUrl
                )

                _state.value = SimulationState.Success(
                    result = result,
                    patientName = patientName
                )
            } catch (e: Exception) {
                _state.value = SimulationState.Error(
                    e.message ?: "Error al procesar la imagen"
                )
            }
        }
    }

    fun updateDescription(newDescription: String) {
        _state.value = when (val s = _state.value) {
            is SimulationState.Success -> SimulationState.Success(
                result = s.result.copy(description = newDescription),
                patientName = s.patientName
            )
            else -> s
        }
    }

    fun reset() {
        _state.value = SimulationState.Idle
        patientName = ""
        patientAge = ""
        photoUri = null
        manualDescription = ""
    }

}
