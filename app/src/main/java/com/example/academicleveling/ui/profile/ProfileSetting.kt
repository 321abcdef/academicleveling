package com.example.academicleveling.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academicleveling.data.AppState
import com.example.academicleveling.ui.shared.*
import com.example.academicleveling.ui.theme.*

@Composable
fun ProfileSettingsScreen(onBack: () -> Unit) {
    var displayName     by remember { mutableStateOf(AppState.name) }
    var email           by remember { mutableStateOf(AppState.email) }
    var currentPw       by remember { mutableStateOf("") }
    var newPw           by remember { mutableStateOf("") }
    var confirmPw       by remember { mutableStateOf("") }

    var showCurrent     by remember { mutableStateOf(false) }
    var showNew         by remember { mutableStateOf(false) }
    var showConfirm     by remember { mutableStateOf(false) }

    var nameError       by remember { mutableStateOf("") }
    var emailError      by remember { mutableStateOf("") }
    var pwError         by remember { mutableStateOf("") }
    
    var profileMsg      by remember { mutableStateOf("") }
    var passwordMsg     by remember { mutableStateOf("") }
    
    var isProfileLoading by remember { mutableStateOf(false) }
    var isPasswordLoading by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(BgPrimary)) {
        SubPageBar("ACCOUNT SETTINGS", onBack)

        Box(
            Modifier.fillMaxWidth()
                .background(Color(0xFF0D0D1A))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                "Manage personal account information and preferences",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        Column(
            Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Display Name (Icon: Person)
            SettingsSection(title = "PROFILE INFORMATION", icon = Icons.Default.Person) {
                OutlinedTextField(
                    value         = displayName,
                    onValueChange = { displayName = it; nameError = ""; profileMsg = "" },
                    modifier      = Modifier.fillMaxWidth(),
                    label         = { Text("Username", fontSize = 12.sp) },
                    isError       = nameError.isNotBlank(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    colors        = fieldColors()
                )
                if (nameError.isNotBlank())
                    Text(nameError, color = DangerRed, fontSize = 11.sp)

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value         = email,
                    onValueChange = { email = it; emailError = ""; profileMsg = "" },
                    modifier      = Modifier.fillMaxWidth(),
                    label         = { Text("Email", fontSize = 12.sp) },
                    isError       = emailError.isNotBlank(),
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape         = RoundedCornerShape(10.dp),
                    colors        = fieldColors()
                )
                if (emailError.isNotBlank())
                    Text(emailError, color = DangerRed, fontSize = 11.sp)

                Spacer(Modifier.height(16.dp))

                TealButton(
                    label = if (isProfileLoading) "UPDATING..." else "UPDATE PROFILE",
                    onClick = {
                        if (displayName.isBlank()) { nameError = "Name cannot be empty"; return@TealButton }
                        if (email.isBlank() || !email.contains("@")) { emailError = "Enter a valid email"; return@TealButton }

                        isProfileLoading = true
                        profileMsg = ""
                        
                        com.example.academicleveling.data.ApiRepository.updateProfile(
                            name = displayName.trim(),
                            email = email.trim(),
                            onSuccess = { response ->
                                isProfileLoading = false
                                AppState.updateProfileWithApi(response)
                                profileMsg = response.message
                            },
                            onError = { error ->
                                isProfileLoading = false
                                if (error.contains("email", ignoreCase = true)) {
                                    emailError = error
                                } else {
                                    nameError = error
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProfileLoading
                )

                if (profileMsg.isNotBlank()) {
                    Text(
                        profileMsg,
                        color = SuccessGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // ── Password (Icon: Lock)
            SettingsSection(
                title = "CHANGE PASSWORD",
                icon = Icons.Default.Lock,
                subtitle = "Security updates"
            ) {
                PwField("Current Password", currentPw, showCurrent,
                    onToggle = { showCurrent = !showCurrent },
                    onChange = { currentPw = it; pwError = ""; passwordMsg = "" })

                PwField("New Password", newPw, showNew,
                    onToggle = { showNew = !showNew },
                    onChange = { newPw = it; pwError = ""; passwordMsg = "" })

                PwField("Confirm New Password", confirmPw, showConfirm,
                    onToggle = { showConfirm = !showConfirm },
                    onChange = { confirmPw = it; pwError = ""; passwordMsg = "" })

                if (pwError.isNotBlank())
                    Text(pwError, color = DangerRed, fontSize = 11.sp)

                Spacer(Modifier.height(16.dp))

                TealButton(
                    label = if (isPasswordLoading) "CHANGING..." else "CHANGE PASSWORD",
                    onClick = {
                        if (currentPw.isBlank()) { pwError = "Current password is required"; return@TealButton }
                        if (newPw.length < 6) { pwError = "Password must be at least 6 characters"; return@TealButton }
                        if (newPw != confirmPw) { pwError = "Passwords do not match"; return@TealButton }

                        isPasswordLoading = true
                        passwordMsg = ""

                        com.example.academicleveling.data.ApiRepository.changePassword(
                            current = currentPw,
                            newPw = newPw,
                            confirmPw = confirmPw,
                            onSuccess = {
                                isPasswordLoading = false
                                currentPw = ""; newPw = ""; confirmPw = ""
                                passwordMsg = "Password changed successfully!"
                            },
                            onError = { error ->
                                isPasswordLoading = false
                                pwError = error
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isPasswordLoading,
                    color = Color(0xFF6200EE) // Different color for distinction
                )

                if (passwordMsg.isNotBlank()) {
                    Text(
                        passwordMsg,
                        color = SuccessGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // ── Account info (read-only)
            Card(
                colors = CardDefaults.cardColors(containerColor = BgCard),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("ACCOUNT INFO", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
                        color = TextSecondary, letterSpacing = 1.sp)
                    InfoRow("Level", "${AppState.level}")
                    InfoRow("Rank",  "${AppState.rank}")
                    InfoRow("Grade", AppState.grade?.display ?: "Not set")
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// ── Helpers updated to use ImageVector ──────────────────────────────────────

@Composable
private fun SettingsSection(
    title:    String,
    icon:     ImageVector,
    subtitle: String = "",
    content:  @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Teal, modifier = Modifier.size(18.dp)) // Naka-Teal na icon
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(title, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
                        color = TextSecondary, letterSpacing = 1.sp)
                    if (subtitle.isNotBlank())
                        Text(subtitle, fontSize = 9.sp, color = TextMuted)
                }
            }
            content()
        }
    }
}

@Composable
private fun PwField(
    label:    String,
    value:    String,
    visible:  Boolean,
    onToggle: () -> Unit,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onChange,
        modifier      = Modifier.fillMaxWidth(),
        label         = { Text(label, fontSize = 12.sp) },
        singleLine    = true,
        shape         = RoundedCornerShape(10.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon  = {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        colors = fieldColors()
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = TextSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = Teal,
    unfocusedBorderColor = Color.White.copy(0.1f),
    focusedLabelColor    = Teal,
    unfocusedLabelColor  = TextSecondary
)