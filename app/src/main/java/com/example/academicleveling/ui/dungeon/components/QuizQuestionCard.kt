package com.example.academicleveling.ui.dungeon.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academicleveling.data.QuizQuestion
import com.example.academicleveling.data.QuizType
import com.example.academicleveling.ui.shared.SoundManager
import com.example.academicleveling.ui.theme.*

@Composable
fun QuizQuestionCard(
    q:                 QuizQuestion,
    qIndex:            Int,
    submitted:         Boolean,
    selectedOption:    Int?,
    identInput:        String,
    eliminatedOptions: Set<Int> = emptySet(),
    onOptionSelect:    (Int) -> Unit,
    onIdentChange:     (String) -> Unit
) {
    // Community quizzes store the answer in q.exp (4th constructor param).
    // User-created quizzes store it in q.identAnswer. Check both.
    val identCorrectAnswer = q.identAnswer.ifBlank { q.exp }.ifBlank { q.opts.getOrElse(q.correct) { "" } }

    // ── Question card ──────────────────────────────────────────────────────
    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF252540))
            .border(1.dp, Color.White.copy(.18f), RoundedCornerShape(14.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.clip(RoundedCornerShape(6.dp)).background(Teal.copy(.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Q${qIndex + 1}", fontSize = 11.sp, color = Teal, fontWeight = FontWeight.ExtraBold)
                }
            }
            Text(q.q, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, lineHeight = 26.sp)
        }
    }

    // ── Multiple Choice / True-False ───────────────────────────────────────
    if (q.type == QuizType.MULTIPLE_CHOICE || q.type == QuizType.TRUE_FALSE) {
        val opts = if (q.type == QuizType.TRUE_FALSE) listOf("True", "False") else q.opts
        opts.forEachIndexed { i, opt ->
            val isEliminated = i in eliminatedOptions && !submitted
            val isSelected   = selectedOption == i
            val isCorrect    = submitted && i == q.correct
            val isWrong      = submitted && isSelected && i != q.correct

            if (isEliminated) {
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2E2E50 ))
                        .border(1.5.dp, Color.White.copy(.08f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                        .alpha(0.3f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(30.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF2A2A3E)),
                        Alignment.Center
                    ) {
                        Text("✕", fontSize = 13.sp, color = TextMuted, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(opt, fontSize = 14.sp, color = TextMuted, textDecoration = TextDecoration.LineThrough,
                        modifier = Modifier.weight(1f))
                }
            } else {
                val bgColor = when {
                    isCorrect                -> Color(0xFF0D2B0D)
                    isWrong                  -> Color(0xFF2B0D0D)
                    isSelected && !submitted -> Color(0xFF0D1F2B)
                    else                     -> Color(0xFF1A1A2E)
                }
                val borderColor = when {
                    isCorrect                -> SuccessGreen
                    isWrong                  -> DangerRed
                    isSelected && !submitted -> Teal
                    else                     -> Color.White.copy(.12f)
                }
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable(enabled = !submitted) { SoundManager.click(); onOptionSelect(i) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(30.dp).clip(RoundedCornerShape(7.dp))
                            .background(
                                when {
                                    isCorrect                -> SuccessGreen
                                    isWrong                  -> DangerRed
                                    isSelected && !submitted -> Teal
                                    else                     -> Color(0xFF2A2A4A)
                                }
                            ),
                        Alignment.Center
                    ) {
                        Text(
                            when { isCorrect -> "✓"; isWrong -> "✗"; else -> "${('A' + i)}" },
                            fontSize   = 13.sp,
                            color      = if (isSelected || isCorrect || isWrong) Color.White else Color.White.copy(.6f),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        opt,
                        fontSize   = 14.sp,
                        color      = when { isCorrect -> SuccessGreen; isWrong -> DangerRed; else -> Color.White },
                        fontWeight = if (isSelected || isCorrect) FontWeight.Bold else FontWeight.Normal,
                        modifier   = Modifier.weight(1f)
                    )
                    if (isCorrect) Text("✓ Correct", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.ExtraBold)
                    if (isWrong)   Text("✗ Wrong",   fontSize = 11.sp, color = DangerRed,   fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }

    // ── Identification ─────────────────────────────────────────────────────
    if (q.type == QuizType.IDENTIFICATION) {
        OutlinedTextField(
            value         = identInput,
            onValueChange = { if (!submitted) onIdentChange(it) },
            modifier      = Modifier.fillMaxWidth(),
            enabled       = !submitted,
            label         = { Text("Your answer", fontSize = 13.sp, color = Color.White.copy(.7f)) },
            shape         = RoundedCornerShape(12.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = Teal,
                unfocusedBorderColor    = Color.White.copy(.3f),
                focusedTextColor        = Color.White,
                unfocusedTextColor      = Color.White,
                disabledTextColor       = Color.White.copy(.7f),
                disabledBorderColor     = Color.White.copy(.2f),
                focusedContainerColor   = Color(0xFF1A1A2E),
                unfocusedContainerColor = Color(0xFF1A1A2E),
                disabledContainerColor  = Color(0xFF1A1A2E),
                cursorColor             = Teal
            )
        )
        if (submitted) {
            val isRight = identInput.trim().equals(identCorrectAnswer.trim(), ignoreCase = true)
            Box(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isRight) Color(0xFF0D2B0D) else Color(0xFF2B0D0D))
                    .border(1.dp, if (isRight) SuccessGreen.copy(.4f) else DangerRed.copy(.4f), RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        if (isRight) "✓ Correct!" else "✗ Incorrect",
                        color = if (isRight) SuccessGreen else DangerRed,
                        fontWeight = FontWeight.ExtraBold, fontSize = 14.sp
                    )
                    if (!isRight) {
                        Text("Correct answer:", fontSize = 11.sp, color = Color.White.copy(.6f))
                        Text(
                            identCorrectAnswer,
                            color = SuccessGreen,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }

    // ── Explanation (MC and T/F only — for IDENTIFICATION, q.exp IS the answer) ──
    if (submitted && q.exp.isNotBlank() && q.type != QuizType.IDENTIFICATION) {
        Box(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0D1A2B))
                .border(1.dp, Blue.copy(.3f), RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("💡 EXPLANATION", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
                    color = Blue, letterSpacing = 1.sp)
                Text(q.exp, fontSize = 13.sp, color = Color.White, lineHeight = 20.sp)
            }
        }
    }
}