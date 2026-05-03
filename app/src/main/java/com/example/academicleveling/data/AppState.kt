package com.example.academicleveling.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object AppState {

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences("academic_leveling_v8", Context.MODE_PRIVATE)
        load()
        if (loggedIn) refreshUserData()
    }

    // ── Auth ──────────────────────────────────────────────────────────────
    var name     by mutableStateOf("")
    var email    by mutableStateOf("")
    var grade    by mutableStateOf<GradeLevel?>(null)
    var loggedIn by mutableStateOf(false)
    var token    by mutableStateOf("")

    // ── Progression ───────────────────────────────────────────────────────
    var level   by mutableStateOf(1)
    var xp      by mutableStateOf(0)
    var totalXP by mutableStateOf(0)
    var maxXP   by mutableStateOf(xpForNext(1))
    var rank    by mutableStateOf(Rank.E)

    // ── Economy ───────────────────────────────────────────────────────────
    var coins              by mutableStateOf(15000)
    var timeWarpCount      by mutableStateOf(0)
    var secondChanceCount  by mutableStateOf(0)
    var hintCount          by mutableStateOf(0)

    // ── Tracking ──────────────────────────────────────────────────────────
    var streak           by mutableStateOf(0)
    var totalMins        by mutableStateOf(0)
    var quizzesCompleted by mutableStateOf(0)
    var streakAtRisk     by mutableStateOf(false)

    // ── UI helpers ────────────────────────────────────────────────────────
    var showLevelUp by mutableStateOf(false)
    var newLevelVal by mutableStateOf(1)

    // ── Inventory / Equipment ─────────────────────────────────────────────
    var inventory: List<Item>     by mutableStateOf(DEFAULT_INVENTORY)
    var equipment: Equipment      by mutableStateOf(DEFAULT_EQUIPMENT)

    // ── Achievements / Sessions ───────────────────────────────────────────
    var achievements:   List<Achievement>      by mutableStateOf(ALL_ACHIEVEMENTS)
    var sessionHistory: List<SessionEntry>     by mutableStateOf(emptyList())
    var quizHistory:    List<QuizHistoryEntry> by mutableStateOf(emptyList())

    // ── Quests ────────────────────────────────────────────────────────────
    var quests: List<Quest> by mutableStateOf(emptyList())
    var weeklyQuests: List<Quest> by mutableStateOf(emptyList())
    var dailyBonusQuest: Quest? by mutableStateOf(null)
    var weeklyBonusQuest: Quest? by mutableStateOf(null)

    // ── Quizzes ───────────────────────────────────────────────────────────
    var myQuizzes:        List<Quiz> by mutableStateOf(emptyList())
    var communityQuizzes: List<Quiz> by mutableStateOf(emptyList())

    val SHOP_ITEMS: List<ShopItem> = listOf(
        ShopItem(1, "Time Warp",       "Adds +30s to your quiz timer.",               100, ShopEffect.TIME_WARP),
        ShopItem(2, "50/50",           "Eliminates 2 wrong options on MC questions.",  200, ShopEffect.SECOND_CHANCE),
        ShopItem(3, "Hint",            "Reveals the correct answer once.",             500, ShopEffect.HINT),
        ShopItem(5, "XP Boost",        "Instant +100 XP to your level.",               300, ShopEffect.XP_BOOST)
    )

    // ══════════════════════════════════════════════════════════════════════
    //  AUTH
    // ══════════════════════════════════════════════════════════════════════

    fun login(n: String, e: String, g: GradeLevel) {
        name = n; email = e; grade = g; loggedIn = true; save()
    }

    fun quickLogin(n: String = "", e: String = "") {
        if (n.isNotBlank()) name = n
        if (name.isBlank()) name = "Player"
        if (e.isNotBlank()) email = e
        loggedIn = true; save()
    }

    fun logout() { loggedIn = false; name = ""; token = ""; ApiRepository.logout(); save() }

    fun loginWithApi(response: LoginResponse) {
        token = response.token
        ApiRepository.setToken(token)
        name = response.data.username
        email = response.data.email
        level = response.data.progress.level
        xp = response.data.progress.currentExp
        maxXP = response.data.progress.expToNextLevel
        coins = response.data.coins
        totalXP = response.data.totalExp ?: 0
        loggedIn = true
        save()

        // Immediately fetch all user data including quests
        refreshUserData()
    }

    fun registerWithApi(response: RegisterResponse) {
        token = response.token
        ApiRepository.setToken(token)
        name = response.data.username
        email = response.data.email
        level = response.data.progress.level
        xp = response.data.progress.currentExp
        maxXP = response.data.progress.expToNextLevel
        coins = response.data.coins
        totalXP = response.data.totalExp ?: 0
        loggedIn = true
        save()

        // Immediately fetch all user data including quests
        refreshUserData()
    }

    fun refreshUserData(onComplete: () -> Unit = {}) {
        if (!loggedIn || token.isEmpty()) { onComplete(); return }
        ApiRepository.getUserInfo(
            onSuccess = { response ->
                name = response.data.username
                email = response.data.email
                level = response.data.progress.level
                xp = response.data.progress.currentExp
                maxXP = response.data.progress.expToNextLevel
                coins = response.data.coins
                totalXP = response.data.totalExp ?: 0
                save()
                
                // Fetch stats too
                refreshUserStats(onComplete)
                refreshSessionHistory()
                refreshQuests()
                refreshAchievements()
            },
            onError = { 
                refreshUserStats(onComplete)
                refreshSessionHistory()
                refreshQuests()
            }
        )
    }

    fun refreshUserStats(onComplete: () -> Unit = {}) {
        if (!loggedIn || token.isEmpty()) { onComplete(); return }
        ApiRepository.getUserStats(
            onSuccess = { response ->
                totalMins = response.totalStudyDurationMinutes
                quizzesCompleted = response.totalQuizzesCompleted
                streak = response.streak
                save()
                onComplete()
            },
            onError = { onComplete() }
        )
    }

    fun refreshCommunityQuizzes(
        search: String? = null,
        difficulty: String? = null,
        gradeLevel: String? = null,
        onComplete: (List<Quiz>) -> Unit = {}
    ) {
        ApiRepository.getQuizzes(
            search = search,
            difficulty = difficulty,
            gradeLevel = gradeLevel,
            onSuccess = { response ->
                val quizzes = response.data.map { apiQuiz ->
                    Quiz(
                        id = apiQuiz.id,
                        title = apiQuiz.title,
                        description = apiQuiz.description,
                        creator = apiQuiz.user.name,
                        creatorName = apiQuiz.user.name,
                        questions = emptyList(),
                        questionsCount = apiQuiz.questionsCount,
                        exp = apiQuiz.questionsCount * 20,
                        quizType = when(apiQuiz.type.lowercase().trim()) {
                            "multiple_choice" -> QuizType.MULTIPLE_CHOICE
                            "true_false"      -> QuizType.TRUE_FALSE
                            "identification"  -> QuizType.IDENTIFICATION
                            "mixed"           -> QuizType.MIX
                            else              -> QuizType.MIX
                        },
                        timerMode = when(apiQuiz.timerMode.lowercase().trim()) {
                            "quiz" -> QuizTimerMode.WHOLE_QUIZ
                            "question" -> QuizTimerMode.PER_QUESTION
                            else -> QuizTimerMode.NONE
                        },
                        timerSeconds = when(apiQuiz.timerMode.lowercase().trim()) {
                            "quiz" -> apiQuiz.questionsCount * 30
                            "question" -> 30
                            else -> 0
                        },
                        subject = apiQuiz.subject,
                        gradeLevel = apiQuiz.gradeLevel,
                        difficulty = when(apiQuiz.difficulty.lowercase()) {
                            "easy" -> Difficulty.EASY
                            "hard" -> Difficulty.HARD
                            else -> Difficulty.MEDIUM
                        },
                        code = apiQuiz.quizCode,
                        dateCreated = apiQuiz.createdAt.split("T").firstOrNull() ?: "",
                        shuffleQuestions = apiQuiz.isQuestionShuffled,
                        shuffleOptions = apiQuiz.isChoicesShuffled
                    )
                }
                communityQuizzes = quizzes
                onComplete(quizzes)
            },
            onError = { onComplete(emptyList()) }
        )
    }

    fun loadQuizFullInfo(quizId: Int, onComplete: (Quiz?) -> Unit) {
        ApiRepository.getQuizFullInfo(
            quizId = quizId,
            onSuccess = { response ->
                val apiQuiz = response.data
                val mappedQuiz = Quiz(
                    id = apiQuiz.id,
                    title = apiQuiz.title,
                    description = apiQuiz.description,
                    creator = apiQuiz.user.name,
                    creatorName = apiQuiz.user.name,
                    questionsCount = apiQuiz.questionsCount,
                    exp = apiQuiz.questionsCount * 20,
                    subject = apiQuiz.subject,
                    gradeLevel = apiQuiz.gradeLevel,
                    code = apiQuiz.quizCode,
                    dateCreated = apiQuiz.createdAt.split("T").firstOrNull() ?: "",
                    shuffleQuestions = apiQuiz.isQuestionShuffled,
                    shuffleOptions = apiQuiz.isChoicesShuffled,
                    quizType = when(apiQuiz.type.lowercase().trim()) {
                        "multiple_choice" -> QuizType.MULTIPLE_CHOICE
                        "true_false"      -> QuizType.TRUE_FALSE
                        "identification"  -> QuizType.IDENTIFICATION
                        else              -> QuizType.MIX
                    },
                    timerMode = when(apiQuiz.timerMode.lowercase().trim()) {
                        "quiz" -> QuizTimerMode.WHOLE_QUIZ
                        "question" -> QuizTimerMode.PER_QUESTION
                        else -> QuizTimerMode.NONE
                    },
                    timerSeconds = when(apiQuiz.timerMode.lowercase().trim()) {
                        "quiz" -> apiQuiz.questionsCount * 30
                        "question" -> 30
                        else -> 0
                    },
                    difficulty = when(apiQuiz.difficulty.lowercase()) {
                        "easy" -> Difficulty.EASY
                        "hard" -> Difficulty.HARD
                        else -> Difficulty.MEDIUM
                    },
                    questions = apiQuiz.questions.map { q ->
                        QuizQuestion(
                            id = q.id,
                            q = q.questionText,
                            type = when(q.type.lowercase().trim()) {
                                "multiple_choice" -> QuizType.MULTIPLE_CHOICE
                                "true_false"      -> QuizType.TRUE_FALSE
                                "identification"  -> QuizType.IDENTIFICATION
                                "mixed"           -> QuizType.MIX
                                else              -> QuizType.MIX
                            },
                            identAnswer = q.correctAnswer ?: "",
                            correct = q.choices.indexOfFirst { it.isCorrect }.coerceAtLeast(0),
                            opts = q.choices.map { it.choiceText },
                            optIds = q.choices.map { it.id }
                        )
                    }
                )
                onComplete(mappedQuiz)
            },
            onError = { onComplete(null) }
        )
    }

    fun updateProfile(newName: String, newEmail: String) {
        if (newName.isNotBlank()) name = newName
        if (newEmail.isNotBlank()) email = newEmail
        save()
    }

    fun updateProfileWithApi(response: UpdateProfileResponse) {
        name = response.data.username
        email = response.data.email
        level = response.data.progress.level
        xp = response.data.progress.currentExp
        maxXP = response.data.progress.expToNextLevel
        coins = response.data.coins
        totalXP = response.data.totalExp ?: 0
        save()
    }

    // ══════════════════════════════════════════════════════════════════════
    //  XP & LEVELS
    // ══════════════════════════════════════════════════════════════════════

    fun addXP(amt: Int): Boolean {
        xp += amt; totalXP += amt; var leveled = false
        while (xp >= maxXP) {
            xp -= maxXP; level++; maxXP = xpForNext(level)
            rank = getRank(level); newLevelVal = level; leveled = true
        }
        if (leveled) showLevelUp = true
        checkAchievements(); save(); return leveled
    }

    fun dismissLevelUp() { showLevelUp = false }

    // ══════════════════════════════════════════════════════════════════════
    //  COINS & SHOP
    // ══════════════════════════════════════════════════════════════════════

    fun addCoins(n: Int) { coins += n; save() }

    fun buyShopItem(item: ShopItem): Boolean {
        if (coins < item.cost) return false
        coins -= item.cost
        when (item.effect) {
            ShopEffect.TIME_WARP      -> timeWarpCount      = (timeWarpCount + 1).coerceAtMost(item.maxStack)
            ShopEffect.SECOND_CHANCE  -> secondChanceCount  = (secondChanceCount + 1).coerceAtMost(item.maxStack)
            ShopEffect.HINT           -> hintCount          = (hintCount + 1).coerceAtMost(item.maxStack)
            ShopEffect.XP_BOOST       -> addXP(100)
        }
        save(); return true
    }

    // ══════════════════════════════════════════════════════════════════════
    //  EQUIPMENT
    // ══════════════════════════════════════════════════════════════════════

    fun equip(item: Item) {
        equipment = when (item.slot) {
            EquipSlot.WEAPON -> equipment.copy(weapon = item)
            EquipSlot.ARMOR  -> equipment.copy(armor = item)
            EquipSlot.ACC1   -> equipment.copy(acc1 = item)
            EquipSlot.ACC2   -> equipment.copy(acc2 = item)
            null             -> equipment
        }; save()
    }

    fun unequip(slot: EquipSlot) {
        equipment = when (slot) {
            EquipSlot.WEAPON -> equipment.copy(weapon = null)
            EquipSlot.ARMOR  -> equipment.copy(armor = null)
            EquipSlot.ACC1   -> equipment.copy(acc1 = null)
            EquipSlot.ACC2   -> equipment.copy(acc2 = null)
        }; save()
    }

    // ══════════════════════════════════════════════════════════════════════
    //  QUESTS
    // ══════════════════════════════════════════════════════════════════════

    fun refreshQuests(onComplete: () -> Unit = {}) {
        if (!loggedIn || token.isEmpty()) { onComplete(); return }
        ApiRepository.getQuests(
            onSuccess = { response ->
                val data = response.data
                val dailyList = data?.daily ?: emptyList()
                val weeklyList = data?.weekly ?: emptyList()
                
                val allDaily = dailyList.mapNotNull { mapQuest(it) }
                val allWeekly = weeklyList.mapNotNull { mapQuest(it) }

                quests = allDaily.filter { !it.isBonus }
                dailyBonusQuest = allDaily.find { it.isBonus }

                weeklyQuests = allWeekly.filter { !it.isBonus }
                weeklyBonusQuest = allWeekly.find { it.isBonus }

                save()
                onComplete()
            },
            onError = { onComplete() }
        )
    }

    private fun mapQuest(api: QuestApiData): Quest {
        val title = api.title ?: "Quest"
        val exp = api.rewards?.exp?.toString()?.toDoubleOrNull()?.toInt() ?: 0
        val coins = api.rewards?.coins?.toString()?.toDoubleOrNull()?.toInt() ?: 0
        val isBonus = api.description?.contains("bonus", ignoreCase = true) == true
        
        val progress = api.progress?.toString()?.toDoubleOrNull()?.toInt() ?: 0
        val target = api.target?.toString()?.toDoubleOrNull()?.toInt() ?: 1
        val id = api.id?.toString()?.toDoubleOrNull()?.toInt() ?: 0
        
        return Quest(
            id = id,
            title = title,
            exp = exp,
            done = api.completedAt != null,
            description = api.description ?: "",
            progress = progress,
            target = target.coerceAtLeast(1),
            coins = coins,
            claimed = api.rewards?.claimedAt != null,
            isBonus = isBonus,
            type = api.type ?: ""
        )
    }

    fun claimQuest(id: Int, onComplete: (Int, Int) -> Unit = { _, _ -> }) {
        ApiRepository.claimQuestReward(id,
            onSuccess = { response ->
                val exp = response.data.expGained
                val coins = response.data.coinsGained
                addXP(exp)
                addCoins(coins)
                
                // Update local state
                quests = quests.map { if (it.id == id) it.copy(claimed = true, done = true) else it }
                weeklyQuests = weeklyQuests.map { if (it.id == id) it.copy(claimed = true, done = true) else it }
                if (dailyBonusQuest?.id == id) dailyBonusQuest = dailyBonusQuest?.copy(claimed = true, done = true)
                if (weeklyBonusQuest?.id == id) weeklyBonusQuest = weeklyBonusQuest?.copy(claimed = true, done = true)
                
                save()
                onComplete(exp, coins)
            },
            onError = { /* Handle error */ }
        )
    }

    fun completeQuest(id: Int) {
        // Now tapping a quest tries to claim it
        claimQuest(id)
        checkAchievements(); save()
    }

    fun claimBonus(): Int {
        val bonus = dailyBonusQuest
        if (bonus != null && bonus.done && !bonus.claimed) {
            claimQuest(bonus.id)
            return bonus.exp
        }
        return 0
    }

    fun claimWeeklyBonus(): Int {
        val bonus = weeklyBonusQuest
        if (bonus != null && bonus.done && !bonus.claimed) {
            claimQuest(bonus.id)
            return bonus.exp
        }
        return 0
    }

    fun questsDone()  = quests.count { it.done }
    fun weeklyDone()  = weeklyQuests.count { it.done }

    // ══════════════════════════════════════════════════════════════════════
    //  QUIZZES
    // ══════════════════════════════════════════════════════════════════════

    fun addQuiz(q: Quiz) {
        myQuizzes = myQuizzes + q
        checkAchievements(4)
        save()
    }

    fun editQuiz(q: Quiz)   { myQuizzes = myQuizzes.map { if (it.id == q.id) q else it }; save() }

    fun updateQuizWithApi(quizId: Int, request: CreateQuizRequest, onComplete: (Quiz?) -> Unit) {
        ApiRepository.updateQuiz(
            quizId = quizId,
            request = request,
            onSuccess = { response ->
                val apiQuiz = response.data
                // Map the updated quiz back to local model
                // Note: We might want to preserve local question IDs if they are important, 
                // but the API returns its own IDs.
                val updatedQuiz = Quiz(
                    id = apiQuiz.id,
                    title = apiQuiz.title,
                    description = apiQuiz.description,
                    creator = apiQuiz.user.name,
                    creatorName = apiQuiz.user.name,
                    questions = apiQuiz.questions.map { q ->
                        QuizQuestion(
                            id = q.id,
                            q = q.questionText,
                            type = when(q.type.lowercase().trim()) {
                                "multiple_choice" -> QuizType.MULTIPLE_CHOICE
                                "true_false"      -> QuizType.TRUE_FALSE
                                "identification"  -> QuizType.IDENTIFICATION
                                else              -> QuizType.MIX
                            },
                            identAnswer = q.correctAnswer ?: "",
                            correct = q.choices.indexOfFirst { it.isCorrect }.coerceAtLeast(0),
                            opts = q.choices.map { it.choiceText },
                            optIds = q.choices.map { it.id }
                        )
                    },
                    questionsCount = apiQuiz.questionsCount,
                    exp = apiQuiz.questionsCount * 20,
                    subject = apiQuiz.subject,
                    gradeLevel = apiQuiz.gradeLevel,
                    difficulty = when(apiQuiz.difficulty.lowercase()) {
                        "easy" -> Difficulty.EASY
                        "hard" -> Difficulty.HARD
                        else -> Difficulty.MEDIUM
                    },
                    code = apiQuiz.quizCode,
                    dateCreated = apiQuiz.createdAt.split("T").firstOrNull() ?: "",
                    shuffleQuestions = apiQuiz.isQuestionShuffled,
                    shuffleOptions = apiQuiz.isChoicesShuffled,
                    quizType = when(apiQuiz.type.lowercase().trim()) {
                        "multiple_choice" -> QuizType.MULTIPLE_CHOICE
                        "true_false"      -> QuizType.TRUE_FALSE
                        "identification"  -> QuizType.IDENTIFICATION
                        else              -> QuizType.MIX
                    },
                    timerMode = when(apiQuiz.timerMode.lowercase().trim()) {
                        "quiz" -> QuizTimerMode.WHOLE_QUIZ
                        "question" -> QuizTimerMode.PER_QUESTION
                        else -> QuizTimerMode.NONE
                    },
                    timerSeconds = when(apiQuiz.timerMode.lowercase().trim()) {
                        "quiz" -> apiQuiz.questionsCount * 30
                        "question" -> 30
                        else -> 0
                    }
                )
                myQuizzes = myQuizzes.map { if (it.id == updatedQuiz.id) updatedQuiz else it }
                save()
                onComplete(updatedQuiz)
            },
            onError = { onComplete(null) }
        )
    }
    
    fun deleteQuiz(id: Int, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        ApiRepository.deleteQuiz(
            quizId = id,
            onSuccess = {
                myQuizzes = myQuizzes.filter { it.id != id }
                // Also remove associated attempts from local history if any
                quizHistory = quizHistory.filter { it.quizId != id }
                save()
                onSuccess()
            },
            onError = { onError(it) }
        )
    }

    fun refreshMyQuizzes(onComplete: (List<Quiz>) -> Unit = {}) {
        ApiRepository.getMyQuizzes(
            onSuccess = { response ->
                val quizzes = response.data.map { apiQuiz ->
                    Quiz(
                        id = apiQuiz.id,
                        title = apiQuiz.title,
                        description = apiQuiz.description,
                        creator = apiQuiz.user.name,
                        creatorName = apiQuiz.user.name,
                        questions = emptyList(),
                        questionsCount = apiQuiz.questionsCount,
                        exp = apiQuiz.questionsCount * 20,
                        quizType = when(apiQuiz.type.lowercase().trim()) {
                            "multiple_choice" -> QuizType.MULTIPLE_CHOICE
                            "true_false"      -> QuizType.TRUE_FALSE
                            "identification"  -> QuizType.IDENTIFICATION
                            "mixed"           -> QuizType.MIX
                            else              -> QuizType.MIX
                        },
                        timerMode = when(apiQuiz.timerMode.lowercase().trim()) {
                            "quiz" -> QuizTimerMode.WHOLE_QUIZ
                            "question" -> QuizTimerMode.PER_QUESTION
                            else -> QuizTimerMode.NONE
                        },
                        timerSeconds = when(apiQuiz.timerMode.lowercase().trim()) {
                            "quiz" -> apiQuiz.questionsCount * 30
                            "question" -> 30
                            else -> 0
                        },
                        subject = apiQuiz.subject,
                        gradeLevel = apiQuiz.gradeLevel,
                        difficulty = when(apiQuiz.difficulty.lowercase()) {
                            "easy" -> Difficulty.EASY
                            "hard" -> Difficulty.HARD
                            else -> Difficulty.MEDIUM
                        },
                        code = apiQuiz.quizCode,
                        dateCreated = apiQuiz.createdAt.split("T").firstOrNull() ?: "",
                        shuffleQuestions = apiQuiz.isQuestionShuffled,
                        shuffleOptions = apiQuiz.isChoicesShuffled
                    )
                }
                myQuizzes = quizzes
                save()
                onComplete(quizzes)
            },
            onError = { onComplete(emptyList()) }
        )
    }

    fun findByCode(code: String): Quiz? =
        (myQuizzes + communityQuizzes).firstOrNull {
            it.code.equals(code.trim(), ignoreCase = true)
        }

    fun recordQuizResult(quiz: Quiz, score: Int, answers: List<AnswerRecord>) {
        val entry = QuizHistoryEntry(
            quiz.id, quiz.title, quiz.code, todayString(), score, quiz.questions.size, answers
        )
        quizHistory = (listOf(entry) + quizHistory).take(50)
        quizzesCompleted++
        // Removed local score-based coin reward. Relying on API response instead.
        refreshQuests()
        checkAchievements(); save()
    }


    // ══════════════════════════════════════════════════════════════════════
    //  STUDY SESSIONS
    // ══════════════════════════════════════════════════════════════════════

    fun addStudySession(durationSeconds: Int, onComplete: (Int, Int) -> Unit = { _, _ -> }) {
        ApiRepository.createStudySession(
            durationSeconds = durationSeconds,
            onSuccess = { response ->
                val mins = durationSeconds / 60
                val earnedExp = response.data.rewards.exp
                val earnedCoins = response.data.rewards.coins
                
                totalMins += mins
                streak++
                streakAtRisk = false
                
                addXP(earnedExp)
                addCoins(earnedCoins)
                
                sessionHistory = (listOf(SessionEntry(isoDate(), mins, earnedExp)) + sessionHistory).take(50)
                refreshQuests()
                checkAchievements()
                save()
                onComplete(earnedExp, earnedCoins)
            },
            onError = {
                // Optional: handle error
            }
        )
    }

    fun refreshSessionHistory(onComplete: () -> Unit = {}) {
        ApiRepository.getStudySessions(
            onSuccess = { response ->
                // Sort by the full timestamp string descending before mapping
                sessionHistory = response.data
                    .sortedByDescending { it.sessionAt }
                    .map { apiSession ->
                        val mins = apiSession.duration / 60
                        val earnedExp = mins 
                        
                        // Use yyyy-MM-dd for sorting/filtering consistency
                        val datePart = apiSession.sessionAt.split("T").firstOrNull() ?: isoDate()
                        SessionEntry(datePart, mins, earnedExp)
                    }
                save()
                onComplete()
            },
            onError = { onComplete() }
        )
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ACHIEVEMENTS
    // ══════════════════════════════════════════════════════════════════════

    fun refreshAchievements(onComplete: () -> Unit = {}) {
        if (!loggedIn || token.isEmpty()) { onComplete(); return }
        ApiRepository.getAchievements(
            onSuccess = { response ->
                achievements = response.data.map { mapAchievement(it) }
                save()
                onComplete()
            },
            onError = { onComplete() }
        )
    }

    private fun mapAchievement(api: AchievementApiData): Achievement {
        val exp = api.rewards?.exp?.toString()?.toDoubleOrNull()?.toInt() ?: 0
        val coins = api.rewards?.coins?.toString()?.toDoubleOrNull()?.toInt() ?: 0
        val progress = api.progress?.toString()?.toDoubleOrNull()?.toInt() ?: 0
        val target = api.target?.toString()?.toDoubleOrNull()?.toInt() ?: 1
        val id = api.id?.toString()?.toDoubleOrNull()?.toInt() ?: 0
        
        return Achievement(
            id = id,
            title = api.name ?: "Achievement",
            description = api.description ?: "",
            expReward = exp,
            coinReward = coins,
            progress = progress,
            target = target,
            unlocked = api.completedAt != null,
            claimed = api.claimedAt != null
        )
    }

    fun claimAchievement(id: Int, onComplete: (Int, Int) -> Unit = { _, _ -> }) {
        ApiRepository.claimAchievementReward(id,
            onSuccess = { response ->
                val exp = response.data.expGained
                val coins = response.data.coinsGained
                addXP(exp)
                addCoins(coins)

                // Update local state
                achievements = achievements.map { 
                    if (it.id == id) it.copy(claimed = true, unlocked = true) else it 
                }
                save()
                onComplete(exp, coins)
            },
            onError = { /* Handle error */ }
        )
    }

    private fun checkAchievements(forceId: Int? = null) {
        // Now mostly handled by backend, but we can refresh to be sure
        refreshAchievements()
    }

    fun unlockSpeedDemon()    {
        achievements = achievements.map { if (it.id == 10) it.copy(unlocked = true) else it }; save()
    }

    fun unlockPopularCreator() {
        achievements = achievements.map { if (it.id == 11) it.copy(unlocked = true) else it }
        addCoins(50); save()
    }

    fun getActivePerks(): List<Perk> = emptyList()

    // ══════════════════════════════════════════════════════════════════════
    //  PERSIST
    // ══════════════════════════════════════════════════════════════════════

    fun save() {
        if (!::prefs.isInitialized) return
        prefs.edit().apply {
            putString("name",  name);  putString("email", email)
            putString("token", token)
            putString("grade", grade?.name); putBoolean("loggedIn", loggedIn)
            putInt("level", level); putInt("xp", xp); putInt("totalXP", totalXP)
            putInt("streak", streak); putInt("totalMins", totalMins)
            putInt("quizzesCompleted", quizzesCompleted)
            putBoolean("streakAtRisk", streakAtRisk)
            putInt("coins", coins)
            putInt("twc",   timeWarpCount);    putInt("scc", secondChanceCount)
            putInt("hintc", hintCount)
            putString("quests",       gson.toJson(quests))
            putString("wquests",      gson.toJson(weeklyQuests))
            putString("dbonus",       gson.toJson(dailyBonusQuest))
            putString("wbonus",       gson.toJson(weeklyBonusQuest))
            putString("myQuizzes",    gson.toJson(myQuizzes))
            putString("achievements", gson.toJson(achievements))
            putString("sessions",     gson.toJson(sessionHistory))
            putString("quizHistory",  gson.toJson(quizHistory))
        }.apply()
    }

    private fun load() {
        if (!::prefs.isInitialized) return
        name     = prefs.getString("name",  "") ?: ""
        email    = prefs.getString("email", "") ?: ""
        token    = prefs.getString("token", "") ?: ""
        if (token.isNotEmpty()) {
            ApiRepository.setToken(token)
            // Optional: Auto-refresh data on load
            // refreshUserData() 
        }
        grade    = prefs.getString("grade", null)
            ?.let { try { GradeLevel.valueOf(it) } catch (_: Exception) { null } }
        loggedIn = prefs.getBoolean("loggedIn", false)
        level    = prefs.getInt("level", 1)
        xp       = prefs.getInt("xp", 0)
        totalXP  = prefs.getInt("totalXP", 0)
        streak   = prefs.getInt("streak", 0)
        totalMins        = prefs.getInt("totalMins", 0)
        quizzesCompleted = prefs.getInt("quizzesCompleted", 0)
        streakAtRisk     = prefs.getBoolean("streakAtRisk", false)
        coins            = prefs.getInt("coins", 15000)
        timeWarpCount      = prefs.getInt("twc",   0)
        secondChanceCount  = prefs.getInt("scc",   0)
        hintCount          = prefs.getInt("hintc", 0)
        maxXP = xpForNext(level); rank = getRank(level)

        val questType   = object : TypeToken<List<Quest>>()            {}.type
        val singleQuestType = object : TypeToken<Quest>()              {}.type
        val quizType    = object : TypeToken<List<Quiz>>()             {}.type
        val achType     = object : TypeToken<List<Achievement>>()      {}.type
        val sessionType = object : TypeToken<List<SessionEntry>>()     {}.type
        val historyType = object : TypeToken<List<QuizHistoryEntry>>() {}.type

        prefs.getString("quests",       null)?.let { s -> try { quests         = gson.fromJson(s, questType)   } catch (_: Exception) {} }
        prefs.getString("wquests",      null)?.let { s -> try { weeklyQuests   = gson.fromJson(s, questType)   } catch (_: Exception) {} }
        prefs.getString("dbonus",       null)?.let { s -> try { dailyBonusQuest = gson.fromJson(s, singleQuestType) } catch (_: Exception) {} }
        prefs.getString("wbonus",       null)?.let { s -> try { weeklyBonusQuest = gson.fromJson(s, singleQuestType) } catch (_: Exception) {} }
        prefs.getString("myQuizzes",    null)?.let { s -> try { myQuizzes      = gson.fromJson(s, quizType)    } catch (_: Exception) {} }
        prefs.getString("achievements", null)?.let { s -> try { achievements   = gson.fromJson(s, achType)     } catch (_: Exception) {} }
        prefs.getString("sessions",     null)?.let { s -> try { sessionHistory = gson.fromJson(s, sessionType) } catch (_: Exception) {} }
        prefs.getString("quizHistory",  null)?.let { s -> try { quizHistory    = gson.fromJson(s, historyType) } catch (_: Exception) {} }
    }
}
