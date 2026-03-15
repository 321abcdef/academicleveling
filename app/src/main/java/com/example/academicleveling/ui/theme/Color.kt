package com.example.academicleveling.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.academicleveling.data.Rank

val BgPrimary  = Color(0xCC0D0D1A)   // 80% opacity dark navy
val BgCard     = Color(0xFF1A1A2E)   // 87% opacity dark purple card
val BgCardDark = Color(0xDD0E0E20)   // 87% opacity darker card
val BgDark     = Color(0xEE0A0A18)   // 93% opacity — bars/headers stay solid enough

// ── Accent ────────────────────────────────────────────────────────────────────
val Teal         = Color(0xFF00E5FF)
val Accent       = Color(0xFF7C3AED)
val Gold         = Color(0xFFFFD700)
val Purple       = Color(0xFFB026FF)
val Blue         = Color(0xFF2979FF)
val SuccessGreen = Color(0xFF00E676)
val DangerRed    = Color(0xFFFF1744)

// ── Text — brighter for readability over dark bg image ───────────────────────
val TextPrimary   = Color(0xFFFFFFFF)   // pure white for max readability
val TextSecondary = Color(0xFFCCCCDD)   // brighter secondary
val TextMuted     = Color(0xFF8888AA)   // slightly brighter muted

// ── Helper ───────────────────────────────────────────────────────────────────
fun rankColor(r: Rank) = when (r) {
    Rank.E -> Color(0xFF9E9E9E)
    Rank.D -> Color(0xFF4CAF50)
    Rank.C -> Color(0xFF2196F3)
    Rank.B -> Color(0xFF9C27B0)
    Rank.A -> Color(0xFFFF9800)
    Rank.S -> Color(0xFFFFD700)
}