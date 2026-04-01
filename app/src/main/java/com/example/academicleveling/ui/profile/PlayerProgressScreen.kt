package com.example.academicleveling.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academicleveling.R
import com.example.academicleveling.data.AppState
import com.example.academicleveling.data.EquipSlot
import com.example.academicleveling.data.Item
import com.example.academicleveling.data.ItemRarity
import com.example.academicleveling.data.rarityColor
import com.example.academicleveling.data.rarityLabel
import com.example.academicleveling.ui.shared.SpaceBackground
import com.example.academicleveling.ui.shared.SubPageBar
import com.example.academicleveling.ui.shared.TealButton
import com.example.academicleveling.ui.theme.BgCard
import com.example.academicleveling.ui.theme.BgPrimary
import com.example.academicleveling.ui.theme.Gold
import com.example.academicleveling.ui.theme.SuccessGreen
import com.example.academicleveling.ui.theme.Teal
import com.example.academicleveling.ui.theme.TextMuted
import com.example.academicleveling.ui.theme.TextPrimary
import com.example.academicleveling.ui.theme.TextSecondary
import com.example.academicleveling.ui.theme.rankColor

@Composable
fun PlayerProgressScreen(onBack: () -> Unit) {
    val xpPct = (AppState.xp.toFloat() / AppState.maxXP.coerceAtLeast(1)).coerceIn(0f, 1f)
    val weapons = AppState.inventory.filter { it.slot == EquipSlot.WEAPON }
    val equippedWeapon = AppState.equipment.weapon

    SpaceBackground {
        Column(Modifier.background(BgPrimary)) {
            SubPageBar("PLAYER PROGRESS", onBack)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgCard, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("CURRENT LEVEL", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.ExtraBold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Level ${AppState.level}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                        Text("${AppState.xp}/${AppState.maxXP} XP", color = TextMuted, fontSize = 12.sp)
                    }
                    LinearProgressIndicator(
                        progress = { xpPct },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = Teal,
                        trackColor = Color(0xFF2A2A3E)
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MiniStat("Rank", AppState.rank.name, rankColor(AppState.rank))
                        MiniStat("Total XP", "${AppState.totalXP}", Teal)
                        MiniStat("Coins", "${AppState.coins}", Gold)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.sword),
                        contentDescription = "Sword",
                        modifier = Modifier.size(150.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgCard, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("EQUIPMENT", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.ExtraBold)
                    weapons.take(4).forEach { item ->
                        val requiredLevel = requiredLevelFor(item)
                        val unlocked = AppState.level >= requiredLevel
                        EquipmentWeaponRow(
                            item = item,
                            unlocked = unlocked,
                            equipped = equippedWeapon?.id == item.id,
                            requiredLevel = requiredLevel,
                            onEquip = {
                                if (unlocked) AppState.equip(item)
                            },
                            onUnequip = { AppState.unequip(EquipSlot.WEAPON) }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgCard, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("ACHIEVEMENTS", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.ExtraBold)
                    val unlockedCount = AppState.achievements.count { it.unlocked }
                    val claimedCount = AppState.achievements.count { it.claimed }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MiniStat("Unlocked", "$unlockedCount", SuccessGreen)
                        MiniStat("Claimed", "$claimedCount", Gold)
                        MiniStat("Total", "${AppState.achievements.size}", Teal)
                    }
                }

                Spacer(Modifier.height(70.dp))
            }
        }
    }
}

@Composable
private fun EquipmentWeaponRow(
    item: Item,
    unlocked: Boolean,
    requiredLevel: Int,
    equipped: Boolean,
    onEquip: () -> Unit,
    onUnequip: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D0D1A), RoundedCornerShape(10.dp))
            .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.SportsEsports, contentDescription = null, tint = if (unlocked) Teal else TextMuted, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = "WEAPON: [${item.name.uppercase()}]",
                color = if (unlocked) TextPrimary else TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (unlocked) {
            if (equipped) {
                TealButton("CHANGE", onUnequip, modifier = Modifier.width(84.dp), color = Teal, textColor = Color.Black)
            } else {
                TealButton("EQUIP", onEquip, modifier = Modifier.width(84.dp))
            }
        } else {
            Box(
                modifier = Modifier
                    .background(Color.White.copy(0.08f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text("L${requiredLevel}", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Paid, contentDescription = null, tint = color, modifier = Modifier.size(10.dp))
            Spacer(Modifier.width(3.dp))
            Text(value, fontSize = 12.sp, color = color, fontWeight = FontWeight.ExtraBold)
        }
        Text(label, fontSize = 9.sp, color = TextMuted)
    }
}

private fun requiredLevelFor(item: Item): Int = when (item.rarity) {
    ItemRarity.COMMON -> 1
    ItemRarity.RARE -> 5
    ItemRarity.EPIC -> 10
    ItemRarity.LEGENDARY -> 15
}
