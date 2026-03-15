package com.example.academicleveling.ui.dungeon.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academicleveling.data.QuizTimerMode
import com.example.academicleveling.ui.theme.*

@Composable
fun QuizTopBar(
    title:          String,
    qIndex:         Int,
    totalQuestions: Int,
    hasTimer:       Boolean,
    timerMode:      QuizTimerMode,
    wholeQuizSecs:  Int,
    perQSecs:       Int,
    correctStreak:  Int,
    onBack:         () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(BgDark)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
            // Back button
            Box(
                Modifier.size(34.dp).clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(.08f))
                    .border(1.dp, Color.White.copy(.12f), RoundedCornerShape(8.dp))
                    .clickable { onBack() },
                Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }

            // Title + counter
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold,
                    color = Color.White, maxLines = 1)
                Text("${qIndex + 1} / $totalQuestions", fontSize = 10.sp, color = Color.White.copy(.5f))
            }

            // Timer or Streak
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (hasTimer) {
                    val displaySecs = when (timerMode) {
                        QuizTimerMode.WHOLE_QUIZ   -> wholeQuizSecs
                        QuizTimerMode.PER_QUESTION -> perQSecs
                        else                       -> 0
                    }
                    val timerColor = if (displaySecs <= 10) DangerRed else Teal
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, null, tint = timerColor, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("%02d:%02d".format(displaySecs / 60, displaySecs % 60),
                            fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = timerColor)
                    }
                    Text("timer", fontSize = 9.sp, color = Color.White.copy(.4f))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Whatshot, null,
                            tint = if (correctStreak >= 3) Gold else Color.White.copy(.7f),
                            modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("$correctStreak", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                            color = if (correctStreak >= 3) Gold else Color.White.copy(.7f))
                    }
                    Text("streak", fontSize = 9.sp, color = Color.White.copy(.4f))
                }
            }
        }
    }
}