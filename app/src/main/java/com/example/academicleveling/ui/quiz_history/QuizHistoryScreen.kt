package com.example.academicleveling.ui.quiz_history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academicleveling.data.AppState
import com.example.academicleveling.data.ApiRepository
import com.example.academicleveling.data.AttemptData
import com.example.academicleveling.data.AttemptAnswerData
import com.example.academicleveling.data.QuizType
import com.example.academicleveling.ui.shared.*
import com.example.academicleveling.ui.theme.*

@Composable
fun QuizHistoryScreen(
    onBack:  () -> Unit,
    onRetry: ((String) -> Unit)? = null
) {
    var isLoading   by remember { mutableStateOf(false) }
    var error       by remember { mutableStateOf<String?>(null) }
    var expanded    by remember { mutableStateOf<Int?>(null) }
    val listState    = rememberLazyListState()

    val history = AppState.attemptsHistory

    LaunchedEffect(Unit) {
        isLoading = true
        AppState.refreshAttempts {
            isLoading = false
        }
    }

    // Infinite Scroll Logic
    val canLoadMore = AppState.canLoadMoreAttempts
    val isMoreLoading = AppState.isAttemptsLoading && history.isNotEmpty()

    LaunchedEffect(listState, canLoadMore) {
        snapshotFlow {
            val lastItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            (lastItem?.index ?: 0) to AppState.isAttemptsLoading
        }.collect { (lastIndex, isApiLoading) ->
            val totalItems = listState.layoutInfo.totalItemsCount
            if (lastIndex >= totalItems - 5 && canLoadMore && !isApiLoading) {
                AppState.loadMoreAttempts()
            }
        }
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
            }

            // Scrollable list
            if (isLoading && history.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Gold)
                }
            } else if (error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Default.Error,
                        title = "Failed to load history",
                        subtitle = error ?: "Unknown error"
                    )
                }
            } else if (history.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Default.History,
                        title = "No quiz history yet!",
                        subtitle = "Complete a quiz to see results here"
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(
                        items = history,
                        key = { _, entry -> entry.id }
                    ) { idx, entry ->
                        HistoryEntryCard(
                            entry = entry,
                            isOpen = expanded == idx,
                            onToggle = { expanded = if (expanded == idx) null else idx },
                            onRetry = onRetry?.let { cb -> { cb(entry.quiz?.quizCode ?: "") } }
                        )
                    }

                    if (isMoreLoading) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                                CircularProgressIndicator(color = Gold, modifier = Modifier.size(32.dp))
                            }
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  HISTORY ENTRY CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HistoryEntryCard(
    entry:    AttemptData,
    isOpen:   Boolean,
    onToggle: () -> Unit,
    onRetry:  (() -> Unit)? = null
) {
    val quiz = entry.quiz ?: return // Safety check, though filtered list prevents this
    val total = quiz.questionsCount
    val pct   = if (total == 0) 0f else entry.score.toFloat() / total
    val grade = when {
        pct >= .9f -> "S"; pct >= .8f -> "A"; pct >= .7f -> "B"
        pct >= .6f -> "C"; pct >= .5f -> "D"; else        -> "F"
    }
    val gradeColor = when (grade) {
        "S","A" -> Gold; "B","C" -> Teal; else -> DangerRed
    }

    var details by remember { mutableStateOf<AttemptData?>(null) }
    var isLoadingDetails by remember { mutableStateOf(false) }

    LaunchedEffect(isOpen) {
        if (isOpen && details == null) {
            isLoadingDetails = true
            ApiRepository.getAttemptDetails(
                attemptId = entry.id,
                onSuccess = { resp ->
                    details = resp.data
                    isLoadingDetails = false
                },
                onError = {
                    isLoadingDetails = false
                }
            )
        }
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
                Text(quiz.title, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = TextMuted, modifier = Modifier.size(10.dp))
                    Text(entry.completedAt ?: entry.startedAt, fontSize = 10.sp, color = TextMuted)
                    if (quiz.quizCode.isNotBlank()) {
                        Box(
                            Modifier.clip(RoundedCornerShape(3.dp))
                                .background(Teal.copy(.12f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(quiz.quizCode, fontSize = 9.sp, color = Teal, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(grade, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = gradeColor)
                    Text("${entry.score}/$total", fontSize = 11.sp, color = TextSecondary)
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

                if (isLoadingDetails) {
                    Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Gold, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                } else {
                    details?.answers?.forEachIndexed { i, record ->
                        QuestionReviewRow(i + 1, record)
                    }
                }

                if (onRetry != null && quiz.quizCode.isNotBlank()) {
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
private fun QuestionReviewRow(num: Int, record: AttemptAnswerData) {
    val bg     = if (record.isCorrect) Color(0xFF0A1A0A) else Color(0xFF1A0A0A)
    val accent = if (record.isCorrect) SuccessGreen else DangerRed

    val correctAnswerDisplay = record.correctAnswer
    val userAnswerDisplay = record.answerText ?: "No answer / Time up"

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
                        if (record.isCorrect) Icons.Default.Check else Icons.Default.Close,
                        null, tint = Color.White, modifier = Modifier.size(10.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        if (record.isCorrect) "CORRECT" else "WRONG",
                        fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Question text
        Text(record.questionText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary, lineHeight = 19.sp)
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
        if (!record.isCorrect) {
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
