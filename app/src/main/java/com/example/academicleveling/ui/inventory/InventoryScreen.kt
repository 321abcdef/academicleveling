package com.example.academicleveling.ui.inventory

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academicleveling.data.*
import com.example.academicleveling.data.AppState.SHOP_ITEMS
import com.example.academicleveling.ui.shared.*
import com.example.academicleveling.ui.theme.*

@Composable
fun InventoryScreen() {
    SpaceBackground {
        Column(Modifier.fillMaxSize()) {
            TopBar()
            ShopTab()
        }
    }
}

@Composable
private fun ShopTab() {
    var purchasedMsg by remember { mutableStateOf("") }
    var errorMsg     by remember { mutableStateOf("") }
    var streakMsg    by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CoinsBanner()

        if (purchasedMsg.isNotBlank()) FeedbackBanner(purchasedMsg, SuccessGreen, Icons.Default.CheckCircle)
        if (errorMsg.isNotBlank())     FeedbackBanner(errorMsg,     DangerRed,    Icons.Default.Error)
        if (streakMsg.isNotBlank())    FeedbackBanner(streakMsg,    Teal,         Icons.Default.Whatshot)

        SectionLabel("YOUR POWER-UPS")
        PowerUpStockRow(
            items = listOf(
                PowerUpInfo(shopItemIcon(ShopEffect.TIME_WARP),      shopItemColor(ShopEffect.TIME_WARP),      "Time Warp",     AppState.timeWarpCount),
                PowerUpInfo(shopItemIcon(ShopEffect.SECOND_CHANCE),  shopItemColor(ShopEffect.SECOND_CHANCE),  "50/50",         AppState.secondChanceCount),
                PowerUpInfo(shopItemIcon(ShopEffect.HINT),           shopItemColor(ShopEffect.HINT),           "Hint",          AppState.hintCount),
                PowerUpInfo(shopItemIcon(ShopEffect.STREAK_BANDAID), shopItemColor(ShopEffect.STREAK_BANDAID), "Streak Fix",    AppState.streakBandaidCount)
            ),
            onUseBandaid = {
                if (AppState.useStreakBandaid()) {
                    SoundManager.claim(); streakMsg = "Streak repaired!"
                }
            }
        )

        SectionLabel("SHOP")
        SHOP_ITEMS.forEach { item ->
            ShopItemCard(
                item      = item,
                canAfford = AppState.coins >= item.cost,
                onBuy     = {
                    if (AppState.buyShopItem(item)) {
                        SoundManager.claim()
                        purchasedMsg = "Purchased ${item.name}!"
                        errorMsg     = ""
                    } else {
                        SoundManager.error()
                        errorMsg     = "Not enough coins."
                        purchasedMsg = ""
                    }
                }
            )
        }

        Box(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF001A1A))
                .border(1.dp, Teal.copy(.2f), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Teal,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Earn coins: 1/study min  •  5/correct answer  •  20–50/quest",
                    fontSize = 11.sp, color = TextSecondary
                )
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

private data class PowerUpInfo(
    val icon:  ImageVector,
    val color: Color,
    val name:  String,
    val count: Int
)

@Composable
private fun PowerUpStockRow(
    items:        List<PowerUpInfo>,
    onUseBandaid: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A2E))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { item ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                            .background(if (item.count > 0) item.color.copy(.15f) else Color(0xFF0D0D1A))
                            .border(
                                1.dp,
                                if (item.count > 0) item.color.copy(.4f) else Color(0xFF2A2A3E),
                                RoundedCornerShape(10.dp)
                            ),
                        Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (item.count > 0) item.color else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "×${item.count}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                        color = if (item.count > 0) item.color else TextMuted
                    )
                    Text(item.name, fontSize = 8.sp, color = TextMuted)
                }
            }
        }
        if (AppState.streakBandaidCount > 0) {
            TealButton(
                label     = "USE STREAK BAND-AID",
                onClick   = onUseBandaid,
                modifier  = Modifier.fillMaxWidth(),
                color     = Color(0xFF0D0D1A),
                textColor = TextPrimary
            )
        }
    }
}

@Composable
private fun CoinsBanner() {
    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1A1A2E))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Paid,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("YOUR COINS", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                Text("${AppState.coins}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Gold)
            }
        }
    }
}

@Composable
fun ShopItemCard(item: ShopItem, canAfford: Boolean, onBuy: () -> Unit) {
    val itemIcon  = shopItemIcon(item.effect)
    val itemColor = shopItemColor(item.effect)
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A2E))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(50.dp).clip(RoundedCornerShape(10.dp))
                .background(itemColor.copy(.15f))
                .border(1.dp, itemColor.copy(.3f), RoundedCornerShape(10.dp)),
            Alignment.Center
        ) {
            Icon(
                imageVector = itemIcon,
                contentDescription = null,
                tint = itemColor,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name,        fontWeight = FontWeight.Bold,  color = TextPrimary)
            Text(item.description, fontSize   = 11.sp,            color = TextSecondary)
        }
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Paid, null, tint = Gold, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(3.dp))
                Text(
                    "${item.cost}",
                    color      = if (canAfford) Gold else DangerRed,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(4.dp))
            TealButton("BUY", onBuy, Modifier.width(64.dp), enabled = canAfford)
        }
    }
}

@Composable
private fun FeedbackBanner(msg: String, color: Color, icon: ImageVector) {
    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(.12f))
            .border(1.dp, color.copy(.3f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(msg, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}