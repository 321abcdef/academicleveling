package com.example.academicleveling.ui.quiz_history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
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
import androidx.compose.ui.window.Dialog
import com.example.academicleveling.data.AppState
import com.example.academicleveling.data.AnswerRecord
import com.example.academicleveling.data.QuizHistoryEntry
import com.example.academicleveling.data.QuizType
import com.example.academicleveling.ui.shared.*
import com.example.academicleveling.ui.theme.*

private const val PAGE_SIZE = 10

@Composable
fun QuizHistoryScreen(
    onBack:  () -> Unit,
    onRetry: ((String) -> Unit)? = null
) {
    var expanded        by remember { mutableStateOf<Int?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }
    var visibleCount    by remember { mutableStateOf(PAGE_SIZE) }

    val history = AppState.quizHistory
    LaunchedEffect(history.size) {
        visibleCount = PAGE_SIZE
        expanded     = null
    }

    if (showClearDialog) {
        ClearHistoryDialog(
            count     = history.size,
            onConfirm = {
                AppState.quizHistory = emptyList()
                AppState.save()
                showClearDialog = false
            },
            onDismiss = { showClearDialog = false }
        )
    }

    SpaceBackground {
        Column(Modifier.fillMaxSize()) {

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(.12f))
                            .border(1.dp, Color.White.copy(.15f), RoundedCornerShape(8.dp))
                            .clickable { SoundManager.click(); onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "QUIZ HISTORY",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        if (history.isNotEmpty()) {
                            Text(
                                "${history.size} attempt${if (history.size != 1) "s" else ""}",
                                fontSize = 10.sp, color = TextMuted
                            )
                        }
                    }
                }

                if (history.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DangerRed.copy(.12f))
                            .border(1.dp, DangerRed.copy(.35f), RoundedCornerShape(8.dp))
                            .clickable { SoundManager.click(); showClearDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                null,
                                tint = DangerRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                "CLEAR", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
                                color = DangerRed, letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            // Scrollable list
            Column(
                Modifier.fillMaxWidth().weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (history.isEmpty()) {
                    // MATCHED: icon (Any), title (String), subtitle (String)
                    EmptyState(
                        icon = Icons.Default.History,
                        title = "No quiz history yet!",
                        subtitle = "Complete a quiz to see results here"
                    )
                } else {
                    history.take(visibleCount).forEachIndexed { idx, entry ->
                        HistoryEntryCard(
                            entry = entry,
                            isOpen = expanded == idx,
                            onToggle = { expanded = if (expanded == idx) null else idx },
                            onRetry = onRetry?.let { cb -> { cb(entry.quizCode) } }
                        )
                    }

                    if (visibleCount < history.size) {
                        val remaining = history.size - visibleCount
                        Box(
                            Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(.06f))
                                .border(1.dp, Color.White.copy(.12f), RoundedCornerShape(10.dp))
                                .clickable { visibleCount += 5 } // Halimbawa ng PAGE_SIZE
                                .padding(vertical = 12.dp),
                            Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Update: AutoMirrored version para iwas deprecation warning
                                Icon(
                                    Icons.AutoMirrored.Filled.List,
                                    null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "Show $remaining more",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(80.dp))
            }
        }
    }
    }
// ─────────────────────────────────────────────────────────────────────────────
//  CLEAR HISTORY DIALOG
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ClearHistoryDialog(
    count:     Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1A1A2E))
                .border(1.dp, DangerRed.copy(.4f), RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier.size(56.dp).clip(RoundedCornerShape(28.dp)).background(DangerRed.copy(.15f)),
                Alignment.Center
            ) {
                Icon(Icons.Default.Delete, null, tint = DangerRed, modifier = Modifier.size(28.dp))
            }
            Text("Clear History?", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Text(
                "This will permanently delete all $count quiz record${if (count != 1) "s" else ""}. This cannot be undone.",
                fontSize = 12.sp, color = TextSecondary, lineHeight = 18.sp, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(DangerRed)
                    .clickable { onConfirm() }
                    .padding(vertical = 13.dp),
                Alignment.Center
            ) {
                Text("YES, CLEAR ALL", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                    color = Color.White, letterSpacing = 0.5.sp)
            }
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(.06f))
                    .border(1.dp, Color.White.copy(.15f), RoundedCornerShape(10.dp))
                    .clickable { onDismiss() }
                    .padding(vertical = 13.dp),
                Alignment.Center
            ) {
                Text("CANCEL", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                    color = TextSecondary, letterSpacing = 0.5.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  HISTORY ENTRY CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HistoryEntryCard(
    entry:    QuizHistoryEntry,
    isOpen:   Boolean,
    onToggle: () -> Unit,
    onRetry:  (() -> Unit)? = null
) {
    val pct   = if (entry.total == 0) 0f else entry.score.toFloat() / entry.total
    val grade = when {
        pct >= .9f -> "S"; pct >= .8f -> "A"; pct >= .7f -> "B"
        pct >= .6f -> "C"; pct >= .5f -> "D"; else        -> "F"
    }
    val gradeColor = when (grade) {
        "S","A" -> Gold; "B","C" -> Teal; else -> DangerRed
    }

    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A2E))
            .padding(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onToggle),
            Arrangement.SpaceBetween, Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(entry.quizTitle, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = TextMuted, modifier = Modifier.size(10.dp))
                    Text(entry.date, fontSize = 10.sp, color = TextMuted)
                    if (entry.quizCode.isNotBlank()) {
                        Box(
                            Modifier.clip(RoundedCornerShape(3.dp))
                                .background(Teal.copy(.12f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(entry.quizCode, fontSize = 9.sp, color = Teal, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(grade, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = gradeColor)
                    Text("${entry.score}/${entry.total}", fontSize = 11.sp, color = TextSecondary)
                }
                Icon(
                    if (isOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = TextMuted, modifier = Modifier.size(18.dp)
                )
            }
        }

        AnimatedVisibility(visible = isOpen) {
            Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HorizontalDivider(color = Color(0xFF2A2A3E))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.List, null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "QUESTION REVIEW", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold,
                        color = TextSecondary, letterSpacing = 1.sp
                    )
                }
                entry.answers.forEachIndexed { i, record ->
                    QuestionReviewRow(i + 1, record)
                }
                if (onRetry != null && entry.quizCode.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(Gold).clickable { onRetry() }
                            .padding(vertical = 10.dp),
                        Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("RETRY THIS QUIZ", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  QUESTION REVIEW ROW
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuestionReviewRow(num: Int, record: AnswerRecord) {
    val bg     = if (record.wasRight) Color(0xFF0A1A0A) else Color(0xFF1A0A0A)
    val accent = if (record.wasRight) SuccessGreen else DangerRed

    // ── Resolve correct answer display ─────────────────────────────────────
    // identAnswer is always populated for all question types from PlayQuizScreen.
    // Fallback for True/False old entries only.
    val correctAnswerDisplay = when {
        record.identAnswer.isNotBlank() -> record.identAnswer
        record.type == QuizType.TRUE_FALSE -> if (record.correct == 0) "True" else "False"
        else -> "—"
    }

    // ── Resolve user's answer display ──────────────────────────────────────
    // chosenText is always populated from PlayQuizScreen for all types.
    // Fallback for True/False old entries only.
    val userAnswerDisplay = when {
        record.chosenText.isNotBlank() -> record.chosenText
        record.chosen == -1            -> "No answer / Time up"
        record.type == QuizType.TRUE_FALSE ->
            if (record.chosen == 0) "True" else "False"
        else -> "—"
    }

    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, accent.copy(.25f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Header
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Q$num", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = TextMuted)
            Box(
                Modifier.clip(RoundedCornerShape(4.dp))
                    .background(accent)
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (record.wasRight) Icons.Default.Check else Icons.Default.Close,
                        null, tint = Color.White, modifier = Modifier.size(10.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        if (record.wasRight) "CORRECT" else "WRONG",
                        fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Question text
        Text(record.question, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary, lineHeight = 19.sp)
        Spacer(Modifier.height(2.dp))

        // User's answer
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("You answered:", fontSize = 10.sp, color = TextSecondary)
            Box(
                Modifier.clip(RoundedCornerShape(4.dp))
                    .background(accent.copy(.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(userAnswerDisplay, fontSize = 11.sp, color = accent, fontWeight = FontWeight.Bold)
            }
        }

        // Correct answer — always visible, always resolved
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Correct answer:", fontSize = 10.sp, color = TextSecondary)
            Box(
                Modifier.clip(RoundedCornerShape(4.dp))
                    .background(SuccessGreen.copy(.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(correctAnswerDisplay, fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
            }
        }

        // Warning for wrong answers
        if (!record.wasRight) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, null, tint = DangerRed.copy(.8f), modifier = Modifier.size(11.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    "Your answer was incorrect — review this topic",
                    fontSize = 10.sp, color = DangerRed.copy(.8f), fontWeight = FontWeight.Bold
                )
            }
        }
    }
}