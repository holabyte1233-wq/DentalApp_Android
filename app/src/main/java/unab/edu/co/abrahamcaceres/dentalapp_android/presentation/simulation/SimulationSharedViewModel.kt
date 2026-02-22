package unab.edu.co.abrahamcaceres.dentalapp_android.presentation.simulation

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.ByteArrayOutputStream
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import unab.edu.co.abrahamcaceres.dentalapp_android.data.Patient
import unab.edu.co.abrahamcaceres.dentalapp_android.data.SimulationResult
import unab.edu.co.abrahamcaceres.dentalapp_android.data.remote.SmileAnalyzer
import unab.edu.co.abrahamcaceres.dentalapp_android.data.repository.SmileRepositoryImpl

sealed interface SimulationState {
    data object Idle : SimulationState
    data object Processing : SimulationState
    data class Success(
        val result: SimulationResult,
        val patientName: String,
        val patientId: String,
        val patientAge: String
    ) : SimulationState
    data class Error(val message: String) : SimulationState
}

sealed interface SaveSimulationState {
    data object Idle : SaveSimulationState
    data object Saving : SaveSimulationState
    data object Success : SaveSimulationState
    data class Error(val message: String) : SaveSimulationState
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

    private val _saveState = MutableStateFlow<SaveSimulationState>(SaveSimulationState.Idle)
    val saveState: StateFlow<SaveSimulationState> = _saveState.asStateFlow()

    var patientName: String = ""
        private set
    var patientAge: String = ""
        private set
    var photoUri: Uri? = null
        private set
    fun setPatientData(name: String, age: String) {
        patientName = name
        patientAge = age
    }

    fun setPhoto(uri: Uri?) {
        photoUri = uri
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

                val description = try {
                    smileAnalyzer.analyzeSmile(bitmap)
                } catch (e: Throwable) {
                    _state.value = SimulationState.Error(
                        e.message ?: "Error al procesar con IA. Revisa la conexión e intenta de nuevo."
                    )
                    return@launch
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
                    // Fallback: save to cache so we have a reliable loadable URI
                    try {
                        java.io.File(appContext.cacheDir, "camera_photos").mkdirs()
                        val cacheFile = java.io.File(appContext.cacheDir, "camera_photos/sim_$patientId.jpg")
                        cacheFile.writeBytes(photoBytes)
                        FileProvider.getUriForFile(
                            appContext,
                            "${appContext.packageName}.fileprovider",
                            cacheFile
                        ).toString()
                    } catch (_: Exception) {
                        uri.toString()
                    }
                }

                // Con Gemini: misma imagen antes/después (IA solo aporta texto).
                // Modo Mock: imagen de ejemplo para slider visual.
                val afterUrl = if (isGeminiActive) {
                    beforeUrl
                } else {
                    "android.resource://${appContext.packageName}/drawable/ic_sample_dental_after"
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
                    patientName = patientName,
                    patientId = patientId,
                    patientAge = patientAge
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
                patientName = s.patientName,
                patientId = s.patientId,
                patientAge = s.patientAge
            )
            else -> s
        }
    }

    fun saveSimulationResult(finalDescription: String) {
        val successState = _state.value as? SimulationState.Success ?: return
        viewModelScope.launch {
            _saveState.value = SaveSimulationState.Saving
            try {
                val patient = Patient(
                    id = successState.patientId,
                    name = successState.patientName,
                    age = successState.patientAge.toIntOrNull() ?: 0,
                    phone = "",
                    email = "",
                    fotoUrl = successState.result.beforeImageUrl
                )
                val savePatientResult = smileRepository.savePatientProfile(patient, successState.result.beforeImageUrl)
                if (savePatientResult.isFailure) {
                    throw savePatientResult.exceptionOrNull() ?: IllegalStateException("No se pudo guardar el paciente")
                }
                val designImageUrl = if (successState.result.afterImageUrl.startsWith("android.resource")) {
                    successState.result.beforeImageUrl
                } else {
                    successState.result.afterImageUrl
                }
                val saveDesignResult = smileRepository.saveDesignToExpediente(
                    patientId = successState.patientId,
                    designImageUrl = designImageUrl,
                    description = finalDescription
                )
                if (saveDesignResult.isFailure) {
                    throw saveDesignResult.exceptionOrNull() ?: IllegalStateException("No se pudo guardar el diseño")
                }
                _saveState.value = SaveSimulationState.Success
            } catch (e: Exception) {
                _saveState.value = SaveSimulationState.Error(
                    e.message ?: "Error al guardar"
                )
            }
        }
    }

    fun clearSaveState() {
        _saveState.value = SaveSimulationState.Idle
    }

    fun reset() {
        _state.value = SimulationState.Idle
        patientName = ""
        patientAge = ""
        photoUri = null
    }

}
