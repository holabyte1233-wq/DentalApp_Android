package unab.edu.co.abrahamcaceres.dentalapp_android.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import unab.edu.co.abrahamcaceres.dentalapp_android.presentation.login.LoginViewModel
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.components.ClockDisplay
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.CardWhite
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.DestructiveRed
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.RoyalBlue
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.SystemGray6

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLogin: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isRegisterMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingName by remember { mutableStateOf("") }
    var pendingEmail by remember { mutableStateOf("") }
    var pendingPassword by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.savedEmail, uiState.savedPassword) {
        if (email.isEmpty() && uiState.savedEmail.isNotEmpty()) {
            email = uiState.savedEmail
        }
        if (password.isEmpty() && uiState.savedPassword.isNotEmpty()) {
            password = uiState.savedPassword
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (!uiState.isCheckingSession && uiState.isSuccess) {
            if (rememberMe) {
                viewModel.saveCredentials(email, password)
            }
            onLogin()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    if (uiState.needsEmailConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissEmailConfirmation() },
            title = { Text("Cuenta creada") },
            text = {
                Column {
                    Text("✅ Cuenta creada. Revisa tu correo electrónico para activarla antes de entrar.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Haz clic en el enlace del correo que te enviamos.")
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissEmailConfirmation() }) {
                    Text("Entendido", color = RoyalBlue)
                }
            }
        )
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Verifica tus datos") },
            text = {
                Column {
                    Text("Nombre: $pendingName")
                    Text("Email: $pendingEmail")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("¿Es correcto este correo? Recibirás un enlace de confirmación.")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    viewModel.signUp(pendingName, pendingEmail, pendingPassword)
                }) {
                    Text("Confirmar", color = RoyalBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Corregir", color = DestructiveRed)
                }
            }
        )
    }

    fun validateAndSubmit() {
        if (isRegisterMode) {
            if (viewModel.validateForRegister(name, email, password)) {
                pendingName = name
                pendingEmail = email
                pendingPassword = password
                showConfirmDialog = true
            }
        } else {
            if (viewModel.validateForLogin(email, password)) {
                viewModel.login(email, password)
            }
        }
    }

    val nameError = uiState.nameError
    val emailError = uiState.emailError
    val passwordError = uiState.passwordError

    if (uiState.isCheckingSession) {
        Box(
            modifier = modifier.fillMaxSize().background(SystemGray6),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SystemGray6)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        ClockDisplay(modifier = Modifier.padding(vertical = 24.dp))
        Spacer(modifier = Modifier.height(24.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = !isRegisterMode,
                onClick = {
                    isRegisterMode = false
                    viewModel.clearError()
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = Color.Black,
                    activeContentColor = Color.White,
                    inactiveContainerColor = CardWhite,
                    inactiveContentColor = Color.Black
                )
            ) { Text("Iniciar Sesión") }
            SegmentedButton(
                selected = isRegisterMode,
                onClick = {
                    isRegisterMode = true
                    viewModel.clearError()
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = Color.Black,
                    activeContentColor = Color.White,
                    inactiveContainerColor = CardWhite,
                    inactiveContentColor = Color.Black
                )
            ) { Text("Registrarse") }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
            visible = isRegisterMode,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardWhite)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; viewModel.clearError() },
                    label = { Text("Nombre") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it, color = DestructiveRed) } },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        errorBorderColor = DestructiveRed,
                        errorSupportingTextColor = DestructiveRed,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CardWhite)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; viewModel.clearError() },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null, modifier = Modifier.size(20.dp)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it, color = DestructiveRed) } },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    errorBorderColor = DestructiveRed,
                    errorSupportingTextColor = DestructiveRed,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CardWhite)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; viewModel.clearError() },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it, color = DestructiveRed) } },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    errorBorderColor = DestructiveRed,
                    errorSupportingTextColor = DestructiveRed,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isRegisterMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = androidx.compose.material3.CheckboxDefaults.colors(
                        checkedColor = RoyalBlue,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )
                Text(
                    text = "Recordar mis datos",
                    modifier = Modifier.clickable { rememberMe = !rememberMe },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = if (isRegisterMode) "¿Ya tienes cuenta? Inicia Sesión" else "¿No tienes cuenta? Regístrate",
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isRegisterMode = !isRegisterMode
                    viewModel.clearError()
                },
            style = MaterialTheme.typography.bodySmall,
            color = RoyalBlue
        )

        Spacer(modifier = Modifier.height(8.dp))

        ScaleButton(
            text = if (isRegisterMode) "Registrarse" else "Iniciar Sesión",
            onClick = { validateAndSubmit() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            isLoading = uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        SnackbarHost(hostState = snackbarHostState)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ScaleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier,
        enabled = !isLoading,
        shape = RoundedCornerShape(24.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            disabledContainerColor = Color.Black.copy(alpha = 0.5f)
        ),
        content = {
            if (isLoading) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = text, color = Color.White)
            }
        }
    )
}
