package com.example.academicleveling.ui.bazaar

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
fun BazaarScreen() {
    var tab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().background(BgPrimary)) {
        TopBar()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgDark)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            val tabs = listOf("SHOP", "POWER-UPS")
            val icons = listOf(Icons.Default.Storefront, Icons.Default.Inventory)

            tabs.forEachIndexed { i, label ->
                BazaarTabButton(
                    label = label,
                    icon = icons[i],
                    isSelected = tab == i,
                    modifier = Modifier.weight(1f),
                    onClick = { SoundManager.click(); tab = i }
                )
                if (i < tabs.lastIndex) Spacer(Modifier.width(8.dp))
            }
        }

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when (tab) {
                0 -> ShopTab()
                1 -> PowerUpBagTab()
            }
        }
    }
}

@Composable
fun RowScope.BazaarTabButton(label: String, icon: ImageVector, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Teal.copy(alpha = 0.2f) else Color.White.copy(.05f))
            .border(1.dp, if (isSelected) Teal else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) Teal else TextMuted
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isSelected) TextPrimary else TextMuted
            )
        }
    }
}

@Composable
private fun ShopTab() {
    var buyMsg by remember { mutableStateOf("") }
    var errMsg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
    ) {
        CoinsBanner()

        if (buyMsg.isNotBlank()) FeedbackBanner(buyMsg, SuccessGreen, Icons.Default.CheckCircle)
        if (errMsg.isNotBlank()) FeedbackBanner(errMsg, DangerRed, Icons.Default.Error)

        Spacer(Modifier.height(16.dp))
        SectionLabel("ABILITIES & POWER-UPS")
        Text("Enhance your quiz performance", fontSize = 11.sp, color = TextMuted)
        Spacer(Modifier.height(12.dp))

        SHOP_ITEMS.forEach { item ->
            ShopItemCard(
                item = item,
                icon = shopItemIcon(item.effect),
                iconColor = shopItemColor(item.effect),
                stock = stockOf(item.effect),
                canAfford = AppState.coins >= item.cost,
                onBuy = {
                    if (AppState.buyShopItem(item)) {
                        SoundManager.claim(); buyMsg = "Bought ${item.name}!"; errMsg = ""
                    } else {
                        SoundManager.error(); errMsg = "Insufficient coins."; buyMsg = ""
                    }
                }
            )
            Spacer(Modifier.height(10.dp))
        }

        // Info Guide
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                .background(BgCardDark).border(1.dp, TextMuted.copy(.2f), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = Blue, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Study: 1/min • Correct: 5 • Quest: 20-50", fontSize = 11.sp, color = TextSecondary)
            }
        }
        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun PowerUpBagTab() {
    var bandaidMsg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CoinsBanner()
        SectionLabel("YOUR INVENTORY")

        if (bandaidMsg.isNotBlank()) FeedbackBanner(bandaidMsg, SuccessGreen, Icons.Default.Star)

        PowerUpRow(shopItemIcon(ShopEffect.TIME_WARP),      shopItemColor(ShopEffect.TIME_WARP),      "Time Warp",       AppState.timeWarpCount)
        PowerUpRow(shopItemIcon(ShopEffect.SECOND_CHANCE),  shopItemColor(ShopEffect.SECOND_CHANCE),  "50/50",           AppState.secondChanceCount)
        PowerUpRow(shopItemIcon(ShopEffect.HINT),           shopItemColor(ShopEffect.HINT),           "Hint",            AppState.hintCount)
        PowerUpRow(shopItemIcon(ShopEffect.STREAK_BANDAID), shopItemColor(ShopEffect.STREAK_BANDAID), "Streak Band-aid", AppState.streakBandaidCount,
            actionLabel = if (AppState.streakBandaidCount > 0) "USE" else null,
            onAction = { if (AppState.useStreakBandaid()) { SoundManager.claim(); bandaidMsg = "Streak Repaired!" } }
        )
        Spacer(Modifier.height(100.dp))
    }
}

@Composable
fun ShopItemCard(item: ShopItem, icon: ImageVector, iconColor: Color, stock: Int, canAfford: Boolean, onBuy: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape = RoundedCornerShape(12.dp),
        border = if (canAfford) null else borderStroke(1.dp, DangerRed.copy(.3f))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(BgCardDark), Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("In Bag: $stock", fontSize = 10.sp, color = TextMuted)
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Paid, null, tint = Gold, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${item.cost}", fontWeight = FontWeight.ExtraBold, color = if (canAfford) Gold else DangerRed)
                }
                Spacer(Modifier.height(6.dp))
                TealButton("BUY", onBuy, Modifier.width(64.dp), enabled = canAfford)
            }
        }
    }
}

@Composable
fun PowerUpRow(icon: ImageVector, color: Color, name: String, count: Int, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BgCard)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Owned: $count", fontSize = 11.sp, color = color.copy(alpha = 0.8f))
            }
            if (actionLabel != null) TealButton(actionLabel, onAction ?: {}, Modifier.width(64.dp))
        }
    }
}

@Composable
private fun CoinsBanner() {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(BgDark).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Gold, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Text("${AppState.coins}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Gold)
            Spacer(Modifier.width(4.dp))
            Text("COINS", fontSize = 12.sp, color = Gold.copy(alpha = .7f))
        }
    }
}

@Composable
private fun FeedbackBanner(msg: String, color: Color, icon: ImageVector) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(color.copy(.1f)).padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(msg, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) =
    androidx.compose.foundation.BorderStroke(width, color)

private fun stockOf(effect: ShopEffect) = when (effect) {
    ShopEffect.TIME_WARP      -> AppState.timeWarpCount
    ShopEffect.SECOND_CHANCE  -> AppState.secondChanceCount
    ShopEffect.HINT           -> AppState.hintCount
    ShopEffect.STREAK_BANDAID -> AppState.streakBandaidCount
    ShopEffect.XP_BOOST       -> 0
}