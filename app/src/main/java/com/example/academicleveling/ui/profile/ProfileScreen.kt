package com.example.academicleveling.ui.profile

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academicleveling.data.*
import com.example.academicleveling.ui.shared.*
import com.example.academicleveling.ui.theme.*

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        ProfileSettingsScreen(onBack = { showSettings = false })
        return
    }

    SpaceBackground {
        Column(Modifier.fillMaxSize()) {
            TopBar()

            Column(
                Modifier.fillMaxWidth().weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // --- HERO BANNER (BACKGROUND REMOVED) ---
                Box(
                    Modifier.fillMaxWidth()
                        .padding(top = 30.dp, bottom = 20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Profile Icon Box
                        Box(
                            Modifier.size(80.dp).clip(RoundedCornerShape(20.dp))
                                .background(Accent.copy(.25f))
                                .border(2.dp, Accent.copy(.4f), RoundedCornerShape(20.dp)),
                            Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Accent,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            AppState.name.ifBlank { "Player" },
                            fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White
                        )
                        Text(
                            AppState.email.ifBlank { "No email set" },
                            fontSize = 12.sp, color = Color.White.copy(.5f)
                        )

                        Spacer(Modifier.height(12.dp))

                        Box(
                            Modifier.clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(.3f))
                                .border(1.dp, rankColor(AppState.rank).copy(0.6f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "RANK ${AppState.rank}  •  Level ${AppState.level}",
                                fontSize = 11.sp, color = rankColor(AppState.rank),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Column(
                    Modifier.padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // PROGRESSION
                    SectionCard("PROGRESSION") {
                        val pct = (AppState.xp.toFloat() / AppState.maxXP.coerceAtLeast(1)).coerceIn(0f, 1f)
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text(
                                "Level ${AppState.level}", fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold, color = TextPrimary
                            )
                            Text("${AppState.xp} / ${AppState.maxXP} XP", fontSize = 11.sp, color = TextMuted)
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress   = { pct },
                            modifier   = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color      = Teal,
                            trackColor = Color(0xFF2A2A3E)
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                            StatPill("Total XP",   "${AppState.totalXP}",          Teal)
                            StatPill("Streak",     "${AppState.streak}",            Gold)
                            StatPill("Study Mins", "${AppState.totalMins}",         Blue)
                            StatPill("Quizzes",    "${AppState.quizzesCompleted}",  Accent)
                        }
                    }

                    // ACHIEVEMENTS
                    SectionCard("ACHIEVEMENTS") {
                        AppState.achievements.forEach { a ->
                            AchievementRow(a) {
                                val earned = AppState.claimAchievement(a.id)
                                if (earned > 0) SoundManager.claim()
                            }
                        }
                    }

                    // ACCOUNT
                    SectionCard("ACCOUNT") {
                        Box(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp))
                                .background(Color(0xFF0D0D1A))
                                .clickable { SoundManager.navigate(); showSettings = true }
                                .padding(14.dp)
                        ) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Settings, null, tint = Teal, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            "Account Settings", fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold, color = TextPrimary
                                        )
                                        Text(
                                            "Change username, email, or password",
                                            fontSize = 10.sp, color = TextMuted
                                        )
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Box(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp))
                                .background(DangerRed.copy(.10f))
                                .border(1.dp, DangerRed.copy(.25f), RoundedCornerShape(9.dp))
                                .clickable { SoundManager.click(); onLogout() }
                                .padding(14.dp),
                            Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Logout, null, tint = DangerRed, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("LOGOUT", fontSize = 13.sp, color = DangerRed, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun AchievementRow(a: Achievement, onClaim: () -> Unit) {
    var claimMsg by remember(a.id, a.claimed) { mutableStateOf("") }

    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                .background(if (a.unlocked) Gold.copy(.15f) else Color.White.copy(.05f)),
            Alignment.Center
        ) {
            Icon(
                imageVector = if (a.unlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                contentDescription = null,
                tint = if (a.unlocked) Gold else TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                a.title, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold,
                color = if (a.unlocked) TextPrimary else TextMuted
            )
            Text(a.description, fontSize = 10.sp, color = TextMuted)
            if (claimMsg.isNotBlank())
                Text(claimMsg, fontSize = 10.sp, color = Gold, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(6.dp))
        when {
            !a.unlocked -> Box(
                Modifier.clip(RoundedCornerShape(4.dp)).background(Color.White.copy(.05f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) { Text("LOCKED", fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.ExtraBold) }

            a.unlocked && !a.claimed -> Box(
                Modifier.clip(RoundedCornerShape(6.dp)).background(Gold.copy(.15f))
                    .border(1.dp, Gold.copy(.4f), RoundedCornerShape(6.dp))
                    .clickable { onClaim(); claimMsg = "+${a.coinReward} coins!" }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Paid, null, tint = Gold, modifier = Modifier.size(10.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("CLAIM", fontSize = 9.sp, color = Gold, fontWeight = FontWeight.ExtraBold)
                }
            }

            else -> Box(
                Modifier.clip(RoundedCornerShape(4.dp)).background(SuccessGreen.copy(.1f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(10.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("CLAIMED", fontSize = 8.sp, color = SuccessGreen, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A2E)) // Solid background for cards
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            title, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
            color = TextSecondary, letterSpacing = 1.sp
        )
        content()
    }
}

@Composable
private fun StatPill(label: String, value: String, color: Color = TextPrimary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, fontSize = 9.sp,  color = TextMuted)
    }
}