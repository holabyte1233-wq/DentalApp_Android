package unab.edu.co.abrahamcaceres.dentalapp_android.presentation.smile

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import unab.edu.co.abrahamcaceres.dentalapp_android.data.Patient
import unab.edu.co.abrahamcaceres.dentalapp_android.data.remote.GeminiService
import unab.edu.co.abrahamcaceres.dentalapp_android.data.repository.SmileRepositoryImpl
import unab.edu.co.abrahamcaceres.dentalapp_android.domain.repository.PatientRepository

sealed interface SmileUiState {
    data object Idle : SmileUiState
    data object Loading : SmileUiState
    data class Success(val generatedImage: String) : SmileUiState
    data class Error(val message: String) : SmileUiState
}

sealed interface SaveDesignState {
    data object Idle : SaveDesignState
    data object Loading : SaveDesignState
    data object Success : SaveDesignState
    data class Error(val message: String) : SaveDesignState
}

@HiltViewModel
class SmileDesignViewModel @Inject constructor(
    private val smileRepository: SmileRepositoryImpl,
    private val patientRepository: PatientRepository,
    private val geminiService: GeminiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<SmileUiState>(SmileUiState.Idle)
    val uiState: StateFlow<SmileUiState> = _uiState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveDesignState>(SaveDesignState.Idle)
    val saveState: StateFlow<SaveDesignState> = _saveState.asStateFlow()

    fun processSmile(
        bitmap: Bitmap,
        patientName: String,
        email: String = "",
        cedula: String = ""
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = SmileUiState.Loading

                if (email.isNotBlank() || cedula.isNotBlank()) {
                    val exists = patientRepository.checkPatientExists(
                        email = email.ifBlank { "" },
                        cedula = cedula.ifBlank { "" }
                    )
                    if (exists) {
                        _uiState.value = SmileUiState.Error("El paciente ya está registrado")
                        return@launch
                    }
                }

                val patientId = UUID.randomUUID().toString()

                val photoBytes = withContext(Dispatchers.Default) {
                    bitmapToJpegByteArray(bitmap, quality = 80)
                }

                val photoUrl = smileRepository.uploadOriginalPhoto(
                    photoBytes = photoBytes,
                    patientId = patientId
                )

                val patient = Patient(
                    id = patientId,
                    name = patientName,
                    age = 0,
                    phone = "",
                    email = email,
                    fotoUrl = photoUrl
                )

                val saveResult = smileRepository.savePatientProfile(patient, photoUrl)
                if (saveResult.isFailure) {
                    throw saveResult.exceptionOrNull()
                        ?: IllegalStateException("No se pudo guardar el perfil del paciente")
                }

                val generatedImage = geminiService.generateSmile(bitmap)

                _uiState.value = SmileUiState.Success(generatedImage)
            } catch (e: Exception) {
                _uiState.value = SmileUiState.Error(
                    e.message ?: "Ocurrió un error procesando la sonrisa"
                )
            }
        }
    }

    /** Guarda el diseño actual en el expediente del paciente. Solo llamar tras confirmación en UI. */
    fun saveDesign(patientId: String, designImageUrl: String) {
        viewModelScope.launch {
            try {
                _saveState.value = SaveDesignState.Loading
                val result = smileRepository.saveDesignToExpediente(patientId, designImageUrl)
                if (result.isSuccess) {
                    _saveState.value = SaveDesignState.Success
                } else {
                    _saveState.value = SaveDesignState.Error(
                        result.exceptionOrNull()?.message ?: "Error al guardar"
                    )
                }
            } catch (e: Exception) {
                _saveState.value = SaveDesignState.Error(
                    e.message ?: "Error al guardar en expediente"
                )
            }
        }
    }

    fun clearSaveState() {
        _saveState.value = SaveDesignState.Idle
    }

    private fun bitmapToJpegByteArray(bitmap: Bitmap, quality: Int): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }
}
