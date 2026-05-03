package com.example.academicleveling.ui.dungeon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academicleveling.data.AppState
import com.example.academicleveling.data.Quiz
import com.example.academicleveling.ui.quiz_history.QuizHistoryScreen
import com.example.academicleveling.ui.shared.SoundManager

private enum class DNav { HUB, MY, COMMUNITY, CREATE, EDIT, PLAY, HISTORY, CODE }

enum class DungeonStartTarget { HUB, MY, COMMUNITY, HISTORY, CODE }

@Composable
fun DungeonScreen(
    startTarget: DungeonStartTarget = DungeonStartTarget.HUB,
    onStartTargetConsumed: () -> Unit = {}
) {
    var nav               by remember { mutableStateOf(DNav.HUB) }
    var selectedQuiz      by remember { mutableStateOf<Quiz?>(null) }
    var editingQuiz       by remember { mutableStateOf<Quiz?>(null) }
    var playReturnNav     by remember { mutableStateOf(DNav.HUB) }
    var isLoadingQuiz     by remember { mutableStateOf(false) }

    LaunchedEffect(startTarget) {
        when (startTarget) {
            DungeonStartTarget.HUB -> Unit
            DungeonStartTarget.MY -> nav = DNav.MY
            DungeonStartTarget.COMMUNITY -> nav = DNav.COMMUNITY
            DungeonStartTarget.HISTORY -> nav = DNav.HISTORY
            DungeonStartTarget.CODE -> nav = DNav.CODE
        }
        if (startTarget != DungeonStartTarget.HUB) {
            onStartTargetConsumed()
        }
    }

    if (isLoadingQuiz) {
        Box(Modifier.fillMaxSize().background(com.example.academicleveling.ui.theme.BgPrimary), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = com.example.academicleveling.ui.theme.Teal)
                Spacer(Modifier.height(12.dp))
                Text("Loading quiz content...", color = Color.White, fontSize = 14.sp)
            }
        }
        return
    }

    when (nav) {
        DNav.HUB -> DungeonHub(
            onMy        = { SoundManager.navigate(); nav = DNav.MY },
            onCommunity = { SoundManager.navigate(); nav = DNav.COMMUNITY },
            onHistory   = { SoundManager.navigate(); nav = DNav.HISTORY },
            onEnterCode = { SoundManager.navigate(); nav = DNav.CODE }
        )

        DNav.MY -> MyQuizzesScreen(
            onBack   = { SoundManager.navigate(); nav = DNav.HUB },
            onCreate = { SoundManager.navigate(); nav = DNav.CREATE },
            onPlay   = { q ->
                SoundManager.navigate()
                isLoadingQuiz = true
                AppState.loadQuizFullInfo(q.id) { fullQuiz ->
                    isLoadingQuiz = false
                    if (fullQuiz != null) {
                        selectedQuiz = fullQuiz
                        playReturnNav = DNav.MY
                        nav = DNav.PLAY
                    }
                }
            },
            onEdit   = { q -> SoundManager.navigate(); editingQuiz = q; nav = DNav.EDIT },
            onDelete = { q -> AppState.deleteQuiz(q.id) }
        )

        DNav.COMMUNITY -> CommunityScreen(
            onBack = { SoundManager.navigate(); nav = DNav.HUB },
            onPlay = { q ->
                SoundManager.navigate()
                isLoadingQuiz = true
                AppState.loadQuizFullInfo(q.id) { fullQuiz ->
                    isLoadingQuiz = false
                    if (fullQuiz != null) {
                        selectedQuiz = fullQuiz
                        playReturnNav = DNav.COMMUNITY
                        nav = DNav.PLAY
                    }
                }
            }
        )

        DNav.CODE -> EnterQuizCodeScreen(
            onBack = { SoundManager.navigate(); nav = DNav.HUB },
            onFound = { quiz ->
                isLoadingQuiz = true
                AppState.loadQuizFullInfo(quiz.id) { fullQuiz ->
                    isLoadingQuiz = false
                    if (fullQuiz != null) {
                        selectedQuiz = fullQuiz
                        playReturnNav = DNav.CODE
                        nav = DNav.PLAY
                    }
                }
            }
        )

        DNav.CREATE -> CreateEditScreen(
            existing = null,
            onBack   = { SoundManager.navigate(); nav = DNav.MY },
            onSave   = { q: Quiz -> AppState.addQuiz(q); SoundManager.claim(); nav = DNav.MY }
        )

        DNav.EDIT -> CreateEditScreen(
            existing = editingQuiz,
            onBack   = { SoundManager.navigate(); nav = DNav.MY },
            onSave   = { q: Quiz -> AppState.editQuiz(q); SoundManager.click(); nav = DNav.MY }
        )

        DNav.HISTORY -> QuizHistoryScreen(
            onBack = { SoundManager.navigate(); nav = DNav.HUB }
        )

        DNav.PLAY -> {
            val q = selectedQuiz
            if (q != null) {
                PlayQuizScreen(
                    quiz   = q,
                    onBack = { SoundManager.navigate(); nav = playReturnNav }
                )
            } else {
                nav = DNav.HUB
            }
        }
    }
}