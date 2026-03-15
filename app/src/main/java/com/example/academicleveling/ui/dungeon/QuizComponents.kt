package com.example.academicleveling.ui.dungeon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academicleveling.data.*
import com.example.academicleveling.ui.shared.InfoChip
import com.example.academicleveling.ui.shared.SoundManager
import com.example.academicleveling.ui.theme.*

// ─────────────────────────────────────────────────────────────────────────────
//  QUIZ CARD  — shown in My Quizzes and Community lists
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun QuizCard(
    quiz:     Quiz,
    showCode: Boolean = true,
    actions:  @Composable () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = BgCard),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(quiz.title, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    if (quiz.dateCreated.isNotBlank()) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarToday, null,
                                tint     = TextMuted,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(quiz.dateCreated, fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
                DifficultyBadge(quiz.difficulty)
            }
            Spacer(Modifier.height(6.dp))

            // Chips row
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                InfoChip(quiz.subject)
                InfoChip(quiz.gradeLevel)
                InfoChip("${quiz.questions.size} Qs")
                InfoChip(quizTypeLabel(quiz.quizType), Accent.copy(.12f), Accent)
                when (quiz.timerMode) {
                    QuizTimerMode.WHOLE_QUIZ   -> InfoChip("${quiz.timerSeconds}s total", Gold.copy(.15f), Gold)
                    QuizTimerMode.PER_QUESTION -> InfoChip("${quiz.timerSeconds}s/Q",     Gold.copy(.15f), Gold)
                    QuizTimerMode.NONE         -> {}
                }
            }

            // Code chip
            if (showCode && quiz.code.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Code: ", fontSize = 10.sp, color = TextMuted)
                    Box(
                        Modifier.clip(RoundedCornerShape(4.dp))
                            .background(Teal.copy(.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(quiz.code, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Teal)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            actions()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  DIFFICULTY BADGE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DifficultyBadge(d: Difficulty) {
    val c = difficultyColor(d)
    Box(
        Modifier.clip(RoundedCornerShape(5.dp))
            .background(c.copy(.15f))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(difficultyLabel(d), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = c)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  FILTER CHIP  (used in community search bar)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FilterChip(label: String, color: Color, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(7.dp))
            .background(if (active) color else Color.White.copy(.10f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            label, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
            color = if (active) Color.White else Color.White.copy(.6f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  ANSWER ROW  — shown in history detail / result review
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AnswerRow(idx: Int, ar: AnswerRecord) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(vertical = 3.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (ar.wasRight) SuccessGreen.copy(.08f) else DangerRed.copy(.08f))
            .padding(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text       = if (ar.wasRight) "✓" else "✗",
            fontSize   = 14.sp,
            color      = if (ar.wasRight) SuccessGreen else DangerRed,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text("Q${idx + 1}: ${ar.question}", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            if (!ar.wasRight) {
                when (ar.type) {
                    QuizType.IDENTIFICATION ->
                        Text("Correct answer: ${ar.identAnswer}", fontSize = 10.sp, color = SuccessGreen)
                    else ->
                        Text("Correct: Option ${('A' + ar.correct)}", fontSize = 10.sp, color = SuccessGreen)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  POWERUP HUD ROW  — shown during a quiz at the top/bottom
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PowerupHud(
    timeWarpCount:     Int,
    secondChanceCount: Int,
    hintCount:         Int,
    secondChanceMode:  Boolean,
    onTimeWarp:        () -> Unit,
    onSecondChance:    () -> Unit,
    onHint:            () -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth().background(BgDark.copy(.95f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text("POWERUPS:", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White.copy(.5f))
        PowerupButton("⏳", timeWarpCount,    secondChanceMode, onTimeWarp)
        PowerupButton("🛡️", secondChanceCount, secondChanceMode, onSecondChance)
        PowerupButton("💡", hintCount, false, onHint)
        Spacer(Modifier.weight(1f))
        if (secondChanceMode) {
            Text("2nd attempt active", fontSize = 10.sp, color = Purple, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun PowerupButton(icon: String, count: Int, active: Boolean, onClick: () -> Unit) {
    val hasStock = count > 0
    Box(
        modifier = Modifier.clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    active   -> Purple.copy(.25f)
                    hasStock -> Color.White.copy(.10f)
                    else     -> Color.White.copy(.04f)
                }
            )
            .border(
                1.dp,
                if (active) Purple else if (hasStock) Teal.copy(.3f) else Color.White.copy(.1f),
                RoundedCornerShape(8.dp)
            )
            .clickable(enabled = hasStock) { SoundManager.click(); onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 14.sp)
            Spacer(Modifier.width(3.dp))
            Text(
                "×$count", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                color = if (hasStock) Color.White else Color.White.copy(.3f)
            )
        }
    }
}