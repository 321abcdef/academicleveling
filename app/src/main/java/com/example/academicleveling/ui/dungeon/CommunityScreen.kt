package com.example.academicleveling.ui.dungeon

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academicleveling.data.*
import com.example.academicleveling.ui.shared.*
import com.example.academicleveling.ui.theme.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.collectLatest

@OptIn(FlowPreview::class, ExperimentalFoundationApi::class)
@Composable
fun CommunityScreen(onBack: () -> Unit, onPlay: (Quiz) -> Unit) {
    var searchInput by remember { mutableStateOf("") }
    var debouncedSearch by remember { mutableStateOf("") }
    var filterDiff  by remember { mutableStateOf<Difficulty?>(null) }
    var filterGrade by remember { mutableStateOf("All") }
    var gradeOpen   by remember { mutableStateOf(false) }
    var isLoading   by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Logic: Debounce search input
    val searchFlow = remember { MutableStateFlow("") }
    LaunchedEffect(searchInput) {
        searchFlow.value = searchInput
    }
    LaunchedEffect(searchFlow) {
        searchFlow
            .debounce(500L)
            .collectLatest { debouncedSearch = it }
    }

    val gradeOptions = remember {
        listOf(
            "All", "G7", "G8", "G9", "G10", "G11", "G12", "College"
        )
    }

    // Load quizzes from API when filters change
    LaunchedEffect(debouncedSearch, filterDiff, filterGrade) {
        isLoading = true
        val apiGrade = when(filterGrade) {
            "All" -> null
            "College" -> "college"
            else -> filterGrade.lowercase() // handles g7, g8, etc.
        }
        AppState.refreshCommunityQuizzes(
            search = if (debouncedSearch.isBlank()) null else debouncedSearch,
            difficulty = filterDiff?.name?.lowercase(),
            gradeLevel = apiGrade,
            onComplete = { 
                isLoading = false
            }
        )
    }

    // Quiz Data: Filtered list is now just what's in AppState.communityQuizzes
    val filtered = AppState.communityQuizzes

    // Auto-scroll to top on filter change
    LaunchedEffect(debouncedSearch, filterDiff, filterGrade) {
        if (filtered.isNotEmpty()) listState.animateScrollToItem(0)
    }

    SpaceBackground {
        Column(Modifier.fillMaxSize()) {
            SubPageBar("COMMUNITY QUIZZES", onBack)

            // SEARCH & FILTER SECTION (Solid Background)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A2E)) // Solid dark navy
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search Field
                OutlinedTextField(
                    value         = searchInput,
                    onValueChange = { searchInput = it },
                    modifier      = Modifier.fillMaxWidth(),
                    placeholder   = {
                        Text(
                            "Search by title or code...",
                            fontSize = 13.sp,
                            color = Color.White.copy(0.6f) // Clear placeholder
                        )
                    },
                    singleLine = true,
                    shape      = RoundedCornerShape(12.dp),
                    colors     = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = Teal,
                        unfocusedBorderColor    = Color.White, // Solid White Border
                        focusedTextColor        = Color.White,
                        unfocusedTextColor      = Color.White,
                        focusedContainerColor   = Color(0xFF2D2D50), // Solid Deep Purple/Blue
                        unfocusedContainerColor = Color(0xFF252545), // Solid Darker Blue
                        cursorColor             = Teal
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.Search, null, tint = Teal, modifier = Modifier.padding(start = 8.dp))
                    },
                    trailingIcon = if (searchInput.isNotBlank()) {
                        {
                            IconButton(onClick = { searchInput = "" }) {
                                Text("✕", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else null
                )

                // Difficulty Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip("All",    Teal,                              filterDiff == null)              { filterDiff = null }
                    FilterChip("Easy",   difficultyColor(Difficulty.EASY),   filterDiff == Difficulty.EASY)   { filterDiff = Difficulty.EASY }
                    FilterChip("Medium", difficultyColor(Difficulty.MEDIUM), filterDiff == Difficulty.MEDIUM) { filterDiff = Difficulty.MEDIUM }
                    FilterChip("Hard",   difficultyColor(Difficulty.HARD),   filterDiff == Difficulty.HARD)   { filterDiff = Difficulty.HARD }
                }

                // Grade & Clear Filters Row
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "GRADE:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Box {
                        FilterChip(filterGrade, Teal, true) { gradeOpen = true }
                        DropdownMenu(
                            expanded         = gradeOpen,
                            onDismissRequest = { gradeOpen = false },
                            modifier         = Modifier.background(Color(0xFF2D2D50))
                        ) {
                            gradeOptions.forEach { g ->
                                DropdownMenuItem(
                                    text    = { Text(g, color = Color.White, fontSize = 14.sp) },
                                    onClick = { filterGrade = g; gradeOpen = false }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Highly visible Reset Button
                    if (filterDiff != null || filterGrade != "All") {
                        TextButton(
                            onClick = { filterDiff = null; filterGrade = "All" },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Text(
                                "RESET FILTERS",
                                fontSize = 11.sp,
                                color = DangerRed,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // RESULTS LIST
            if (isLoading && filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = Teal)
                }
            } else if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    EmptyState(
                        icon     = Icons.Default.Search,
                        tint     = Teal,
                        title    = "No quizzes found",
                        subtitle = "Try a different search or filter"
                    )
                }
            } else {
                LazyColumn(
                    state               = listState,
                    modifier            = Modifier.fillMaxWidth().weight(1f),
                    contentPadding      = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Result Counter Header
                    stickyHeader {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0D0D1A)) // Solid background for sticky header
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                "SHOWING ${filtered.size} QUIZZES",
                                fontSize = 12.sp,
                                color = Teal,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    items(
                        items = filtered,
                        key   = { it.id }
                    ) { quiz ->
                        QuizCard(quiz = quiz, showCode = true) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        "By: ${quiz.creator}",
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (quiz.dateCreated.isNotBlank())
                                        Text(
                                            quiz.dateCreated,
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                }
                                ActionChip("PLAY NOW", SuccessGreen) { onPlay(quiz) }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}