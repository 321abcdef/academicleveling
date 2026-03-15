package com.example.academicleveling.ui.shared

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.academicleveling.data.*
import com.example.academicleveling.ui.theme.*

// ─────────────────────────────────────────────────────────────────────────────
//  SHOP ITEM ICONS
//  Maps ShopEffect enum → (icon, tint color)
// ─────────────────────────────────────────────────────────────────────────────

fun shopItemIcon(effect: ShopEffect): ImageVector = when (effect) {
    ShopEffect.TIME_WARP      -> Icons.Default.HourglassEmpty  // ⌛ core
    ShopEffect.SECOND_CHANCE  -> Icons.Default.FilterNone      // two overlapping squares
    ShopEffect.HINT           -> Icons.Default.Lightbulb        // 💡 core
    ShopEffect.STREAK_BANDAID -> Icons.Default.Favorite         // ❤️ core
    ShopEffect.XP_BOOST       -> Icons.Default.FlashOn          // ⚡ core
}

fun shopItemColor(effect: ShopEffect): Color = when (effect) {
    ShopEffect.TIME_WARP      -> Blue
    ShopEffect.SECOND_CHANCE  -> Purple
    ShopEffect.HINT           -> Gold
    ShopEffect.STREAK_BANDAID -> SuccessGreen
    ShopEffect.XP_BOOST       -> Accent
}

// ─────────────────────────────────────────────────────────────────────────────
//  ACHIEVEMENT ICONS
//  Maps achievement ID → (icon, tint color)
// ─────────────────────────────────────────────────────────────────────────────

data class AchievementIcon(val icon: ImageVector, val tint: Color)

fun achievementIcon(id: Int): AchievementIcon = when (id) {
    1  -> AchievementIcon(Icons.Default.MenuBook,         Gold)          // First Steps
    2  -> AchievementIcon(Icons.Default.Quiz,             Teal)          // Quiz Taker
    3  -> AchievementIcon(Icons.Default.MilitaryTech,     Accent)        // Quest Master
    4  -> AchievementIcon(Icons.Default.Edit,             Blue)          // Quiz Creator
    5  -> AchievementIcon(Icons.Default.Star,             Gold)          // Level 5
    6  -> AchievementIcon(Icons.Default.AutoAwesome,      Gold)          // Level 10
    7  -> AchievementIcon(Icons.Default.FlashOn,          Purple)        // 1000 XP
    8  -> AchievementIcon(Icons.Default.Timer,            Teal)          // Study Hour
    9  -> AchievementIcon(Icons.Default.EmojiEvents,      Gold)          // Quiz Veteran
    10 -> AchievementIcon(Icons.Default.Speed,            DangerRed)     // Speed Demon
    11 -> AchievementIcon(Icons.Default.Public,           SuccessGreen)  // Popular Creator
    else -> AchievementIcon(Icons.Default.WorkspacePremium, Gold)
}

// ─────────────────────────────────────────────────────────────────────────────
//  BULLETIN ICONS
//  For subjects/categories in bulletin posts
// ─────────────────────────────────────────────────────────────────────────────

fun subjectIcon(subject: String): Pair<ImageVector, Color> = when {
    subject.contains("Math",    true) -> Icons.Default.Calculate        to Teal
    subject.contains("Science", true) -> Icons.Default.Science          to SuccessGreen
    subject.contains("English", true) -> Icons.Default.MenuBook         to Blue
    subject.contains("History", true) -> Icons.Default.AccountBalance   to Gold
    subject.contains("Program", true) -> Icons.Default.Code             to Accent
    subject.contains("Music",   true) -> Icons.Default.MusicNote        to Purple
    subject.contains("P.E.",    true) -> Icons.Default.SportsBasketball to DangerRed
    subject.contains("I.T.",    true) -> Icons.Default.Computer         to Teal
    subject.contains("H.E.",    true) -> Icons.Default.Restaurant       to Gold
    subject.contains("Lit",     true) -> Icons.Default.AutoStories      to Accent
    subject.contains("Geo",     true) -> Icons.Default.Public           to SuccessGreen
    else                              -> Icons.Default.School            to TextSecondary
}

// ─────────────────────────────────────────────────────────────────────────────
//  RANK ICONS
// ─────────────────────────────────────────────────────────────────────────────

fun rankIcon(rank: Rank): Pair<ImageVector, Color> = when (rank) {
    Rank.E -> Icons.Default.RadioButtonUnchecked to Color(0xFF9E9E9E)
    Rank.D -> Icons.Default.Security             to Color(0xFF4CAF50)
    Rank.C -> Icons.Default.GppGood         to Color(0xFF2196F3)
    Rank.B -> Icons.Default.VerifiedUser    to Color(0xFF9C27B0)
    Rank.A -> Icons.Default.MilitaryTech    to Color(0xFFFF9800)
    Rank.S -> Icons.Default.WorkspacePremium to Color(0xFFFFD700)
}