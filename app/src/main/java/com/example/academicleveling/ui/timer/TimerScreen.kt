package com.example.academicleveling.ui.timer

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.academicleveling.data.AppState
import com.example.academicleveling.data.isoDate
import com.example.academicleveling.data.formatDate
import com.example.academicleveling.ui.shared.*
import com.example.academicleveling.ui.theme.*

@Composable
fun TimerScreen() {
    val mode          = TimerState.mode
    val totalSecs     = TimerState.totalSecs
    val remaining     = TimerState.remaining
    val running       = TimerState.running
    val sessionsToday = TimerState.sessionsToday
    val showDone      = TimerState.showDone
    val progress      = 1f - (remaining.toFloat() / totalSecs.coerceAtLeast(1)).coerceIn(0f, 1f)

    val inf = rememberInfiniteTransition(label = "ring")
    val rotation by inf.animateFloat(
        initialValue  = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label         = "rot"
    )

    SpaceBackground {
        LaunchedEffect(Unit) {
            AppState.refreshSessionHistory()
        }
        Column(Modifier.fillMaxSize()) {
            TopBar()

            Column(
                modifier            = Modifier.fillMaxWidth().weight(1f)
                    .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF001A1A))
                        .border(1.dp, Teal.copy(.25f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Teal,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Study Timer Tab",
                                fontSize = 12.sp,
                                color = Teal,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            "Quiz countdown timer appears inside the Play Quiz screen.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // MODE TABS
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1A1A2E))
                        .padding(4.dp)
                ) {
                    TimerMode.entries.forEach { m ->
                        Box(
                            modifier = Modifier.weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (mode == m) Color(0xFF0D0D1A) else Color.Transparent)
                                .clickable(enabled = !running) { SoundManager.click(); TimerState.updateMode(m) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    m.label, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
                                    color = if (mode == m) Teal else TextMuted
                                )
                                if (m.isFocus && mode == m)
                                    Text("XP + Coins", fontSize = 7.sp, color = Gold, fontWeight = FontWeight.Bold)
                                else if (!m.isFocus && mode == m)
                                    Text("Rest only", fontSize = 7.sp, color = TextMuted)
                            }
                        }
                    }
                }

                // Break notice
                if (!mode.isFocus) {
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1A1A00))
                            .border(1.dp, Gold.copy(.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Coffee,
                                contentDescription = null,
                                tint = Gold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Break time — no XP or coins. Only Pomodoro sessions give rewards.",
                                fontSize = 11.sp, color = Gold.copy(.9f), fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // TIMER CIRCLE
                Box(Modifier.size(220.dp), Alignment.Center) {
                    if (running) {
                        Box(
                            Modifier.size(215.dp).clip(CircleShape)
                                .border(2.dp, Teal.copy(.4f), CircleShape)
                                .rotate(rotation)
                        )
                    }
                    Box(
                        Modifier.size(200.dp).clip(CircleShape)
                            .background(Color(0xFF0D0D1A))
                            .border(4.dp, if (running) Teal else Color(0xFF2A2A3E), CircleShape),
                        Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "%02d:%02d".format(remaining / 60, remaining % 60),
                                fontSize = 44.sp, fontWeight = FontWeight.ExtraBold, color = Color.White
                            )
                            Text(
                                mode.label.uppercase(), fontSize = 10.sp,
                                color = if (mode.isFocus) Teal else Gold,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (running) {
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FiberManualRecord,
                                        contentDescription = null,
                                        tint = Teal,
                                        modifier = Modifier.size(8.dp)
                                    )
                                    Spacer(Modifier.width(3.dp))
                                    Text("RUNNING", fontSize = 9.sp, color = Teal.copy(.8f))
                                }
                            }
                        }
                    }
                }

                // PROGRESS
                Column(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1A1A2E))
                        .padding(12.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Progress", fontSize = 11.sp, color = TextSecondary)
                        Text("${(progress * 100).toInt()}%", fontSize = 11.sp, color = Teal, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(6.dp))
                    ProgressBar(progress, if (mode.isFocus) Teal else Gold)
                }

                // CONTROLS
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TealButton(
                        label    = if (running) "PAUSE" else "START",
                        onClick  = {
                            if (!running) SoundManager.timerStart() else SoundManager.click()
                            TimerState.toggleRunning()
                        },
                        modifier = Modifier.weight(1f).height(50.dp)
                    )
                    TealButton(
                        label    = "RESET",
                        onClick  = { SoundManager.click(); TimerState.reset() },
                        modifier = Modifier.weight(1f).height(50.dp),
                        color    = Color(0xFF1A1A2E),
                        textColor = TextPrimary
                    )
                }

                if (running) {
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF001A1A))
                            .border(1.dp, Teal.copy(.25f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Teal,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Timer keeps counting even when you switch tabs",
                                fontSize = 11.sp, color = Teal, fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // CURRENT SESSION
                if (mode.isFocus) {
                    Column(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1A1A2E))
                            .padding(14.dp)
                    ) {
                        SectionLabel("CURRENT SESSION")
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                            val durationMins = totalSecs / 60
                            val estExp = durationMins // duration / 60
                            val estCoins = totalSecs / 120 // duration / 120
                            
                            SessionTile(Icons.Default.Timer,                "$durationMins min", "duration",  Teal)
                            SessionTile(Icons.Default.Bolt,                 "+$estExp XP",      "earned",    Gold)
                            SessionTile(Icons.Default.Paid,                 "+$estCoins",       "coins",     Gold)
                        }
                    }
                }

                // SESSION HISTORY
                if (AppState.sessionHistory.isNotEmpty()) {
                    Column(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1A1A2E))
                            .padding(14.dp)
                    ) {
                        SectionLabel("HISTORY")
                        Spacer(Modifier.height(8.dp))
                        val todayStr = isoDate()
                        val todayMins = AppState.sessionHistory
                            .filter { it.date == todayStr }
                            .sumOf { it.minutes }
                        
                        // Total minutes across all logged history
                        val totalMinsHistory = AppState.sessionHistory.sumOf { it.minutes }
                        
                        Text(
                            "Today: $todayMins min  •  Total History: $totalMinsHistory min",
                            fontSize = 12.sp, color = TextPrimary
                        )
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFF2A2A3E))
                        Spacer(Modifier.height(8.dp))
                        AppState.sessionHistory.forEach { s ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                Arrangement.SpaceBetween, Alignment.CenterVertically
                            ) {
                                Text(formatDate(s.date), fontSize = 11.sp, color = TextSecondary)
                                Text("${s.minutes} min", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Box(
                                    Modifier.clip(RoundedCornerShape(4.dp))
                                        .background(Teal.copy(.15f))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text("+${s.xpGained} XP", fontSize = 10.sp, color = Teal, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                TealButton(
                    label    = "NEW SESSION",
                    onClick  = { SoundManager.click(); TimerState.reset() },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    color    = Color(0xFF1A1A2E),
                    textColor = TextPrimary
                )
                Spacer(Modifier.height(80.dp))
            }
        }
    }

    if (showDone) {
        SessionDoneDialog(mode = mode, sessions = sessionsToday) { TimerState.showDone = false }
    }
}

@Composable
private fun SessionTile(icon: ImageVector, value: String, label: String, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Text(label, fontSize = 9.sp,  color = TextMuted)
    }
}

@Composable
private fun SessionDoneDialog(mode: TimerMode, sessions: Int, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            Modifier.clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0D0D1A))
                .border(2.dp, Teal, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (mode.isFocus) Icons.Default.EmojiEvents else Icons.Default.Coffee,
                    contentDescription = null,
                    tint = if (mode.isFocus) Gold else Gold.copy(.8f),
                    modifier = Modifier.size(52.dp)
                )
                Text(
                    if (mode.isFocus) "SESSION COMPLETE!" else "BREAK OVER!",
                    fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White
                )
                if (mode.isFocus) {
                    Text("XP & coins earned!", fontSize = 12.sp, color = Teal)
                    Text("Sessions today: $sessions", fontSize = 13.sp, color = Color.White.copy(.65f))
                } else {
                    Text("Ready to focus again? Switch to Pomodoro.", fontSize = 12.sp, color = Gold.copy(.8f))
                }
                Spacer(Modifier.height(10.dp))
                TealButton("CONTINUE", onDismiss, Modifier.fillMaxWidth())
            }
        }
    }
}