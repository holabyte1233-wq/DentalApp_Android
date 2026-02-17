package unab.edu.co.abrahamcaceres.dentalapp_android.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import unab.edu.co.abrahamcaceres.dentalapp_android.data.Patient
import unab.edu.co.abrahamcaceres.dentalapp_android.domain.repository.AuthRepository
import unab.edu.co.abrahamcaceres.dentalapp_android.domain.repository.PatientRepository

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(val patients: List<Patient>) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadPatients()
    }

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onDone()
        }
    }

    fun loadPatients() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                val patients = patientRepository.getPatients()
                _uiState.value = DashboardUiState.Success(patients)
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(
                    e.message ?: "Error al cargar pacientes"
                )
            }
        }
    }
}
