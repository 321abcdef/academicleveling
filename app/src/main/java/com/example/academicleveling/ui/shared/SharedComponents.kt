package com.example.academicleveling.ui.shared

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.academicleveling.R
import com.example.academicleveling.data.AppState
import com.example.academicleveling.ui.theme.*
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.text.style.TextAlign
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
//  SOUND MANAGER
// ─────────────────────────────────────────────────────────────────────────────

object SoundManager {
    var enabled = true

    private fun tone(freq: Float, durationMs: Int, volume: Float = 0.4f) {
        if (!enabled) return
        Thread {
            try {
                val sr   = 44100
                val n    = (sr * durationMs / 1000).coerceAtLeast(1)
                val buf  = ShortArray(n)
                val fade = (sr * 0.02).toInt().coerceAtMost(n / 4).coerceAtLeast(1)
                for (i in 0 until n) {
                    val env = when {
                        i < fade     -> i.toFloat() / fade
                        i > n - fade -> (n - i).toFloat() / fade
                        else         -> 1f
                    }
                    buf[i] = (sin(2.0 * Math.PI * freq * i / sr) * 32767 * volume * env)
                        .toInt().toShort()
                }
                val audioAttr = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
                val audioFmt = AudioFormat.Builder()
                    .setSampleRate(sr)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
                val track = AudioTrack.Builder()
                    .setAudioAttributes(audioAttr)
                    .setAudioFormat(audioFmt)
                    .setBufferSizeInBytes(buf.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                track.write(buf, 0, buf.size)
                track.play()
                Thread.sleep(durationMs.toLong() + 100)
                track.release()
            } catch (_: Exception) {}
        }.start()
    }

    fun click()      = tone(600f,  60)
    fun navigate()   = tone(800f,  80)
    fun questDone()  = tone(900f, 150)
    fun levelUp()    { tone(440f, 200); Thread.sleep(100); tone(880f, 300) }
    fun correct()    = tone(1000f, 120)
    fun wrong()      = tone(250f,  150)
    fun quizWin()    { listOf(440f, 550f, 660f, 880f).forEachIndexed { i, f -> Thread.sleep(i * 120L); tone(f, 150) } }
    fun equip()      = tone(750f, 100)
    fun timerStart() = tone(500f, 100)
    fun timerDone()  { tone(700f, 150); Thread.sleep(150); tone(900f, 250) }
    fun claim()      = tone(850f, 180)
    fun error()      = tone(200f, 200)
    fun hint()       = tone(660f, 120)
}

// ─────────────────────────────────────────────────────────────────────────────
//  SPACE BACKGROUND
// ─────────────────────────────────────────────────────────────────────────────


@Composable
fun SpaceBackground(content: @Composable () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "galaxy_core")


    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(250000, easing = LinearEasing)
        ), label = "rotation"
    )


    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(200000, easing = LinearEasing)
        ), label = "time"
    )


    val fwProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, delayMillis = 1500),
            repeatMode = RepeatMode.Restart
        ), label = "fireworks"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val random = kotlin.random.Random(88)

            rotate(rotation) {
                repeat(180) { i ->
                    val startX = random.nextFloat() * size.width
                    val startY = random.nextFloat() * size.height
                    val speed = (0.1f + random.nextFloat() * 0.4f)
                    val currentY = (startY + (time * speed)) % size.height

                    drawCircle(
                        color = if (i % 15 == 0) Color(0xFF00E5FF) else Color.White,
                        radius = 1f + random.nextFloat() * 2f,
                        center = Offset(startX, currentY),
                        alpha = 0.3f + random.nextFloat() * 0.5f
                    )
                }

                if (fwProgress > 0.1f && fwProgress < 0.9f) {
                    val fwSeed = (time / 5000).toInt().toLong()
                    val fwRandom = kotlin.random.Random(fwSeed)

                    // RANDOM X at Y (0 to Full Screen)
                    val fx = fwRandom.nextFloat() * size.width
                    val fy = fwRandom.nextFloat() * size.height

                    val p = (fwProgress - 0.1f) / 0.8f

                    repeat(16) { i ->
                        val angle = (360f / 16f) * i
                        val rad = Math.toRadians(angle.toDouble()).toFloat()
                        val dist = 220f * p

                        val centerPoint = Offset(
                            fx + (dist * kotlin.math.cos(rad)),
                            fy + (dist * kotlin.math.sin(rad))
                        )

                        // Neon Glow Effect
                        drawCircle(
                            color = if (i % 2 == 0) Color.Cyan else Color(0xFFFF00FF),
                            radius = 9f * (1f - p),
                            center = centerPoint,
                            alpha = (1f - p) * 0.4f,
                            blendMode = BlendMode.Screen
                        )
                        // Core Light
                        drawCircle(
                            color = Color.White,
                            radius = 3f * (1f - p),
                            center = centerPoint,
                            alpha = 1f - p
                        )
                    }
                }
            }

            // --- LAYER 3: SHOOTING STARS (Random Directions & Full Screen) ---
            val sInterval = 700
            val sStep = (time / sInterval).toInt()
            val sRandom = kotlin.random.Random(sStep.toLong())
            val sProgress = (time % sInterval) / sInterval.toFloat()

            if (sRandom.nextFloat() > 0.5f) { // 50% chance
                val animP = sProgress

                // Randomly pick a starting side (Top, Bottom, Left, Right)
                val side = sRandom.nextInt(4)
                val sX: Float
                val sY: Float
                when(side) {
                    0 -> { sX = sRandom.nextFloat() * size.width; sY = -50f }
                    1 -> { sX = sRandom.nextFloat() * size.width; sY = size.height + 50f }
                    2 -> { sX = -50f; sY = sRandom.nextFloat() * size.height }
                    else -> { sX = size.width + 50f; sY = sRandom.nextFloat() * size.height }
                }

                // Random angle papasok sa screen
                val angle = sRandom.nextFloat() * 360f
                val rad = Math.toRadians(angle.toDouble()).toFloat()

                val totalTravel = size.width * 1.5f
                val curX = sX + (totalTravel * animP * kotlin.math.cos(rad))
                val curY = sY + (totalTravel * animP * kotlin.math.sin(rad))

                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, Color(0xFF00B0FF), Color.White),
                        start = Offset(
                            curX - (300f * kotlin.math.cos(rad)),
                            curY - (300f * kotlin.math.sin(rad))
                        ),
                        end = Offset(curX, curY)
                    ),
                    start = Offset(
                        curX - (200f * kotlin.math.cos(rad)),
                        curY - (200f * kotlin.math.sin(rad))
                    ),
                    end = Offset(curX, curY),
                    strokeWidth = 7f,
                    cap = StrokeCap.Round,
                    alpha = 1f - animP,
                    blendMode = BlendMode.Plus
                )
            }
        }

        // Overlay Content (Text, Buttons, etc.)
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TOP BAR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TopBar(onLogout: (() -> Unit)? = null) {
    var soundOn by remember { mutableStateOf(SoundManager.enabled) }

    Column(
        Modifier.fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.65f))
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: avatar + name/level
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier         = Modifier.size(36.dp).clip(CircleShape)
                        .background(Teal.copy(.25f))
                        .border(1.5.dp, Teal, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint               = Teal,
                        modifier           = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        AppState.name.ifBlank { "Player" },
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White
                    )
                    Text(
                        "Lv.${AppState.level}  ${AppState.rank}",
                        fontSize = 10.sp,
                        color    = Teal
                    )
                }
            }

            // Center: logo
            androidx.compose.foundation.Image(
                painter            = painterResource(id = R.drawable.academiclevelingmainlogo),
                contentDescription = "Academic Leveling",
                modifier           = Modifier
                    .height(80.dp)
                    .widthIn(max = 150.dp),
                contentScale       = ContentScale.Fit
            )

            // Right: coins + sound + logout
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(.10f))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Paid,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        "${AppState.coins}",
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Gold
                    )
                }
                Box(
                    modifier         = Modifier.size(30.dp).clip(CircleShape)
                        .background(Color.White.copy(.10f))
                        .clickable { soundOn = !soundOn; SoundManager.enabled = soundOn },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = if (soundOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Sound",
                        tint               = if (soundOn) Color.White else Color.White.copy(.4f),
                        modifier           = Modifier.size(16.dp)
                    )
                }
                if (onLogout != null) {
                    Box(
                        modifier         = Modifier.size(30.dp).clip(CircleShape)
                            .background(DangerRed.copy(.20f))
                            .clickable { SoundManager.click(); onLogout() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint               = DangerRed,
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // XP bar
        val xpPct = (AppState.xp.toFloat() / AppState.maxXP.coerceAtLeast(1)).coerceIn(0f, 1f)
        Box(
            Modifier.fillMaxWidth().height(3.dp)
                .background(Color.White.copy(.10f))
        ) {
            Box(Modifier.fillMaxWidth(xpPct).fillMaxHeight().background(Teal))
        }
    }

    if (AppState.showLevelUp) {
        LevelUpDialog(AppState.newLevelVal) { AppState.dismissLevelUp() }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  SUB PAGE BAR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SubPageBar(title: String, onBack: () -> Unit) {
    Row(
        modifier          = Modifier.fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.65f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(.12f))
                .border(1.dp, Color.White.copy(.15f), RoundedCornerShape(8.dp))
                .clickable { SoundManager.click(); onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint               = Color.White,
                modifier           = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  LEVEL-UP DIALOG
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LevelUpDialog(newLevel: Int, onDismiss: () -> Unit) {
    LaunchedEffect(Unit) { SoundManager.levelUp() }
    val inf = rememberInfiniteTransition(label = "lvl")
    val sc by inf.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.12f,
        animationSpec = infiniteRepeatable(tween(400), RepeatMode.Reverse),
        label         = "sc"
    )
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.90f))
                .border(2.dp, Gold, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(52.dp).scale(sc)
                )
                Spacer(Modifier.height(6.dp))
                Text("LEVEL UP!", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Gold)
                Text("You are now Level $newLevel", fontSize = 14.sp, color = Color.White)
                Text("Rank: ${AppState.rank}", fontSize = 12.sp, color = rankColor(AppState.rank))
                Spacer(Modifier.height(6.dp))
                Text("+4 Stat Points available!", fontSize = 12.sp, color = Teal)
                Spacer(Modifier.height(18.dp))
                TealButton(
                    label     = "CONTINUE",
                    onClick   = onDismiss,
                    modifier  = Modifier.fillMaxWidth(),
                    color     = Gold,
                    textColor = Color(0xFF1A2332)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  REUSABLE COMPONENTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TealButton(
    label:     String,
    onClick:   () -> Unit,
    modifier:  Modifier = Modifier,
    enabled:   Boolean  = true,
    color:     Color    = Teal,
    textColor: Color    = Color.White
) {
    Box(
        modifier         = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) color else Color.White.copy(.08f))
            .clickable(enabled = enabled) { SoundManager.click(); onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            fontSize   = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color      = if (enabled) textColor else TextMuted
        )
    }
}

@Composable
fun GameCard(
    modifier: Modifier = Modifier,
    content:  @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = BgCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(14.dp), content = content)
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text          = text,
        fontSize      = 11.sp,
        fontWeight    = FontWeight.ExtraBold,
        color         = TextSecondary,
        letterSpacing = 1.sp
    )
}

@Composable
fun ProgressBar(progress: Float, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color.White.copy(.10f))
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
    }
}

@Composable
fun InfoChip(text: String, bg: Color = Color.White.copy(.08f), fg: Color = TextSecondary) {
    Box(
        Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, fontSize = 9.sp, color = fg, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ActionChip(label: String, color: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(color)
            .clickable { SoundManager.click(); onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun StatBox(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier         = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp))
                .background(color.copy(.18f))
                .border(1.dp, color.copy(.45f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label,    fontSize = 9.sp,  fontWeight = FontWeight.ExtraBold, color = color)
                Text("$value", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: Any,
    tint: Color = Color.Gray,
    title: String,
    subtitle: String
) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            when (icon) {
                is ImageVector -> Icon(icon, null, tint = tint, modifier = Modifier.size(38.dp))
                is Int -> Icon(painterResource(id = icon), null, tint = tint, modifier = Modifier.size(38.dp))
                is String -> Text(icon, fontSize = 48.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, fontSize = 12.sp, color = Color.White.copy(0.6f), textAlign = TextAlign.Center)
    }
}

@Composable
fun IconActionChip(
    icon:    ImageVector,
    label:   String,
    color:   Color,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(color)
            .clickable { SoundManager.click(); onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = Color.White,
                modifier           = Modifier.size(13.dp)
            )
            Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}