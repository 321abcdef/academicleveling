package com.example.academicleveling.ui.dungeon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academicleveling.data.AnswerRecord
import com.example.academicleveling.data.Quiz
import com.example.academicleveling.data.QuizType
import com.example.academicleveling.ui.shared.SpaceBackground
import com.example.academicleveling.ui.shared.SubPageBar
import com.example.academicleveling.ui.theme.*

@Composable
fun QuizResultScreen(
    quiz:    Quiz,
    answers: List<AnswerRecord>,
    onBack:  () -> Unit
) {
    val score = answers.count { it.wasRight }
    val total = answers.size
    val pct   = if (total == 0) 0f else score.toFloat() / total

    val grade = when {
        pct >= .9f -> "S"; pct >= .8f -> "A"; pct >= .7f -> "B"
        pct >= .6f -> "C"; pct >= .5f -> "D"; else        -> "F"
    }
    val gradeColor = when (grade) {
        "S", "A" -> Gold; "B", "C" -> Teal; else -> DangerRed
    }
    val coinsEarned = score * 5

    SpaceBackground {
        Column(Modifier.fillMaxSize()) {
            SubPageBar("QUIZ RESULTS", onBack)

            Column(
                Modifier.fillMaxWidth().weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Score card ─────────────────────────────────────────────
                Column(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1A1A2E))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(quiz.title.uppercase(), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Teal)
                    Text(grade, fontSize = 80.sp, fontWeight = FontWeight.ExtraBold, color = gradeColor)
                    Text("$score / $total", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text(
                        "FINAL SCORE: ${(pct * 100).toInt()}%",
                        fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RewardChip("+${coinsEarned} COINS", Gold,   Icons.Default.Paid)
                        RewardChip("+${quiz.exp} XP",       Teal,   Icons.Default.Star)
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick  = onBack,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Teal)
                    ) {
                        Icon(Icons.Default.Home, null, modifier = Modifier.size(18.dp), tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text("RETURN TO MENU", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    }
                }

                // ── Review header ──────────────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                    Icon(Icons.Default.FactCheck, null, tint = Teal, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "ANSWERS REVIEW",
                        fontSize = 14.sp, fontWeight = FontWeight.ExtraBold,
                        color = Color.White, letterSpacing = 1.sp
                    )
                }

                // ── Questions list ─────────────────────────────────────────
                answers.forEachIndexed { i, record ->
                    AnswerReviewCard(index = i, record = record)
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun RewardChip(label: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        color  = color.copy(alpha = 0.15f),
        shape  = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(0.3f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 12.sp, color = color, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun AnswerReviewCard(index: Int, record: AnswerRecord) {
    val cardBg      = if (record.wasRight) Color(0xFF142914) else Color(0xFF2D1616)
    val statusColor = if (record.wasRight) SuccessGreen else DangerRed

    // ── Resolve what to display as the correct answer ──────────────────────
    // identAnswer is always populated for all question types from PlayQuizScreen.
    // Fallback for True/False old entries only (index is sufficient to reconstruct).
    val correctAnswerDisplay = when {
        record.identAnswer.isNotBlank() -> record.identAnswer
        record.type == QuizType.TRUE_FALSE -> if (record.correct == 0) "True" else "False"
        else -> "—"
    }

    // ── Resolve what to display as the user's answer ───────────────────────
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
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header row
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(
                "QUESTION ${index + 1}",
                fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                color    = Color.White.copy(0.5f)
            )
            Surface(color = statusColor, shape = RoundedCornerShape(6.dp)) {
                Text(
                    text       = if (record.wasRight) " CORRECT " else " WRONG ",
                    fontSize   = 10.sp,
                    color      = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Question text
        Text(
            text       = record.question,
            fontSize   = 15.sp,
            fontWeight = FontWeight.Bold,
            color      = Color.White,
            lineHeight = 22.sp
        )

        HorizontalDivider(color = Color.White.copy(0.1f), thickness = 1.dp)

        // Answers section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // User's answer
            Column {
                Text(
                    "YOUR ANSWER:",
                    fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = TextSecondary
                )
                AnswerBadge(text = userAnswerDisplay, color = statusColor)
            }
            // Correct answer — always visible
            Column {
                Text(
                    "CORRECT ANSWER:",
                    fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = SuccessGreen
                )
                AnswerBadge(text = correctAnswerDisplay, color = SuccessGreen)
            }
        }
    }
}

@Composable
private fun AnswerBadge(text: String, color: Color) {
    Box(
        Modifier.fillMaxWidth()
            .padding(top = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(0.12f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text       = text,
            fontSize   = 14.sp,
            color      = color,
            fontWeight = FontWeight.ExtraBold
        )
    }
}