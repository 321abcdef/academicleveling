package com.example.academicleveling.ui.dungeon

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academicleveling.data.ApiRepository
import com.example.academicleveling.data.AppState
import com.example.academicleveling.data.Quiz
import com.example.academicleveling.ui.shared.SoundManager
import com.example.academicleveling.ui.shared.SpaceBackground
import com.example.academicleveling.ui.shared.SubPageBar
import com.example.academicleveling.ui.shared.TealButton
import com.example.academicleveling.ui.theme.DangerRed
import com.example.academicleveling.ui.theme.SuccessGreen
import com.example.academicleveling.ui.theme.Teal
import com.example.academicleveling.ui.theme.TextMuted
import com.example.academicleveling.ui.theme.TextPrimary
import com.example.academicleveling.ui.theme.TextSecondary

@Composable
fun EnterQuizCodeScreen(
    onBack: () -> Unit,
    onFound: (Quiz) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    SpaceBackground {
        Column(Modifier.fillMaxSize()) {
            SubPageBar("ENTER QUIZ CODE", onBack)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1A2E), RoundedCornerShape(12.dp))
                        .border(1.dp, Teal.copy(0.35f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconText("Use a quiz code from classmate or teacher", Teal)
                        }

                        OutlinedTextField(
                            value = code,
                            onValueChange = {
                                code = it.uppercase().take(8)
                                error = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g. CPP001", color = TextMuted) },
                            singleLine = true,
                            isError = error.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Teal,
                                unfocusedBorderColor = Color.White.copy(0.25f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        if (error.isNotBlank()) {
                            Text(error, color = DangerRed, fontSize = 11.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TealButton(
                                label = "CANCEL",
                                onClick = onBack,
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF0D0D1A),
                                textColor = TextPrimary
                            )
                            TealButton(
                                label = if (isLoading) "SEARCHING..." else "FIND QUIZ",
                                onClick = {
                                    val normalizedCode = code.trim()
                                    if (normalizedCode.isBlank()) return@TealButton

                                    isLoading = true
                                    error = ""
                                    ApiRepository.getQuizByCode(
                                        code = normalizedCode,
                                        onSuccess = { apiQuiz ->
                                            if (apiQuiz.questions.isEmpty()) {
                                                val localQuiz = AppState.findByCode(normalizedCode)
                                                isLoading = false
                                                if (localQuiz != null) {
                                                    SoundManager.navigate()
                                                    onFound(localQuiz)
                                                } else {
                                                    SoundManager.error()
                                                    error = "Quiz was found but has no questions yet."
                                                }
                                                return@getQuizByCode
                                            }
                                            isLoading = false
                                            SoundManager.navigate()
                                            onFound(apiQuiz)
                                        },
                                        onError = { apiError ->
                                            val localQuiz = AppState.findByCode(normalizedCode)
                                            isLoading = false
                                            if (localQuiz != null) {
                                                SoundManager.navigate()
                                                onFound(localQuiz)
                                            } else {
                                                SoundManager.error()
                                                error = if (apiError.isBlank()) {
                                                    "Code not found. Check and try again."
                                                } else {
                                                    apiError
                                                }
                                            }
                                        }
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                enabled = code.isNotBlank() && !isLoading
                            )
                        }
                        if (isLoading) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Teal
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF001A1A), RoundedCornerShape(10.dp))
                        .border(1.dp, Teal.copy(0.2f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Icon(Icons.Default.Info, null, tint = Teal, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("How this works", color = Teal, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Text("1. Enter a shared quiz code.", color = TextSecondary, fontSize = 11.sp)
                        Text("2. Tap FIND QUIZ.", color = TextSecondary, fontSize = 11.sp)
                        Text("3. You will proceed to the Play Quiz screen.", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(70.dp))
            }
        }
    }
}

@Composable
private fun IconText(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.material3.Icon(Icons.Default.VpnKey, null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, color = TextPrimary, fontSize = 12.sp)
    }
}