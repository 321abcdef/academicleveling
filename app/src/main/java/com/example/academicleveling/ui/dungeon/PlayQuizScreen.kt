package com.example.academicleveling.ui.dungeon

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academicleveling.data.*
import com.example.academicleveling.ui.dungeon.components.*
import com.example.academicleveling.ui.shared.*
import com.example.academicleveling.ui.theme.*

@Composable
fun PlayQuizScreen(
    quiz:   Quiz,
    onBack: () -> Unit
) {
    val hasTimer = quiz.timerMode != QuizTimerMode.NONE

    val questions = remember(quiz.id) {
        val qs = if (quiz.shuffleQuestions) quiz.questions.shuffled() else quiz.questions
        if (quiz.shuffleOptions) {
            qs.map { q ->
                if (q.type == QuizType.MULTIPLE_CHOICE && q.opts.isNotEmpty()) {
                    val indexed  = q.opts.mapIndexed { i, opt -> i to opt }
                    val shuffled = indexed.shuffled()
                    q.copy(
                        opts    = shuffled.map { it.second },
                        correct = shuffled.indexOfFirst { it.first == q.correct }
                    )
                } else q
            }
        } else qs
    }

    var qIndex            by remember { mutableStateOf(0) }
    var selectedOption    by remember { mutableStateOf<Int?>(null) }
    var submitted         by remember { mutableStateOf(false) }
    var identInput        by remember { mutableStateOf("") }
    var answers           by remember { mutableStateOf(listOf<AnswerRecord>()) }
    var showResults       by remember { mutableStateOf(false) }
    var correctStreak     by remember { mutableStateOf(0) }
    var streakBonusMsg    by remember { mutableStateOf("") }
    var wholeQuizSecs     by remember { mutableStateOf(quiz.timerSeconds) }
    var perQuestionSecs   by remember { mutableStateOf(quiz.timerSeconds) }
    var eliminatedOptions by remember { mutableStateOf(setOf<Int>()) }
    var fiftyFiftyUsed    by remember { mutableStateOf(false) }

    // Rewards received from API after submission
    var apiExpEarned   by remember { mutableStateOf(0) }
    var apiCoinsEarned by remember { mutableStateOf(0) }
    var apiAttemptId   by remember { mutableStateOf<Int?>(null) }

    // Start attempt when screen opens
    LaunchedEffect(quiz.id) {
        ApiRepository.startAttempt(
            quizId    = quiz.id,
            onSuccess = { response -> apiAttemptId = response.attemptId },
            onError   = { /* attempt not started, will fall back to local */ }
        )
    }

    fun correctTextFor(q: QuizQuestion): String = when (q.type) {
        QuizType.MULTIPLE_CHOICE -> q.opts.getOrElse(q.correct) { "" }
        QuizType.TRUE_FALSE      -> if (q.correct == 0) "True" else "False"
        QuizType.IDENTIFICATION  -> q.identAnswer.ifBlank { q.exp }.ifBlank { q.opts.getOrElse(q.correct) { "" } }
        else                     -> q.identAnswer.ifBlank { q.exp }.ifBlank { q.opts.getOrElse(q.correct) { "" } }
    }

    /** Submit quiz to API and apply rewards from response. Falls back to local if API fails. */
    fun finishQuiz(finalAnswers: List<AnswerRecord>) {
        val attemptId = apiAttemptId
        if (attemptId != null) {
            ApiRepository.submitAttempt(
                attemptId = attemptId,
                onSuccess = { response ->
                    val rewards = response.data.rewards
                    apiExpEarned   = rewards.exp
                    apiCoinsEarned = rewards.coins
                    // Apply rewards from API
                    AppState.addXP(rewards.exp)
                    AppState.addCoins(rewards.coins)
                    // Record quiz for history/achievements (no local reward calc)
                    AppState.recordQuizResult(quiz, finalAnswers.count { it.wasRight }, finalAnswers)
                    showResults = true
                },
                onError = {
                    // Fallback: compute locally if API fails
                    AppState.recordQuizResult(quiz, finalAnswers.count { it.wasRight }, finalAnswers)
                    apiExpEarned   = quiz.exp
                    apiCoinsEarned = finalAnswers.count { it.wasRight } * 5
                    showResults = true
                }
            )
        } else {
            // No attempt ID (API unavailable) — fallback to local
            AppState.recordQuizResult(quiz, finalAnswers.count { it.wasRight }, finalAnswers)
            apiExpEarned   = quiz.exp
            apiCoinsEarned = finalAnswers.count { it.wasRight } * 5
            showResults = true
        }
    }

    fun applyTimeWarp() {
        if (AppState.timeWarpCount <= 0 || !hasTimer) return
        AppState.timeWarpCount--
        SoundManager.timerStart()
        when (quiz.timerMode) {
            QuizTimerMode.WHOLE_QUIZ   -> wholeQuizSecs   += 30
            QuizTimerMode.PER_QUESTION -> perQuestionSecs += 30
            else -> {}
        }
    }

    fun applyFiftyFifty() {
        val q = questions.getOrNull(qIndex) ?: return
        if (AppState.secondChanceCount <= 0 || submitted || fiftyFiftyUsed) return
        if (q.type != QuizType.MULTIPLE_CHOICE || q.opts.size < 4) return
        AppState.secondChanceCount--
        SoundManager.click()
        fiftyFiftyUsed = true
        val wrongIndices = q.opts.indices.filter { it != q.correct }.shuffled().take(2)
        eliminatedOptions = wrongIndices.toSet()
        if (selectedOption in eliminatedOptions) selectedOption = null
    }

    fun applyHint() {
        val q = questions.getOrNull(qIndex) ?: return
        if (AppState.hintCount <= 0 || submitted) return
        AppState.hintCount--
        SoundManager.hint()
        when (q.type) {
            QuizType.MULTIPLE_CHOICE,
            QuizType.TRUE_FALSE     -> selectedOption = q.correct
            QuizType.IDENTIFICATION -> {
                identInput = q.identAnswer.ifBlank { q.exp }.ifBlank { q.opts.getOrElse(q.correct) { "" } }
            }
            else -> {}
        }
    }

    LaunchedEffect(quiz.timerMode, showResults) {
        if (quiz.timerMode == QuizTimerMode.WHOLE_QUIZ && !showResults) {
            while (wholeQuizSecs > 0 && !showResults) {
                kotlinx.coroutines.delay(1000L)
                wholeQuizSecs--
            }
            if (wholeQuizSecs == 0 && !showResults) {
                finishQuiz(answers)
            }
        }
    }

    LaunchedEffect(qIndex, quiz.timerMode) {
        if (quiz.timerMode == QuizTimerMode.PER_QUESTION && !submitted) {
            perQuestionSecs = quiz.timerSeconds
            while (perQuestionSecs > 0 && !submitted) {
                kotlinx.coroutines.delay(1000L)
                perQuestionSecs--
            }
            if (perQuestionSecs == 0 && !submitted) {
                submitted = true
                val q = questions[qIndex]
                answers = answers + AnswerRecord(
                    question    = q.q,
                    chosen      = -1,
                    correct     = q.correct,
                    wasRight    = false,
                    type        = q.type,
                    identAnswer = correctTextFor(q),
                    chosenText  = "(time up)"
                )
            }
        }
    }

    if (showResults) {
        QuizResultScreen(
            quiz       = quiz,
            answers    = answers,
            expEarned  = apiExpEarned,
            coinsEarned = apiCoinsEarned,
            onBack     = onBack
        )
        return
    }

    val q = questions[qIndex]

    Box(Modifier.fillMaxSize().background(Color(0xFF0D0D1A))) {
        Column(Modifier.fillMaxSize()) {

            // ── Top bar ────────────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth().background(Color(0xFF12122A))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2A2A4A))
                        .border(1.dp, Color.White.copy(.2f), RoundedCornerShape(8.dp))
                        .clickableSound { onBack() },
                    Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        quiz.title.take(24).uppercase(),
                        fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White
                    )
                    Text(
                        "${qIndex + 1} / ${questions.size}",
                        fontSize = 10.sp, color = Teal
                    )
                }

                // Timer display
                when (quiz.timerMode) {
                    QuizTimerMode.WHOLE_QUIZ -> TimerBadge(wholeQuizSecs)
                    QuizTimerMode.PER_QUESTION -> TimerBadge(perQuestionSecs)
                    else -> {
                        // Correct streak display
                        val streakColor = if (correctStreak >= 3) Gold else Teal
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Whatshot, null, tint = streakColor, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(3.dp))
                                Text("$correctStreak", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = streakColor)
                            }
                            Text("streak", fontSize = 9.sp, color = Color.White.copy(.6f))
                        }
                    }
                }
            }

            // ── Streak bonus banner ────────────────────────────────────────
            AnimatedVisibility(visible = streakBonusMsg.isNotBlank()) {
                Box(Modifier.fillMaxWidth().background(Color(0xFF2A2000)).padding(vertical = 8.dp), Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Whatshot, null, tint = Gold, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(streakBonusMsg, fontSize = 12.sp, color = Gold, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            // ── Scrollable quiz content ────────────────────────────────────
            Column(
                Modifier.fillMaxWidth().weight(1f)
                    .verticalScroll(rememberScrollState())
                    .background(Color(0xFF0D0D1A))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Progress bar
                Box(
                    Modifier.fillMaxWidth().height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF2A2A4A))
                ) {
                    val progress = (qIndex + 1).toFloat() / questions.size
                    Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(Teal))
                }

                QuizQuestionCard(
                    q = q,
                    qIndex = qIndex,
                    submitted = submitted,
                    selectedOption = selectedOption,
                    identInput = identInput,
                    eliminatedOptions = eliminatedOptions,
                    onOptionSelect = { selectedOption = it },
                    onIdentChange = { identInput = it }
                )

                Spacer(Modifier.height(8.dp))

                if (!submitted) {
                    TealButton(
                        label = "SUBMIT ANSWER",
                        enabled = (q.type == QuizType.IDENTIFICATION && identInput.isNotBlank()) ||
                                (q.type != QuizType.IDENTIFICATION && selectedOption != null),
                        onClick = {
                            submitted = true
                            val isRight = when (q.type) {
                                QuizType.IDENTIFICATION -> identInput.trim().equals(q.identAnswer.ifBlank { q.exp }.ifBlank { q.opts.getOrElse(q.correct) { "" } }.trim(), ignoreCase = true)
                                else -> selectedOption == q.correct
                            }

                            if (isRight) {
                                SoundManager.correct()
                                correctStreak++
                                if (correctStreak >= 3) {
                                    val bonus = correctStreak * 2
                                    streakBonusMsg = "STREAK BONUS: +$bonus XP!"
                                    AppState.addXP(bonus)
                                }
                            } else {
                                SoundManager.wrong()
                                correctStreak = 0
                                streakBonusMsg = ""
                            }

                            answers = answers + AnswerRecord(
                                question = q.q,
                                chosen = selectedOption ?: -1,
                                correct = q.correct,
                                wasRight = isRight,
                                type = q.type,
                                identAnswer = correctTextFor(q),
                                chosenText = if (q.type == QuizType.IDENTIFICATION) identInput else (q.opts.getOrElse(selectedOption ?: -1) { "" })
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    val isLast = qIndex == questions.size - 1
                    TealButton(
                        label = if (isLast) "FINISH QUIZ" else "NEXT QUESTION",
                        color = if (isLast) Gold else Teal,
                        textColor = if (isLast) Color(0xFF1A2332) else Color.White,
                        onClick = {
                            if (isLast) {
                                finishQuiz(answers)
                            } else {
                                qIndex++
                                selectedOption = null
                                submitted = false
                                identInput = ""
                                eliminatedOptions = emptySet()
                                fiftyFiftyUsed = false
                                streakBonusMsg = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ── Bottom power-up bar ────────────────────────────────────────
            QuizPowerUpBar(
                hasTimer = hasTimer,
                submitted = submitted,
                fiftyFiftyUsed = fiftyFiftyUsed,
                isMcQuestion = q.type == QuizType.MULTIPLE_CHOICE && q.opts.size >= 4,
                onTimeWarp = { applyTimeWarp() },
                onFiftyFifty = { applyFiftyFifty() },
                onHint = { applyHint() }
            )
        }
    }
}

@Composable
private fun TimerBadge(seconds: Int) {
    val timerColor = if (seconds <= 10) DangerRed else Teal
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Timer, null, tint = timerColor, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(3.dp))
            Text(
                "%02d:%02d".format(seconds / 60, seconds % 60),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = timerColor
            )
        }
        Text("timer", fontSize = 9.sp, color = Color.White.copy(.6f))
    }
}

private fun Modifier.clickableSound(onClick: () -> Unit): Modifier = this.clickable { onClick() }
