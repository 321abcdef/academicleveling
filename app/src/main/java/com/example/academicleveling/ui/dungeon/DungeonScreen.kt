package com.example.academicleveling.ui.dungeon

import androidx.compose.runtime.*
import com.example.academicleveling.data.AppState
import com.example.academicleveling.data.Quiz
import com.example.academicleveling.ui.quiz_history.QuizHistoryScreen
import com.example.academicleveling.ui.shared.SoundManager

private enum class DNav { HUB, MY, COMMUNITY, CREATE, EDIT, PLAY, HISTORY }

@Composable
fun DungeonScreen() {
    var nav               by remember { mutableStateOf(DNav.HUB) }
    var selectedQuiz      by remember { mutableStateOf<Quiz?>(null) }
    var editingQuiz       by remember { mutableStateOf<Quiz?>(null) }
    var playFromCommunity by remember { mutableStateOf(false) }

    when (nav) {
        DNav.HUB -> DungeonHub(
            onMy        = { SoundManager.navigate(); nav = DNav.MY },
            onCommunity = { SoundManager.navigate(); nav = DNav.COMMUNITY },
            onHistory   = { SoundManager.navigate(); nav = DNav.HISTORY },
            onCodeFound = { quiz ->
                SoundManager.navigate()
                selectedQuiz = quiz; playFromCommunity = true; nav = DNav.PLAY
            }
        )

        DNav.MY -> MyQuizzesScreen(
            onBack   = { SoundManager.navigate(); nav = DNav.HUB },
            onCreate = { SoundManager.navigate(); nav = DNav.CREATE },
            onPlay   = { q -> SoundManager.navigate(); selectedQuiz = q; playFromCommunity = false; nav = DNav.PLAY },
            onEdit   = { q -> SoundManager.navigate(); editingQuiz = q; nav = DNav.EDIT },
            onDelete = { q -> AppState.deleteQuiz(q.id) }
        )

        DNav.COMMUNITY -> CommunityScreen(
            onBack = { SoundManager.navigate(); nav = DNav.HUB },
            onPlay = { q -> SoundManager.navigate(); selectedQuiz = q; playFromCommunity = true; nav = DNav.PLAY }
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
                    onBack = { SoundManager.navigate(); nav = if (playFromCommunity) DNav.COMMUNITY else DNav.MY }
                )
            } else {
                nav = DNav.HUB
            }
        }
    }
}