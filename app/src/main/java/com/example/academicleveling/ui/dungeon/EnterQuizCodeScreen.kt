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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.academicleveling.data.Difficulty
import com.example.academicleveling.data.Quiz
import com.example.academicleveling.data.QuizTimerMode
import com.example.academicleveling.data.QuizType
import com.example.academicleveling.ui.shared.ActionChip
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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
@Composable
fun EnterQuizCodeScreen(
    onBack: () -> Unit,
    onFound: (Quiz) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Quiz>>(emptyList()) }

    // Debounced search logic
    val searchFlow = remember { MutableStateFlow("") }
    LaunchedEffect(code) {
        searchFlow.value = code
    }
    LaunchedEffect(searchFlow) {
        searchFlow.debounce(600L).collectLatest { query ->
            if (query.length >= 8) {
                isLoading = true
                ApiRepository.getQuizzes(
                    search = query,
                    onSuccess = { response ->
                        isLoading = false
                        searchResults = response.data.map { apiQuiz ->
                            Quiz(
                                id = apiQuiz.id,
                                title = apiQuiz.title,
                                description = apiQuiz.description,
                                creator = apiQuiz.user.name,
                                creatorName = apiQuiz.user.name,
                                questions = emptyList(),
                                questionsCount = apiQuiz.questionsCount,
                                exp = apiQuiz.questionsCount * 20,
                                quizType = when(apiQuiz.type.lowercase().trim()) {
                                    "multiple_choice" -> QuizType.MULTIPLE_CHOICE
                                    "true_false"      -> QuizType.TRUE_FALSE
                                    "identification"  -> QuizType.IDENTIFICATION
                                    else              -> QuizType.MIX
                                },
                                timerMode = when(apiQuiz.timerMode.lowercase().trim()) {
                                    "quiz" -> QuizTimerMode.WHOLE_QUIZ
                                    "question" -> QuizTimerMode.PER_QUESTION
                                    else -> QuizTimerMode.NONE
                                },
                                timerSeconds = when(apiQuiz.timerMode.lowercase().trim()) {
                                    "quiz" -> apiQuiz.questionsCount * 30
                                    "question" -> 30
                                    else -> 0
                                },
                                subject = apiQuiz.subject,
                                gradeLevel = apiQuiz.gradeLevel,
                                difficulty = when(apiQuiz.difficulty.lowercase()) {
                                    "easy" -> Difficulty.EASY
                                    "hard" -> Difficulty.HARD
                                    else -> Difficulty.MEDIUM
                                },
                                code = apiQuiz.quizCode,
                                dateCreated = apiQuiz.createdAt.split("T").firstOrNull() ?: "",
                                shuffleQuestions = apiQuiz.isQuestionShuffled,
                                shuffleOptions = apiQuiz.isChoicesShuffled
                            )
                        }
                    },
                    onError = {
                        isLoading = false
                    }
                )
            } else {
                searchResults = emptyList()
            }
        }
    }

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
                                code = it.uppercase()
                                error = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search by title or code...", color = TextMuted) },
                            singleLine = true,
                            isError = error.isNotBlank(),
                            enabled = !isLoading,
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
                                textColor = TextPrimary,
                                enabled = !isLoading
                            )
                            TealButton(
                                label = "FIND QUIZ",
                                onClick = {
                                    isLoading = true
                                    error = ""
                                    ApiRepository.getQuizzes(
                                        search = code,
                                        onSuccess = { response ->
                                            isLoading = false
                                            val apiQuiz = response.data.find { it.quizCode.equals(code, ignoreCase = true) }
                                            if (apiQuiz != null) {
                                                SoundManager.navigate()
                                                // Map to local Quiz model
                                                val mapped = Quiz(
                                                    id = apiQuiz.id,
                                                    title = apiQuiz.title,
                                                    description = apiQuiz.description,
                                                    creator = apiQuiz.user.name,
                                                    creatorName = apiQuiz.user.name,
                                                    questions = emptyList(),
                                                    questionsCount = apiQuiz.questionsCount,
                                                    exp = apiQuiz.questionsCount * 20,
                                                    quizType = when(apiQuiz.type.lowercase().trim()) {
                                                        "multiple_choice" -> QuizType.MULTIPLE_CHOICE
                                                        "true_false"      -> QuizType.TRUE_FALSE
                                                        "identification"  -> QuizType.IDENTIFICATION
                                                        else              -> QuizType.MIX
                                                    },
                                                    timerMode = when(apiQuiz.timerMode.lowercase().trim()) {
                                                        "quiz" -> QuizTimerMode.WHOLE_QUIZ
                                                        "question" -> QuizTimerMode.PER_QUESTION
                                                        else -> QuizTimerMode.NONE
                                                    },
                                                    timerSeconds = when(apiQuiz.timerMode.lowercase().trim()) {
                                                        "quiz" -> apiQuiz.questionsCount * 30
                                                        "question" -> 30
                                                        else -> 0
                                                    },
                                                    subject = apiQuiz.subject,
                                                    gradeLevel = apiQuiz.gradeLevel,
                                                    difficulty = when(apiQuiz.difficulty.lowercase()) {
                                                        "easy" -> Difficulty.EASY
                                                        "hard" -> Difficulty.HARD
                                                        else -> Difficulty.MEDIUM
                                                    },
                                                    code = apiQuiz.quizCode,
                                                    dateCreated = apiQuiz.createdAt.split("T").firstOrNull() ?: "",
                                                    shuffleQuestions = apiQuiz.isQuestionShuffled,
                                                    shuffleOptions = apiQuiz.isChoicesShuffled
                                                )
                                                onFound(mapped)
                                            } else {
                                                SoundManager.error()
                                                error = "Code not found. Check and try again."
                                            }
                                        },
                                        onError = { err ->
                                            isLoading = false
                                            error = "Search failed: $err"
                                            SoundManager.error()
                                        }
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                enabled = code.isNotBlank() && !isLoading
                            )
                        }
                        
                        if (isLoading && searchResults.isEmpty()) {
                            Box(Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Teal, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }

                        if (searchResults.isNotEmpty()) {
                            Text("SEARCH RESULTS", color = Teal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            searchResults.forEach { quiz ->
                                QuizCard(quiz = quiz, showCode = true) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("By: ${quiz.creator}", fontSize = 11.sp, color = TextSecondary)
                                        ActionChip("PLAY", SuccessGreen) { onFound(quiz) }
                                    }
                                }
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