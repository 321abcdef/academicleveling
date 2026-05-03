package com.example.academicleveling.ui.dungeon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.academicleveling.data.AppState
import com.example.academicleveling.data.Quiz
import com.example.academicleveling.ui.shared.*
import com.example.academicleveling.ui.theme.*

@Composable
fun MyQuizzesScreen(
    onBack:   () -> Unit,
    onCreate: () -> Unit,
    onPlay:   (Quiz) -> Unit,
    onEdit:   (Quiz) -> Unit,
    onDelete: (Quiz) -> Unit
) {
    var deleteTarget by remember { mutableStateOf<Quiz?>(null) }
    var isLoading    by remember { mutableStateOf(false) }
    val listState    = rememberLazyListState()

    LaunchedEffect(Unit) {
        isLoading = true
        AppState.refreshMyQuizzes {
            isLoading = false
        }
    }

    // Infinite Scroll Logic
    val canLoadMore = AppState.canLoadMoreMyQuizzes
    val isMoreLoading = AppState.isMyQuizzesLoading && AppState.myQuizzes.isNotEmpty()

    LaunchedEffect(listState, canLoadMore) {
        snapshotFlow {
            val lastItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            (lastItem?.index ?: 0) to AppState.isMyQuizzesLoading
        }.collect { (lastIndex, isApiLoading) ->
            val totalItems = listState.layoutInfo.totalItemsCount
            if (lastIndex >= totalItems - 5 && canLoadMore && !isApiLoading) {
                AppState.loadMoreMyQuizzes()
            }
        }
    }

    // Confirmation dialog
    deleteTarget?.let { quiz ->
        Dialog(onDismissRequest = { deleteTarget = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A1A2E))
                    .border(1.dp, DangerRed.copy(.4f), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier.size(56.dp).clip(RoundedCornerShape(28.dp))
                        .background(DangerRed.copy(.15f)),
                    Alignment.Center
                ) {
                    Icon(Icons.Default.Delete, null, tint = DangerRed, modifier = Modifier.size(28.dp))
                }

                Text("Delete Quiz?", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)

                Text(
                    "\"${quiz.title}\" will be permanently deleted. This cannot be undone.",
                    fontSize = 12.sp, color = TextSecondary, lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(DangerRed)
                        .clickable {
                            onDelete(quiz)
                            deleteTarget = null
                        }
                        .padding(vertical = 13.dp),
                    Alignment.Center
                ) {
                    Text(
                        "YES, DELETE",
                        fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                        color = Color.White, letterSpacing = 0.5.sp
                    )
                }

                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(.06f))
                        .border(1.dp, Color.White.copy(.15f), RoundedCornerShape(10.dp))
                        .clickable { deleteTarget = null }
                        .padding(vertical = 13.dp),
                    Alignment.Center
                ) {
                    Text(
                        "CANCEL",
                        fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                        color = TextSecondary, letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }

    SpaceBackground {
        Column(Modifier.fillMaxSize()) {
            SubPageBar("MY QUIZZES", onBack)

            if (isLoading && AppState.myQuizzes.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = Teal)
                }
            } else if (AppState.myQuizzes.isEmpty()) {
                Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TealButton("+ CREATE NEW QUIZ", onCreate, Modifier.fillMaxWidth())
                    EmptyState(
                        icon = Icons.Default.Quiz,
                        tint = Teal,
                        title = "No quizzes yet!",
                        subtitle = "Tap Create New Quiz to get started"
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        TealButton("+ CREATE NEW QUIZ", onCreate, Modifier.fillMaxWidth().padding(bottom = 8.dp))
                    }

                    items(
                        items = AppState.myQuizzes,
                        key = { it.id }
                    ) { quiz ->
                        QuizCard(quiz = quiz, showCode = true) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconActionChip(Icons.Default.PlayArrow, "PLAY", SuccessGreen) {
                                    onPlay(quiz)
                                }
                                IconActionChip(Icons.Default.Edit, "EDIT", Blue) {
                                    onEdit(quiz)
                                }
                                IconActionChip(Icons.Default.Delete, "DELETE", DangerRed) { deleteTarget = quiz }
                            }
                        }
                    }

                    if (isMoreLoading) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                                CircularProgressIndicator(color = Teal, modifier = Modifier.size(32.dp))
                            }
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}