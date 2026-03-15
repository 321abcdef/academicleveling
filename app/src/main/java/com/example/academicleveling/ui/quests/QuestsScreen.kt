package com.example.academicleveling.ui.quests

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academicleveling.data.AppState
import com.example.academicleveling.data.Quest
import com.example.academicleveling.ui.shared.SoundManager
import com.example.academicleveling.ui.shared.SpaceBackground
import com.example.academicleveling.ui.shared.TopBar
import com.example.academicleveling.ui.theme.*

@Composable
fun QuestsScreen() {
    var dailyClaimMsg  by remember { mutableStateOf("") }
    var weeklyClaimMsg by remember { mutableStateOf("") }

    SpaceBackground {
        Column(Modifier.fillMaxSize()) {
            TopBar()

            Column(
                modifier            = Modifier.fillMaxWidth().weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Hero card
                Row(
                    modifier              = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1A1A2E))
                        .border(1.dp, Teal.copy(.3f), RoundedCornerShape(14.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "WELCOME BACK,",
                            fontSize      = 9.sp,
                            color         = Color.White.copy(.5f),
                            letterSpacing = 1.sp,
                            fontWeight    = FontWeight.ExtraBold
                        )
                        Text(
                            AppState.name.ifBlank { "Player" },
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Color.White
                        )
                        Text(
                            "Level ${AppState.level}  •  ${AppState.rank} Rank",
                            fontSize = 11.sp,
                            color    = Teal
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            "${AppState.streak}",
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Gold
                        )
                        Text("day streak", fontSize = 9.sp, color = Color.White.copy(.6f))
                    }
                }

                // Quick stats row
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniStatCard(Modifier.weight(1f), Icons.Default.MenuBook,   "${AppState.totalMins}",        "mins studied", Purple)
                    MiniStatCard(Modifier.weight(1f), Icons.Default.Quiz,       "${AppState.quizzesCompleted}", "quizzes done", Teal)
                    MiniStatCard(Modifier.weight(1f), Icons.Default.Paid,       "${AppState.coins}",            "coins",        Gold)
                }

                // Daily Quests
                QuestSection(
                    title        = "DAILY QUESTS",
                    titleIcon    = Icons.Default.Today,
                    accentColor  = Teal,
                    quests       = AppState.quests,
                    claimMsg     = dailyClaimMsg,
                    allDone      = AppState.quests.all { it.done },
                    bonusLabel   = "CLAIM DAILY BONUS  •  +50 XP  +20 coins",
                    pendingLabel = "Complete all daily quests to claim bonus",
                    onComplete   = { id -> AppState.completeQuest(id) },
                    onClaim      = {
                        val earned = AppState.claimBonus()
                        if (earned > 0) {
                            dailyClaimMsg = "Claimed! +${earned} XP  +20 coins"
                            SoundManager.claim()
                        }
                    }
                )

                // Weekly Quests
                QuestSection(
                    title        = "WEEKLY QUESTS",
                    titleIcon    = Icons.Default.DateRange,
                    accentColor  = Purple,
                    quests       = AppState.weeklyQuests,
                    claimMsg     = weeklyClaimMsg,
                    allDone      = AppState.weeklyQuests.all { it.done },
                    bonusLabel   = "CLAIM WEEKLY BONUS  •  +100 XP  +50 coins",
                    pendingLabel = "Complete all weekly quests to claim bonus",
                    onComplete   = { id -> AppState.completeQuest(id) },
                    onClaim      = {
                        val earned = AppState.claimWeeklyBonus()
                        if (earned > 0) {
                            weeklyClaimMsg = "Claimed! +${earned} XP  +50 coins"
                            SoundManager.claim()
                        }
                    }
                )

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun QuestSection(
    title:        String,
    titleIcon:    ImageVector,
    accentColor:  Color,
    quests:       List<Quest>,
    claimMsg:     String,
    allDone:      Boolean,
    bonusLabel:   String,
    pendingLabel: String,
    onComplete:   (Int) -> Unit,
    onClaim:      () -> Unit
) {
    Column(
        modifier            = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1A1A2E))
            .border(1.dp, accentColor.copy(.25f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = titleIcon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    title,
                    fontSize      = 12.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    color         = accentColor,
                    letterSpacing = 0.5.sp
                )
            }
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "${quests.count { it.done }}/${quests.size}",
                    fontSize   = 11.sp,
                    color      = accentColor,
                    fontWeight = FontWeight.ExtraBold
                )
                Box(
                    Modifier.width(60.dp).height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF2A2A3E))
                ) {
                    Box(
                        Modifier.fillMaxHeight()
                            .fillMaxWidth(quests.count { it.done }.toFloat() / quests.size.coerceAtLeast(1))
                            .background(accentColor)
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFF2A2A3E))

        quests.forEach { q ->
            QuestRow(
                quest       = q,
                accentColor = accentColor,
                onComplete  = { if (!q.done) { SoundManager.questDone(); onComplete(q.id) } }
            )
        }

        if (claimMsg.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(claimMsg, fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (allDone) accentColor.copy(.20f) else Color(0xFF0D0D1A))
                .border(1.dp, if (allDone) accentColor else Color(0xFF2A2A3E), RoundedCornerShape(10.dp))
                .clickable(enabled = allDone) { onClaim() }
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (allDone) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    if (allDone) bonusLabel else pendingLabel,
                    fontSize   = 11.sp,
                    color      = if (allDone) accentColor else TextMuted,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun QuestRow(
    quest:       Quest,
    accentColor: Color,
    onComplete:  () -> Unit
) {
    val done = quest.done

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (done) accentColor.copy(.12f) else Color(0xFF0D0D1A))
            .border(1.dp, if (done) accentColor.copy(.3f) else Color(0xFF2A2A3E), RoundedCornerShape(10.dp))
            .clickable(enabled = !done) { onComplete() }
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(if (done) accentColor else Color(0xFF2A2A3E))
                .border(1.5.dp, accentColor.copy(.5f), RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (done) Icons.Default.Check else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (done) Color.White else accentColor.copy(.4f),
                modifier = Modifier.size(14.dp)
            )
        }

        Column(Modifier.weight(1f)) {
            Text(
                text       = quest.title,
                fontSize   = 12.sp,
                color      = if (done) TextMuted else TextPrimary,
                fontWeight = if (done) FontWeight.Normal else FontWeight.SemiBold
            )
            if (!done) {
                Text("Tap to mark as done", fontSize = 10.sp, color = accentColor.copy(.7f))
            }
        }

        Box(
            Modifier.clip(RoundedCornerShape(5.dp))
                .background(accentColor.copy(.15f))
                .padding(horizontal = 7.dp, vertical = 4.dp)
        ) {
            Text(
                "+${quest.exp} XP",
                fontSize   = 9.sp,
                color      = if (done) TextMuted else accentColor,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun MiniStatCard(
    modifier: Modifier,
    icon:     ImageVector,
    value:    String,
    label:    String,
    color:    Color
) {
    Column(
        modifier            = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A2E))
            .border(1.dp, color.copy(.25f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, fontSize = 9.sp,  color = TextMuted)
    }
}