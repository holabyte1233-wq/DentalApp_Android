package unab.edu.co.abrahamcaceres.dentalapp_android.presentation.login

import android.content.Context
import androidx.core.content.edit
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import unab.edu.co.abrahamcaceres.dentalapp_android.domain.repository.AuthRepository

data class LoginUiState(
    val isCheckingSession: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val needsEmailConfirmation: Boolean = false,
    val savedEmail: String = "",
    val savedPassword: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val phoneError: String? = null,
    val nameError: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val securePrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            "secure_login_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    init {
        loadSavedCredentials()
        viewModelScope.launch {
            val hasSession = repository.hasActiveSession()
            _uiState.update {
                it.copy(
                    isCheckingSession = false,
                    isSuccess = hasSession
                )
            }
        }
    }

    private fun isValidEmail(email: String): Boolean =
        email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun isValidPassword(pass: String): Boolean = pass.length > 5

    fun loadSavedCredentials() {
        val email = securePrefs.getString("saved_email", null) ?: ""
        val password = securePrefs.getString("saved_password", null) ?: ""
        if (email.isNotEmpty() || password.isNotEmpty()) {
            _uiState.update {
                it.copy(savedEmail = email, savedPassword = password)
            }
        }
    }

    fun saveCredentials(email: String, password: String) {
        securePrefs.edit {
            putString("saved_email", email)
            putString("saved_password", password)
        }
    }

    @Suppress("UNUSED_MEMBER")
    fun clearSavedCredentials() {
        securePrefs.edit { clear() }
        _uiState.update { it.copy(savedEmail = "", savedPassword = "") }
    }

    fun login(email: String, password: String) {
        if (!validateForLogin(email, password)) return

        _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false, needsEmailConfirmation = false) }

        viewModelScope.launch {
            val result = repository.login(email, password)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isSuccess = result.getOrDefault(false),
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun signUp(email: String, password: String, name: String, phone: String, avatarBytes: ByteArray?) {
        if (!validateForRegister(email, password, name, phone)) return

        _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false, needsEmailConfirmation = false) }

        viewModelScope.launch {
            val result = repository.signUp(email, password, name, phone, avatarBytes)
            result.fold(
                onSuccess = {
                    val hasSession = repository.hasActiveSession()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = hasSession,
                            needsEmailConfirmation = !hasSession,
                            error = null
                        )
                    }
                },
                onFailure = { e ->
                    val errorMessage = if (e.message == "User already exists") {
                        "Este correo ya está registrado. Por favor inicia sesión."
                    } else {
                        e.message
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = errorMessage,
                            needsEmailConfirmation = false
                        )
                    }
                }
            )
        }
    }

    fun dismissEmailConfirmation() {
        _uiState.update { it.copy(needsEmailConfirmation = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null, emailError = null, passwordError = null, phoneError = null, nameError = null) }
    }

    private fun isValidPhone(phone: String): Boolean = phone.isNotBlank()
    private fun isValidName(name: String): Boolean = name.isNotBlank()

    fun validateForRegister(email: String, password: String, name: String, phone: String): Boolean {
        _uiState.update { it.copy(emailError = null, passwordError = null, phoneError = null, nameError = null) }
        if (!isValidEmail(email)) {
            _uiState.update { it.copy(emailError = "Formato de correo inválido") }
            return false
        }
        if (!isValidPassword(password)) {
            _uiState.update { it.copy(passwordError = "La contraseña debe tener más de 5 caracteres") }
            return false
        }
        if (!isValidName(name)) {
            _uiState.update { it.copy(nameError = "El nombre es requerido") }
            return false
        }
        if (!isValidPhone(phone)) {
            _uiState.update { it.copy(phoneError = "El teléfono es requerido") }
            return false
        }
        return true
    }

    fun validateForLogin(email: String, password: String): Boolean {
        _uiState.update { it.copy(emailError = null, passwordError = null) }
        if (!isValidEmail(email)) {
            _uiState.update { it.copy(emailError = "Formato de correo inválido") }
            return false
        }
        if (!isValidPassword(password)) {
            _uiState.update { it.copy(passwordError = "La contraseña debe tener más de 5 caracteres") }
            return false
        }
        return true
    }
}
