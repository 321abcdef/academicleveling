package com.example.academicleveling.ui.profile

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgCard, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("EQUIPMENT", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.ExtraBold)
                    EquipmentRow("Weapon", equippedWeapon?.name ?: "None Equipped", Icons.Default.WorkspacePremium)
                    EquipmentRow("Armor", AppState.equipment.armor?.name ?: "None Equipped", Icons.Default.Shield)
                    EquipmentRow("Accessory 1", AppState.equipment.acc1?.name ?: "None Equipped", Icons.Default.Star)
                    EquipmentRow("Accessory 2", AppState.equipment.acc2?.name ?: "None Equipped", Icons.Default.Star)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgCard, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("WEAPONS", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.ExtraBold)
                    weapons.forEach { item ->
                        val requiredLevel = requiredLevelFor(item)
                        val unlocked = AppState.level >= requiredLevel
                        WeaponRow(
                            item = item,
                            unlocked = unlocked,
                            requiredLevel = requiredLevel,
                            equipped = equippedWeapon?.id == item.id,
                            onEquip = {
                                if (unlocked) {
                                    AppState.equip(item)
                                }
                            },
                            onUnequip = {
                                AppState.unequip(EquipSlot.WEAPON)
                            }
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
private fun WeaponRow(
    item: Item,
    unlocked: Boolean,
    requiredLevel: Int,
    equipped: Boolean,
    onEquip: () -> Unit,
    onUnequip: () -> Unit
) {
    val chipColor = rarityColor(item.rarity)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D0D1A), RoundedCornerShape(10.dp))
            .border(1.dp, if (unlocked) chipColor.copy(0.35f) else Color.White.copy(0.12f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SportsEsports, contentDescription = null, tint = if (unlocked) chipColor else TextMuted, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(item.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }
            Box(
                modifier = Modifier
                    .background(chipColor.copy(0.15f), RoundedCornerShape(5.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(rarityLabel(item.rarity), color = chipColor, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        if (unlocked) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(item.description, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                if (equipped) {
                    TealButton("EQUIPPED", onUnequip, modifier = Modifier.width(92.dp), color = SuccessGreen, textColor = Color.Black)
                } else {
                    TealButton("EQUIP", onEquip, modifier = Modifier.width(92.dp))
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("Locked until Level $requiredLevel", color = TextMuted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun EquipmentRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D0D1A), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Teal, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, color = TextSecondary, fontSize = 11.sp)
        }
        Text(value, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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