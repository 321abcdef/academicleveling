package com.example.academicleveling.ui.dungeon

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academicleveling.data.*
import com.example.academicleveling.ui.shared.*
import com.example.academicleveling.ui.theme.*

private enum class CreateStep { INFO, QUESTIONS, REVIEW }

@Composable
fun CreateEditScreen(
    existing: Quiz?,
    onBack:   () -> Unit,
    onSave:   (Quiz) -> Unit
) {
    val isEdit = existing != null

    var step by remember { mutableStateOf(if (isEdit) CreateStep.QUESTIONS else CreateStep.INFO) }

    var title            by remember { mutableStateOf(existing?.title      ?: "") }
    var description      by remember { mutableStateOf(existing?.description ?: "") }
    var subject          by remember { mutableStateOf(existing?.subject    ?: "") }
    var gradeLevel       by remember { mutableStateOf(existing?.gradeLevel ?: "All") }
    var difficulty       by remember { mutableStateOf(existing?.difficulty ?: Difficulty.MEDIUM) }
    var quizType         by remember { mutableStateOf(existing?.quizType   ?: QuizType.MULTIPLE_CHOICE) }
    var timerMode        by remember { mutableStateOf(existing?.timerMode  ?: QuizTimerMode.NONE) }
    var timerSecs        by remember { mutableStateOf((existing?.timerSeconds ?: 30).toString()) }
    var shuffleQuestions by remember { mutableStateOf(existing?.shuffleQuestions ?: false) }
    var shuffleOptions   by remember { mutableStateOf(existing?.shuffleOptions   ?: false) }
    var isPublic         by remember { mutableStateOf(true) }
    var titleError       by remember { mutableStateOf(false) }

    var questions     by remember { mutableStateOf(existing?.questions ?: listOf<QuizQuestion>()) }
    var editingQIndex by remember { mutableStateOf<Int?>(null) }
    var formError     by remember { mutableStateOf("") }
    var isSaving      by remember { mutableStateOf(false) }

    var gradeOpen by remember { mutableStateOf(false) }
    var diffOpen  by remember { mutableStateOf(false) }
    var typeOpen  by remember { mutableStateOf(false) }
    var timerOpen by remember { mutableStateOf(false) }

    fun buildCreateRequest(): CreateQuizRequest? {
        if (title.isBlank()) { formError = "Quiz title is required"; return null }
        if (questions.isEmpty()) { formError = "Add at least one question"; return null }
        formError = ""

        val apiQuestions = questions.mapIndexed { idx, q ->
            CreateQuestionRequest(
                questionText = q.q,
                type = when (q.type) {
                    QuizType.MULTIPLE_CHOICE -> "multiple_choice"
                    QuizType.TRUE_FALSE -> "true_false"
                    QuizType.IDENTIFICATION -> "identification"
                    QuizType.MIX -> "multiple_choice"
                },
                points = 1,
                order = idx + 1,
                correctAnswer = if (q.type == QuizType.IDENTIFICATION) q.identAnswer else null,
                choices = if (q.type != QuizType.IDENTIFICATION) {
                    if (q.type == QuizType.TRUE_FALSE) {
                        listOf(
                            CreateChoiceRequest("True", q.correct == 0),
                            CreateChoiceRequest("False", q.correct == 1)
                        )
                    } else {
                        q.opts.filter { it.isNotBlank() }.mapIndexed { i, opt ->
                            CreateChoiceRequest(opt, q.correct == i)
                        }
                    }
                } else null
            )
        }

        return CreateQuizRequest(
            title = title.trim(),
            description = description.ifBlank { "Test your knowledge" },
            subject = subject.trim().ifBlank { "General" },
            gradeLevel = gradeLevel.lowercase().replace(" ", ""),
            type = when (quizType) {
                QuizType.MULTIPLE_CHOICE -> "multiple_choice"
                QuizType.TRUE_FALSE -> "true_false"
                QuizType.IDENTIFICATION -> "identification"
                QuizType.MIX -> "mixed"
            },
            difficulty = difficulty.name.lowercase(),
            timerMode = when (timerMode) {
                QuizTimerMode.WHOLE_QUIZ -> "quiz"
                QuizTimerMode.PER_QUESTION -> "question"
                else -> "none"
            },
            isQuestionShuffled = shuffleQuestions,
            isChoicesShuffled = shuffleOptions,
            isPublic = isPublic,
            questions = apiQuestions
        )
    }

    fun buildQuiz(apiData: QuizFullData): Quiz {
        return Quiz(
            id = apiData.id,
            title = apiData.title,
            description = apiData.description,
            creator = apiData.user.name,
            creatorName = apiData.user.name,
            questions = questions,
            questionsCount = apiData.questionsCount,
            exp = apiData.questionsCount * 20,
            quizType = quizType,
            timerMode = timerMode,
            timerSeconds = if (timerMode != QuizTimerMode.NONE) (timerSecs.toIntOrNull() ?: 30) else 0,
            subject = apiData.subject,
            gradeLevel = apiData.gradeLevel,
            difficulty = difficulty,
            code = apiData.quizCode,
            dateCreated = apiData.createdAt.split("T").firstOrNull() ?: "",
            shuffleQuestions = apiData.isQuestionShuffled,
            shuffleOptions = apiData.isChoicesShuffled
        )
    }

    Column(Modifier.fillMaxSize().background(BgPrimary)) {
        SubPageBar(if (isEdit) "EDIT QUIZ" else "CREATE QUIZ", onBack)

        // Step indicator (create mode only)
        if (!isEdit) {
            Row(
                Modifier.fillMaxWidth().background(BgDark)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                CreateStep.entries.forEachIndexed { idx, s ->
                    val active = step == s
                    val done   = step.ordinal > s.ordinal
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(6.dp))
                            .background(when { active -> Teal; done -> Teal.copy(.3f); else -> Color.White.copy(.08f) })
                            .padding(vertical = 6.dp),
                        Alignment.Center
                    ) {
                        Text(
                            when (s) {
                                CreateStep.INFO      -> "1. INFO"
                                CreateStep.QUESTIONS -> "2. QUESTIONS"
                                CreateStep.REVIEW    -> "3. REVIEW"
                            },
                            fontSize   = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = if (active) Color.White else if (done) Teal else TextMuted
                        )
                    }
                    if (idx < CreateStep.entries.lastIndex) {
                        Text("›", fontSize = 12.sp, color = TextMuted)
                    }
                }
            }
        }

        Column(
            Modifier.fillMaxWidth().weight(1f)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ══════════════════════════════════════════════════════════════
            //  STEP 1 — Quiz Info
            // ══════════════════════════════════════════════════════════════
            if (step == CreateStep.INFO || isEdit) {
                Card(
                    colors    = CardDefaults.cardColors(containerColor = BgCard),
                    shape     = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("QUIZ INFO", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
                            color = TextSecondary, letterSpacing = 1.sp)

                        OutlinedTextField(
                            value         = title,
                            onValueChange = { title = it; titleError = false; formError = "" },
                            modifier      = Modifier.fillMaxWidth(),
                            label         = { Text("Quiz Title *", fontSize = 12.sp) },
                            isError       = titleError,
                            singleLine    = true,
                            shape         = RoundedCornerShape(9.dp),
                            colors        = quizFieldColors()
                        )

                        OutlinedTextField(
                            value         = description,
                            onValueChange = { description = it },
                            modifier      = Modifier.fillMaxWidth(),
                            label         = { Text("Description", fontSize = 12.sp) },
                            singleLine    = false,
                            minLines      = 2,
                            shape         = RoundedCornerShape(9.dp),
                            colors        = quizFieldColors()
                        )

                        OutlinedTextField(
                            value         = subject,
                            onValueChange = { subject = it },
                            modifier      = Modifier.fillMaxWidth(),
                            label         = { Text("Subject (e.g. Math, History)", fontSize = 12.sp) },
                            singleLine    = true,
                            shape         = RoundedCornerShape(9.dp),
                            colors        = quizFieldColors()
                        )

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value         = gradeLevel,
                                    onValueChange = {},
                                    modifier      = Modifier.fillMaxWidth().clickable { gradeOpen = true },
                                    label         = { Text("Grade Level", fontSize = 12.sp) },
                                    enabled       = false,
                                    singleLine    = true,
                                    shape         = RoundedCornerShape(9.dp),
                                    colors        = quizFieldColors()
                                )
                                DropdownMenu(gradeOpen, { gradeOpen = false }, Modifier.background(BgCard)) {
                                    listOf("All","G7","G8","G9","G10","G11","G12",
                                        "College 1","College 2","College 3","College 4").forEach { g ->
                                        DropdownMenuItem(
                                            text    = { Text(g, color = TextPrimary, fontSize = 12.sp) },
                                            onClick = { gradeLevel = g; gradeOpen = false }
                                        )
                                    }
                                }
                            }
                            Box(Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value         = difficultyLabel(difficulty),
                                    onValueChange = {},
                                    modifier      = Modifier.fillMaxWidth().clickable { diffOpen = true },
                                    label         = { Text("Difficulty", fontSize = 12.sp) },
                                    enabled       = false,
                                    singleLine    = true,
                                    shape         = RoundedCornerShape(9.dp),
                                    colors        = quizFieldColors()
                                )
                                DropdownMenu(diffOpen, { diffOpen = false }, Modifier.background(BgCard)) {
                                    Difficulty.entries.forEach { d ->
                                        DropdownMenuItem(
                                            text    = { Text(difficultyLabel(d), color = TextPrimary, fontSize = 12.sp) },
                                            onClick = { difficulty = d; diffOpen = false }
                                        )
                                    }
                                }
                            }
                        }

                        Box(Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value         = quizTypeLabel(quizType),
                                onValueChange = {},
                                modifier      = Modifier.fillMaxWidth().clickable { typeOpen = true },
                                label         = { Text("Quiz Type", fontSize = 12.sp) },
                                enabled       = false,
                                singleLine    = true,
                                shape         = RoundedCornerShape(9.dp),
                                colors        = quizFieldColors()
                            )
                            DropdownMenu(typeOpen, { typeOpen = false }, Modifier.background(BgCard)) {
                                listOf(
                                    QuizType.MULTIPLE_CHOICE,
                                    QuizType.TRUE_FALSE,
                                    QuizType.IDENTIFICATION,
                                    QuizType.MIX
                                ).forEach { t ->
                                    DropdownMenuItem(
                                        text    = { Text(quizTypeLabel(t), color = TextPrimary, fontSize = 12.sp) },
                                        onClick = { quizType = t; typeOpen = false }
                                    )
                                }
                            }
                        }

                        Row(Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment     = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value         = timerMode.name.replace("_", " "),
                                    onValueChange = {},
                                    modifier      = Modifier.fillMaxWidth().clickable { timerOpen = true },
                                    label         = { Text("Timer", fontSize = 12.sp) },
                                    enabled       = false,
                                    singleLine    = true,
                                    shape         = RoundedCornerShape(9.dp),
                                    colors        = quizFieldColors()
                                )
                                DropdownMenu(timerOpen, { timerOpen = false }, Modifier.background(BgCard)) {
                                    QuizTimerMode.entries.forEach { m ->
                                        DropdownMenuItem(
                                            text    = { Text(m.name.replace("_", " "), color = TextPrimary, fontSize = 12.sp) },
                                            onClick = { timerMode = m; timerOpen = false }
                                        )
                                    }
                                }
                            }
                            if (timerMode != QuizTimerMode.NONE) {
                                OutlinedTextField(
                                    value           = timerSecs,
                                    onValueChange   = { timerSecs = it.filter(Char::isDigit).take(4) },
                                    modifier        = Modifier.width(90.dp),
                                    label           = { Text("Secs", fontSize = 11.sp) },
                                    singleLine      = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape           = RoundedCornerShape(9.dp),
                                    colors          = quizFieldColors()
                                )
                            }
                        }

                        // Timer note — replaced ⏱ emoji with Icon inline
                        if (timerMode != QuizTimerMode.NONE) {
                            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                .background(Gold.copy(.08f)).padding(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Timer, null, tint = Gold,
                                        modifier = Modifier.size(13.dp))
                                    Text(
                                        if (timerMode == QuizTimerMode.WHOLE_QUIZ)
                                            "Whole quiz has ${timerSecs}s total. Time Warp power-up will work."
                                        else
                                            "Each question has ${timerSecs}s. Auto-submits when time runs out.",
                                        fontSize = 11.sp, color = Gold
                                    )
                                }
                            }
                        }

                        // Shuffle options — replaced 🔀 emoji with Shuffle icon
                        Text("SHUFFLE OPTIONS", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
                            color = TextSecondary, letterSpacing = 1.sp)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ToggleChip(
                                icon     = Icons.Default.Shuffle,
                                label    = "Shuffle Questions",
                                active   = shuffleQuestions,
                                modifier = Modifier.weight(1f),
                                onClick  = { shuffleQuestions = !shuffleQuestions }
                            )
                            ToggleChip(
                                icon     = Icons.Default.Shuffle,
                                label    = "Shuffle Options",
                                active   = shuffleOptions,
                                modifier = Modifier.weight(1f),
                                onClick  = { shuffleOptions = !shuffleOptions }
                            )
                        }
                    }
                }

                if (!isEdit) {
                    TealButton(
                        label    = "NEXT: ADD QUESTIONS",
                        onClick  = {
                            if (title.isBlank()) { titleError = true; SoundManager.error() }
                            else { SoundManager.click(); step = CreateStep.QUESTIONS }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ══════════════════════════════════════════════════════════════
            //  STEP 2 — Questions
            // ══════════════════════════════════════════════════════════════
            if (step == CreateStep.QUESTIONS || isEdit) {
                Card(
                    colors    = CardDefaults.cardColors(containerColor = BgCard),
                    shape     = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("QUESTIONS  (${questions.size})", fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold, color = TextSecondary, letterSpacing = 1.sp)
                            ActionChip("+ ADD", Teal) {
                                val newType = if (quizType == QuizType.MIX) QuizType.MULTIPLE_CHOICE else quizType
                                val blank = when (newType) {
                                    QuizType.TRUE_FALSE     -> QuizQuestion(id = 0, q = "", opts = emptyList(), optIds = emptyList(), correct = 0, exp = "", type = QuizType.TRUE_FALSE, identAnswer = "")
                                    QuizType.IDENTIFICATION -> QuizQuestion(id = 0, q = "", opts = emptyList(), optIds = emptyList(), correct = 0, exp = "", type = QuizType.IDENTIFICATION, identAnswer = "")
                                    else                    -> QuizQuestion(id = 0, q = "", opts = listOf("", "", "", ""), optIds = emptyList(), correct = 0, exp = "", type = QuizType.MULTIPLE_CHOICE, identAnswer = "")
                                }
                                questions     = questions + blank
                                editingQIndex = questions.lastIndex
                            }
                        }

                        if (questions.isEmpty()) {
                            Box(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                    .background(BgCardDark).padding(20.dp),
                                Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Edit, null, tint = TextMuted,
                                        modifier = Modifier.size(32.dp))
                                    Text("No questions yet", fontSize = 13.sp, color = TextMuted)
                                    Text("Tap + ADD to create your first question",
                                        fontSize = 11.sp, color = TextMuted)
                                }
                            }
                        } else {
                            questions.forEachIndexed { i, q ->
                                QuestionEditorRow(
                                    index     = i,
                                    q         = q,
                                    quizType  = quizType,
                                    isEditing = editingQIndex == i,
                                    onToggle  = { editingQIndex = if (editingQIndex == i) null else i },
                                    onUpdate  = { updated ->
                                        questions = questions.toMutableList().also { it[i] = updated }
                                    },
                                    onDelete  = {
                                        questions = questions.toMutableList().also { it.removeAt(i) }
                                        if (editingQIndex == i) editingQIndex = null
                                    }
                                )
                            }
                        }
                    }
                }

                if (!isEdit) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TealButton("BACK", { step = CreateStep.INFO },
                            Modifier.weight(1f), color = BgCardDark, textColor = TextPrimary)
                        TealButton("NEXT: REVIEW",
                            {
                                if (questions.isEmpty()) {
                                    formError = "Add at least one question"; SoundManager.error()
                                } else {
                                    formError = ""; SoundManager.click(); step = CreateStep.REVIEW
                                }
                            },
                            Modifier.weight(2f)
                        )
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════
            //  STEP 3 — Review & Save
            // ══════════════════════════════════════════════════════════════
            if (step == CreateStep.REVIEW || isEdit) {
                if (step == CreateStep.REVIEW) {
                    Card(
                        colors    = CardDefaults.cardColors(containerColor = BgCard),
                        shape     = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("REVIEW", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
                                color = TextSecondary, letterSpacing = 1.sp)
                            SummaryRow("Title",           title)
                            SummaryRow("Subject",         subject.ifBlank { "General" })
                            SummaryRow("Grade",           gradeLevel)
                            SummaryRow("Difficulty",      difficultyLabel(difficulty))
                            SummaryRow("Type",            quizTypeLabel(quizType))
                            SummaryRow("Timer",
                                if (timerMode == QuizTimerMode.NONE) "None"
                                else "${timerMode.name.replace("_", " ")} - ${timerSecs}s")
                            SummaryRow("Questions",       "${questions.size}")
                            SummaryRow("Shuffle Qs",      if (shuffleQuestions) "Yes" else "No")
                            SummaryRow("Shuffle Options", if (shuffleOptions) "Yes" else "No")
                        }
                    }
                }

                if (formError.isNotBlank()) {
                    Text(formError, color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (!isEdit) {
                    TealButton("BACK", { step = CreateStep.QUESTIONS },
                        Modifier.fillMaxWidth(), color = BgCardDark, textColor = TextPrimary)
                }

                // Save button
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(if (isSaving) Teal.copy(0.5f) else Teal)
                        .clickable(enabled = !isSaving) {
                            val req = buildCreateRequest()
                            if (req != null) {
                                isSaving = true
                                if (isEdit) {
                                    AppState.updateQuizWithApi(
                                        quizId = existing.id,
                                        request = req,
                                        onComplete = { updated ->
                                            isSaving = false
                                            if (updated != null) {
                                                SoundManager.claim()
                                                onSave(updated)
                                            } else {
                                                formError = "Failed to update quiz"
                                                SoundManager.error()
                                            }
                                        }
                                    )
                                } else {
                                    ApiRepository.createQuiz(
                                        request = req,
                                        onSuccess = { resp ->
                                            isSaving = false
                                            SoundManager.claim()
                                            onSave(buildQuiz(resp.data))
                                        },
                                        onError = { err ->
                                            isSaving = false
                                            formError = err
                                            SoundManager.error()
                                        }
                                    )
                                }
                            }
                        }
                        .padding(vertical = 13.dp),
                    Alignment.Center
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Save, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text(
                                if (isEdit) "SAVE CHANGES" else "SAVE QUIZ",
                                fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Question editor row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuestionEditorRow(
    index:     Int,
    q:         QuizQuestion,
    quizType:  QuizType,
    isEditing: Boolean,
    onToggle:  () -> Unit,
    onUpdate:  (QuizQuestion) -> Unit,
    onDelete:  () -> Unit
) {
    var qText       by remember(index) { mutableStateOf(q.q) }
    var explanation by remember(index) { mutableStateOf(q.exp) }
    var identAnswer by remember(index) { mutableStateOf(q.identAnswer) }
    var correctIdx  by remember(index) { mutableStateOf(q.correct) }
    var opts        by remember(index) {
        mutableStateOf(q.opts.toMutableList().also { while (it.size < 4) it.add("") }.toList())
    }
    var qType by remember(index) { mutableStateOf(q.type) }

    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(BgCardDark)
            .border(1.dp,
                if (isEditing) Teal.copy(.4f) else Color.Transparent,
                RoundedCornerShape(10.dp))
    ) {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(12.dp),
            Arrangement.SpaceBetween, Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(26.dp).clip(RoundedCornerShape(6.dp)).background(Teal.copy(.2f)),
                    Alignment.Center) {
                    Text("${index + 1}", fontSize = 11.sp, color = Teal, fontWeight = FontWeight.ExtraBold)
                }
                Column {
                    Text(q.q.ifBlank { "(empty question)" }, fontSize = 12.sp,
                        color = if (q.q.isBlank()) TextMuted else TextPrimary,
                        fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(quizTypeLabel(q.type), fontSize = 9.sp, color = TextSecondary)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(if (isEditing) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = TextMuted, modifier = Modifier.size(16.dp))
                Icon(Icons.Default.Delete, null, tint = DangerRed,
                    modifier = Modifier.size(16.dp).clickable { onDelete() })
            }
        }

        AnimatedVisibility(visible = isEditing) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

                if (quizType == QuizType.MIX) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Type:", fontSize = 11.sp, color = TextSecondary,
                            modifier = Modifier.align(Alignment.CenterVertically))
                        listOf(QuizType.MULTIPLE_CHOICE to "MC", QuizType.TRUE_FALSE to "T/F",
                            QuizType.IDENTIFICATION to "ID").forEach { (qt, lbl) ->
                            val active = qType == qt
                            Box(
                                Modifier.clip(RoundedCornerShape(6.dp))
                                    .background(if (active) Accent else BgCard)
                                    .clickable { qType = qt; onUpdate(q.copy(q = qText, type = qt)) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(lbl, fontSize = 10.sp,
                                    color = if (active) Color.White else TextMuted,
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value         = qText,
                    onValueChange = { qText = it; onUpdate(q.copy(q = it, type = qType)) },
                    modifier      = Modifier.fillMaxWidth().height(72.dp),
                    label         = { Text("Question *", fontSize = 11.sp) },
                    shape         = RoundedCornerShape(8.dp),
                    colors        = quizFieldColors()
                )

                when (qType) {
                    QuizType.MULTIPLE_CHOICE -> {
                        Text("Options — tap circle to mark correct answer:",
                            fontSize = 11.sp, color = TextSecondary)
                        opts.forEachIndexed { i, opt ->
                            Row(Modifier.fillMaxWidth(),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    Modifier.size(24.dp).clip(RoundedCornerShape(50.dp))
                                        .background(if (correctIdx == i) SuccessGreen else BgCard)
                                        .border(1.5.dp,
                                            if (correctIdx == i) SuccessGreen else TextMuted,
                                            RoundedCornerShape(50.dp))
                                        .clickable {
                                            correctIdx = i
                                            onUpdate(q.copy(q = qText, opts = opts, correct = i, type = qType))
                                        },
                                    Alignment.Center
                                ) {
                                    if (correctIdx == i) {
                                        Text("✓", fontSize = 9.sp, color = Color.White,
                                            fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                                OutlinedTextField(
                                    value         = opt,
                                    onValueChange = { v ->
                                        val newOpts = opts.toMutableList().also { list -> list[i] = v }
                                        opts = newOpts
                                        onUpdate(q.copy(q = qText, opts = newOpts, correct = correctIdx, type = qType))
                                    },
                                    modifier   = Modifier.weight(1f),
                                    label      = { Text("Option ${('A' + i)}", fontSize = 10.sp) },
                                    singleLine = true,
                                    shape      = RoundedCornerShape(7.dp),
                                    colors     = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor   = if (correctIdx == i) Teal else BgCardDark,
                                        unfocusedBorderColor = if (correctIdx == i) Teal.copy(.5f) else BgCardDark
                                    )
                                )
                            }
                        }
                        Text("Tap circle to set correct answer", fontSize = 9.sp, color = TextMuted)
                    }

                    QuizType.TRUE_FALSE -> {
                        Text("Select correct answer:", fontSize = 11.sp, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("True" to 0, "False" to 1).forEach { (lbl, idx) ->
                                Box(
                                    Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                        .background(if (correctIdx == idx) SuccessGreen.copy(.15f) else BgCard)
                                        .border(1.5.dp,
                                            if (correctIdx == idx) SuccessGreen else BgCardDark,
                                            RoundedCornerShape(8.dp))
                                        .clickable {
                                            correctIdx = idx
                                            onUpdate(q.copy(q = qText, correct = idx, type = qType))
                                        }
                                        .padding(vertical = 12.dp),
                                    Alignment.Center
                                ) {
                                    Text(lbl, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                                        color = if (correctIdx == idx) SuccessGreen else TextSecondary)
                                }
                            }
                        }
                    }

                    QuizType.IDENTIFICATION -> {
                        OutlinedTextField(
                            value         = identAnswer,
                            onValueChange = {
                                identAnswer = it
                                onUpdate(q.copy(q = qText, identAnswer = it, type = qType))
                            },
                            modifier      = Modifier.fillMaxWidth(),
                            label         = { Text("Correct Answer *", fontSize = 11.sp) },
                            placeholder   = { Text("e.g. Photosynthesis", color = TextMuted, fontSize = 12.sp) },
                            singleLine    = true,
                            shape         = RoundedCornerShape(8.dp),
                            colors        = quizFieldColors()
                        )
                    }

                    else -> {}
                }

                OutlinedTextField(
                    value         = explanation,
                    onValueChange = {
                        explanation = it
                        onUpdate(q.copy(q = qText, exp = it, type = qType))
                    },
                    modifier    = Modifier.fillMaxWidth(),
                    label       = { Text("Explanation (optional)", fontSize = 11.sp) },
                    placeholder = { Text("Why is this the correct answer?", color = TextMuted, fontSize = 11.sp) },
                    singleLine  = true,
                    shape       = RoundedCornerShape(8.dp),
                    colors      = quizFieldColors()
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ToggleChip(
    icon:     androidx.compose.ui.graphics.vector.ImageVector,
    label:    String,
    active:   Boolean,
    modifier: Modifier = Modifier,
    onClick:  () -> Unit
) {
    Box(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) Teal.copy(.15f) else BgCardDark)
            .border(1.dp, if (active) Teal else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 10.dp),
        Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(icon, null, tint = if (active) Teal else TextMuted, modifier = Modifier.size(13.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = if (active) Teal else TextMuted, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = TextSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
private fun quizFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = Teal,
    unfocusedBorderColor = BgCardDark,
    disabledBorderColor  = BgCardDark,
    disabledTextColor    = TextPrimary
)