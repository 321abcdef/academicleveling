package com.example.academicleveling.ui.dungeon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.academicleveling.data.AppState
import com.example.academicleveling.data.Quiz
import com.example.academicleveling.ui.shared.*
import com.example.academicleveling.ui.theme.*

@Composable
fun DungeonHub(
    onMy:        () -> Unit,
    onCommunity: () -> Unit,
    onHistory:   () -> Unit,
    onCodeFound: (Quiz) -> Unit
) {
    var showCodeDialog by remember { mutableStateOf(false) }

    SpaceBackground {
        Column(Modifier.fillMaxSize()) {
            TopBar()
            Column(
                modifier            = Modifier.fillMaxWidth().weight(1f)
                    .verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // Header — Lock icon replaces Castle (extended)
                Icon(
                    imageVector        = Icons.Default.Lock,
                    contentDescription = null,
                    tint               = Teal,
                    modifier           = Modifier.size(52.dp).align(Alignment.CenterHorizontally)
                )
                Text(
                    "KNOWLEDGE VAULT",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = TextPrimary,
                    modifier   = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    "Create, share, and master quizzes",
                    fontSize = 12.sp,
                    color    = TextSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(8.dp))

                // Book  replaces LibraryBooks (extended)
                HubCard(Icons.Default.Book,          "MY QUIZZES",         "Create & manage your own quizzes",              Teal,         onMy)
                HubCard(Icons.Default.Verified,      "COMMUNITY QUIZZES",  "Discover quizzes from other players",           Accent,       onCommunity)
                HubCard(Icons.Default.List,          "QUIZ HISTORY",       "Review correct & wrong answers per quiz",       Gold,         onHistory)
                // VpnKey replaces Key (extended)
                HubCard(Icons.Default.VpnKey,        "ENTER QUIZ CODE",    "Join a quiz shared by a classmate or teacher",  SuccessGreen, { showCodeDialog = true })

                Spacer(Modifier.height(80.dp))
            }
        }
    }

    if (showCodeDialog) {
        CodeEntryDialog(
            onDismiss = { showCodeDialog = false },
            onFound   = { quiz -> showCodeDialog = false; onCodeFound(quiz) }
        )
    }
}

@Composable
private fun HubCard(
    icon:     ImageVector,
    title:    String,
    subtitle: String,
    accent:   Color,
    onClick:  () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1A1A2E))
            .border(1.dp, accent.copy(.5f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(.15f)),
                Alignment.Center
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title,    fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text(subtitle, fontSize = 11.sp, color = Color.White.copy(.7f))
            }
            // ArrowForward replaces ChevronRight (extended)
            Icon(Icons.Default.ArrowForward, null, tint = accent, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun CodeEntryDialog(onDismiss: () -> Unit, onFound: (Quiz) -> Unit) {
    var code  by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            Modifier.clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1A1A2E))
                .border(2.dp, Teal.copy(.4f), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VpnKey, null, tint = Teal, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("ENTER QUIZ CODE", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                }
                Spacer(Modifier.height(4.dp))
                Text("6-character code from a classmate or teacher", fontSize = 11.sp, color = TextSecondary)
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value         = code,
                    onValueChange = { code = it.uppercase().take(8); error = "" },
                    modifier      = Modifier.fillMaxWidth(),
                    placeholder   = { Text("e.g. CPP001", color = TextMuted) },
                    singleLine    = true,
                    isError       = error.isNotBlank(),
                    shape         = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Teal,
                        unfocusedBorderColor = Color.White.copy(.25f),
                        focusedTextColor     = Color.White,
                        unfocusedTextColor   = Color.White
                    )
                )
                if (error.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(error, color = DangerRed, fontSize = 11.sp)
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TealButton("CANCEL", onDismiss, Modifier.weight(1f),
                        color = Color(0xFF0D0D1A), textColor = TextPrimary)
                    TealButton("FIND QUIZ", {
                        val quiz = AppState.findByCode(code)
                        if (quiz != null) onFound(quiz)
                        else { error = "Code not found. Check and try again."; SoundManager.error() }
                    }, Modifier.weight(1f))
                }
            }
        }
    }
}