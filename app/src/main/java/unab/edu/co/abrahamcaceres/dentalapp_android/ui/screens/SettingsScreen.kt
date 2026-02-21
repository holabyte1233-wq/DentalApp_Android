package unab.edu.co.abrahamcaceres.dentalapp_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import unab.edu.co.abrahamcaceres.dentalapp_android.presentation.settings.SettingsViewModel
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.AccentBlue
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.Background
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.ErrorRed
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.Primary
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember(uiState.doctorName) { mutableStateOf(uiState.doctorName) }
    var editEmail by remember(uiState.email) { mutableStateOf(uiState.email.ifBlank { "roberto.sanchez@dentaltech.com" }) }
    var editPhone by remember(uiState.phone) { mutableStateOf(uiState.phone) }

    fun cancelEdit() {
        isEditing = false
        editName = uiState.doctorName
        editEmail = uiState.email.ifBlank { "roberto.sanchez@dentaltech.com" }
        editPhone = uiState.phone
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Top bar: Atrás left, Ajustes center, Salir right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Primary
                )
            }
            Text(
                text = "Ajustes",
                style = MaterialTheme.typography.titleLarge,
                color = Primary
            )
            TextButton(onClick = { viewModel.signOut(onLogout) }) {
                Text("Salir", color = ErrorRed, fontWeight = FontWeight.Medium)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Profile header card
            val context = LocalContext.current
            val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    context.contentResolver.openInputStream(it)?.use { stream ->
                        viewModel.updateAvatar(stream.readBytes())
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AccentBlue)
                        .clickable { galleryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    val avatarUrl = uiState.avatarUrl
                    if (avatarUrl != null) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = Color.White
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text(
                        text = if (isEditing) editName else uiState.doctorName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = {
                        isEditing = !isEditing
                        if (!isEditing) cancelEdit()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isEditing) AccentBlue.copy(alpha = 0.2f) else Background)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        modifier = Modifier.size(20.dp),
                        tint = if (isEditing) AccentBlue else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info fields - editable when isEditing, read-only otherwise
            val readOnlyBg = Color.White
            val editModeBg = Color.White
            val readOnlyBorder = Color.Transparent
            val editModeBorder = AccentBlue.copy(alpha = 0.5f)

            if (isEditing) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(2.dp, editModeBorder, RoundedCornerShape(24.dp))
                        .background(editModeBg)
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = editModeBorder,
                            focusedContainerColor = editModeBg,
                            unfocusedContainerColor = editModeBg
                        )
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(2.dp, editModeBorder, RoundedCornerShape(24.dp))
                        .background(editModeBg)
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = editModeBorder,
                            focusedContainerColor = editModeBg,
                            unfocusedContainerColor = editModeBg
                        )
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(2.dp, editModeBorder, RoundedCornerShape(24.dp))
                        .background(editModeBg)
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = editModeBorder,
                            focusedContainerColor = editModeBg,
                            unfocusedContainerColor = editModeBg
                        )
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { cancelEdit() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = TextSecondary.copy(alpha = 0.3f)
                        )
                    ) {
                        Text("Cancelar", color = TextSecondary)
                    }
                    Button(
                        onClick = {
                            viewModel.updateProfile(editName, editPhone)
                            isEditing = false
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = AccentBlue
                        )
                    ) {
                        Text("Guardar", color = Color.White)
                    }
                }
            } else {
                val maskedPhone = maskPhone(uiState.phone)
                val displayEmail = uiState.email.ifBlank { "roberto.sanchez@dentaltech.com" }
                SettingsInfoCard(label = "Nombre", value = uiState.doctorName, readOnlyBg = readOnlyBg)
                Spacer(modifier = Modifier.height(12.dp))
                SettingsInfoCard(label = "Email", value = displayEmail, readOnlyBg = readOnlyBg)
                Spacer(modifier = Modifier.height(12.dp))
                SettingsInfoCard(label = "Teléfono", value = maskedPhone, readOnlyBg = readOnlyBg)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsInfoCard(
    label: String,
    value: String,
    readOnlyBg: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(readOnlyBg)
            .padding(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = Primary,
            fontWeight = FontWeight.Medium
        )
    }
}

/** Masks phone digits with "*", keeping only the last 2 digits visible. */
private fun maskPhone(phone: String): String {
    if (phone.isBlank()) return ""

    var visibleDigits = 4
    return buildString {
        for (c in phone) {
            if (c.isDigit()) {
                append(if (visibleDigits > 0) {
                    visibleDigits--
                    c
                } else '*')
            } else {
                append(c)
            }
        }
    }
}