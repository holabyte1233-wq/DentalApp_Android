package unab.edu.co.abrahamcaceres.dentalapp_android.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import unab.edu.co.abrahamcaceres.dentalapp_android.domain.repository.AuthRepository

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false) }

        viewModelScope.launch {
            val result = authRepository.login(email, password)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isSuccess = result.getOrDefault(false),
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }
}
