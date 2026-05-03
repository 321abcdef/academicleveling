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
    var perQuestionSecs   by remember { mutableIntStateOf(quiz.timerSeconds) }
    var eliminatedOptions by remember { mutableStateOf(setOf<Int>()) }
    var fiftyFiftyUsed    by remember { mutableStateOf(false) }

    // Reset whole quiz timer if quiz changes
    var wholeQuizSecs by remember(quiz.id) { mutableIntStateOf(quiz.timerSeconds) }

    fun correctTextFor(q: QuizQuestion): String = when (q.type) {
        QuizType.MULTIPLE_CHOICE -> q.opts.getOrElse(q.correct) { "" }
        QuizType.TRUE_FALSE      -> if (q.correct == 0) "True" else "False"
        QuizType.IDENTIFICATION  -> q.identAnswer.ifBlank { q.exp }.ifBlank { q.opts.getOrElse(q.correct) { "" } }
        else                     -> q.identAnswer.ifBlank { q.exp }.ifBlank { q.opts.getOrElse(q.correct) { "" } }
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

    LaunchedEffect(quiz.id, showResults) {
        if (quiz.timerMode == QuizTimerMode.WHOLE_QUIZ && !showResults && wholeQuizSecs > 0) {
            while (wholeQuizSecs > 0 && !showResults) {
                kotlinx.coroutines.delay(1000L)
                wholeQuizSecs--
            }
            if (wholeQuizSecs == 0 && !showResults) {
                AppState.recordQuizResult(quiz, answers.count { it.wasRight }, answers)
                showResults = true
            }
        }
    }

    LaunchedEffect(qIndex, quiz.id) {
        if (quiz.timerMode == QuizTimerMode.PER_QUESTION && !submitted) {
            perQuestionSecs = quiz.timerSeconds
            if (perQuestionSecs > 0) {
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
    }

    if (showResults) {
        QuizResultScreen(quiz = quiz, answers = answers, onBack = onBack)
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
                    Text(quiz.title, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 1)
                    Text("${qIndex + 1} / ${questions.size}", fontSize = 11.sp, color = Teal)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (hasTimer) {
                        val displaySecs = when (quiz.timerMode) {
                            QuizTimerMode.WHOLE_QUIZ   -> wholeQuizSecs
                            QuizTimerMode.PER_QUESTION -> perQuestionSecs
                            else -> 0
                        }
                        val timerColor = if (displaySecs <= 10) DangerRed else Teal
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, null, tint = timerColor, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("%02d:%02d".format(displaySecs / 60, displaySecs % 60),
                                fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = timerColor)
                        }
                        Text("quiz countdown", fontSize = 9.sp, color = Color.White.copy(.6f))
                    } else {
                        val streakColor = if (correctStreak >= 3) Gold else Color.White
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Whatshot, null, tint = streakColor, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("$correctStreak", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = streakColor)
                        }
                        Text("streak", fontSize = 9.sp, color = Color.White.copy(.6f))
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
                    Box(
                        Modifier.fillMaxWidth(qIndex.toFloat() / questions.size)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Teal)
                    )
                }

                QuizQuestionCard(
                    q                 = q,
                    qIndex            = qIndex,
                    submitted         = submitted,
                    selectedOption    = selectedOption,
                    identInput        = identInput,
                    eliminatedOptions = eliminatedOptions,
                    onOptionSelect    = { if (!submitted) selectedOption = it },
                    onIdentChange     = { if (!submitted) identInput = it }
                )

                if (!submitted) {
                    val canSubmit = when (q.type) {
                        QuizType.IDENTIFICATION -> identInput.isNotBlank()
                        else                    -> selectedOption != null
                    }
                    TealButton(
                        label    = "SUBMIT ANSWER",
                        enabled  = canSubmit,
                        modifier = Modifier.fillMaxWidth(),
                        onClick  = {
                            SoundManager.click()
                            submitted = true
                            val correctText = correctTextFor(q)
                            val chosenText = when (q.type) {
                                QuizType.MULTIPLE_CHOICE -> q.opts.getOrElse(selectedOption ?: -1) { "" }
                                QuizType.TRUE_FALSE      -> if (selectedOption == 0) "True" else "False"
                                else                     -> identInput.trim()
                            }
                            val isRight = when (q.type) {
                                QuizType.IDENTIFICATION -> identInput.trim().equals(correctText.trim(), ignoreCase = true)
                                else                    -> selectedOption == q.correct
                            }
                            if (isRight) {
                                correctStreak++
                                if (correctStreak % 3 == 0) {
                                    AppState.addCoins(3)
                                    streakBonusMsg = "$correctStreak streak!  +3 coins"
                                }
                            } else {
                                correctStreak = 0
                                streakBonusMsg = ""
                            }
                            answers = answers + AnswerRecord(
                                question    = q.q,
                                chosen      = selectedOption ?: -1,
                                correct     = q.correct,
                                wasRight    = isRight,
                                type        = q.type,
                                identAnswer = correctText,
                                chosenText  = chosenText
                            )
                        }
                    )
                    if (!canSubmit) {
                        Text("Select an answer before submitting",
                            fontSize = 11.sp, color = TextMuted, textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth())
                    }
                } else {
                    val isLast = qIndex == questions.lastIndex
                    TealButton(
                        label    = if (isLast) "FINISH QUIZ" else "NEXT QUESTION →",
                        modifier = Modifier.fillMaxWidth(),
                        onClick  = {
                            SoundManager.click()
                            if (isLast) {
                                AppState.recordQuizResult(quiz, answers.count { it.wasRight }, answers)
                                showResults = true
                            } else {
                                qIndex++
                                selectedOption    = null
                                submitted         = false
                                identInput        = ""
                                streakBonusMsg    = ""
                                eliminatedOptions = emptySet()
                                fiftyFiftyUsed    = false
                                if (quiz.timerMode == QuizTimerMode.PER_QUESTION)
                                    perQuestionSecs = quiz.timerSeconds
                            }
                        }
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            // ── Power-up bar ───────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth().background(Color(0xFF12122A))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                Arrangement.spacedBy(8.dp), Alignment.CenterVertically
            ) {
                Text("POWER-UPS", fontSize = 9.sp, color = Color.White.copy(.7f),
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                Spacer(Modifier.weight(1f))
                PowerUpChip(
                    icon = Icons.Default.AccessTime, iconTint = Blue,
                    count = AppState.timeWarpCount,
                    enabled = hasTimer && AppState.timeWarpCount > 0,
                    tooltip = if (!hasTimer) "No timer" else null,
                    onUse = { applyTimeWarp() }
                )
                val fiftyEnabled = AppState.secondChanceCount > 0 && !submitted && !fiftyFiftyUsed &&
                        q.type == QuizType.MULTIPLE_CHOICE && q.opts.size >= 4
                PowerUpChip(
                    icon = Icons.Default.Security, iconTint = Purple,
                    count = AppState.secondChanceCount, enabled = fiftyEnabled,
                    tooltip = when {
                        q.type != QuizType.MULTIPLE_CHOICE -> "MC only"
                        fiftyFiftyUsed -> "Used"
                        submitted -> "Too late"
                        else -> null
                    },
                    label = "50/50",
                    onUse = { applyFiftyFifty() }
                )
                PowerUpChip(
                    icon = Icons.Default.Lightbulb, iconTint = Gold,
                    count = AppState.hintCount,
                    enabled = AppState.hintCount > 0 && !submitted,
                    tooltip = if (submitted && AppState.hintCount > 0) "Too late" else null,
                    onUse = { applyHint() }
                )
            }
        }
    }
}

private fun Modifier.clickableSound(onClick: () -> Unit): Modifier = this.clickable { onClick() }