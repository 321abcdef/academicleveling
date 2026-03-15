package com.example.academicleveling.ui.dungeon.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academicleveling.data.AppState
import com.example.academicleveling.ui.shared.SoundManager
import com.example.academicleveling.ui.theme.*

// ─────────────────────────────────────────────────────────────────────────────
//  QuizPowerUpBar
//
//   ⏳ Time Warp  — timed quizzes only
//   🛡️ 50/50     — MC with 4 options only; once per question; before submit
//   💡 Hint       — any type; before submit
//   🩹 Streak Band-aid — NOT shown (Home screen only)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun QuizPowerUpBar(
    hasTimer:       Boolean,
    submitted:      Boolean,
    fiftyFiftyUsed: Boolean,
    isMcQuestion:   Boolean,
    onTimeWarp:     () -> Unit,
    onFiftyFifty:   () -> Unit,
    onHint:         () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        // Ginawang SpaceBetween para pantay-pantay ang agwat
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label Section
        Column {
            Text(
                "POWER-UPS", fontSize = 10.sp, color = TextMuted,
                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp
            )
            // Optional: Dagdagan natin ng small indicator para hindi boring
            Box(Modifier.width(20.dp).height(2.dp).background(Teal))
        }

        // Power-up Buttons Container
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time Warp
            PowerUpChip(
                icon    = Icons.Default.AccessTime,
                iconTint = Blue,
                count   = AppState.timeWarpCount,
                enabled = hasTimer && AppState.timeWarpCount > 0,
                tooltip = if (!hasTimer) "No timer" else null,
                onUse   = onTimeWarp
            )

            // 50/50
            val fiftyEnabled = AppState.secondChanceCount > 0
                    && !submitted
                    && !fiftyFiftyUsed
                    && isMcQuestion
            PowerUpChip(
                icon    = Icons.Default.Security,
                iconTint = Purple,
                count   = AppState.secondChanceCount,
                enabled = fiftyEnabled,
                tooltip = when {
                    !isMcQuestion   -> "MC only"
                    fiftyFiftyUsed  -> "Used"
                    submitted       -> "Too late"
                    else            -> null
                },
                label   = "50/50",
                onUse   = onFiftyFifty
            )

            // Hint
            PowerUpChip(
                icon    = Icons.Default.Lightbulb,
                iconTint = Gold,
                count   = AppState.hintCount,
                enabled = AppState.hintCount > 0 && !submitted,
                tooltip = if (submitted && AppState.hintCount > 0) "Too late" else null,
                onUse   = onHint
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  PowerUpChip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PowerUpChip(
    icon:     ImageVector,
    iconTint: Color = Teal,
    count:    Int,
    enabled:  Boolean,
    tooltip:  String? = null,
    label:    String? = null,
    onUse:    () -> Unit
) {
    Box(
        Modifier
            .defaultMinSize(minWidth = 50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (enabled) Teal.copy(.12f) else Color.White.copy(.04f))
            .border(1.dp, if (enabled) Teal.copy(.3f) else Color.White.copy(.06f), RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { SoundManager.click(); onUse() }
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) iconTint else TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "×$count",
                    fontSize = 10.sp,
                    color = if (enabled) Teal else TextMuted,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            if (label != null) {
                Text(label, fontSize = 7.sp, color = if (enabled) Teal else TextMuted, fontWeight = FontWeight.Bold)
            }
            if (tooltip != null) {
                Text(tooltip, fontSize = 7.sp, color = TextMuted)
            }
        }
    }
}