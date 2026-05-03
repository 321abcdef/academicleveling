package com.example.academicleveling.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object AppState {

    private lateinit var appContext: Context
    private lateinit var prefs: SharedPreferences        // global prefs (token, last email)
    private lateinit var accountPrefs: SharedPreferences // per-account prefs
    private val gson = Gson()

    fun init(context: Context) {
        appContext = context.applicationContext
        prefs = context.getSharedPreferences("academic_leveling_global", Context.MODE_PRIVATE)
        val lastEmail = prefs.getString("lastEmail", "") ?: ""
        val lastToken = prefs.getString("token",     "") ?: ""
        if (lastEmail.isNotEmpty() && lastToken.isNotEmpty()) {
            accountPrefs = accountPrefsFor(lastEmail)
            token = lastToken
            ApiRepository.setToken(token)
            loadAccount()
            if (loggedIn) refreshUserData()
        } else {
            accountPrefs = context.getSharedPreferences("academic_leveling_guest", Context.MODE_PRIVATE)
        }
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
    var coins              by mutableStateOf(0)
    var timeWarpCount      by mutableStateOf(0)
    var secondChanceCount  by mutableStateOf(0)
    var hintCount          by mutableStateOf(0)
    var streakBandaidCount by mutableStateOf(0)

    // ── Tracking ──────────────────────────────────────────────────────────
    var streak           by mutableStateOf(0)
    var totalMins        by mutableStateOf(0)
    var quizzesCompleted by mutableStateOf(0)
    var streakAtRisk     by mutableStateOf(false)

    // ── UI helpers ────────────────────────────────────────────────────────
    var showLevelUp by mutableStateOf(false)
    var newLevelVal by mutableStateOf(1)

    // ── Inventory / Equipment ─────────────────────────────────────────────
    var inventory: List<Item>  by mutableStateOf(DEFAULT_INVENTORY)
    var equipment: Equipment   by mutableStateOf(DEFAULT_EQUIPMENT)

    // ── Achievements / Sessions ───────────────────────────────────────────
    var achievements:   List<Achievement>      by mutableStateOf(ALL_ACHIEVEMENTS)
    var sessionHistory: List<SessionEntry>     by mutableStateOf(emptyList())
    var quizHistory:    List<QuizHistoryEntry> by mutableStateOf(emptyList())

    // ── Quests ────────────────────────────────────────────────────────────
    val defaultDailyQuests get() = listOf(
        Quest(1, "STUDY FOR 30 MINUTES",     10),
        Quest(2, "ANSWER A QUIZ",            15),
        Quest(3, "COMPLETE 1 HOMEWORK TASK", 15),
        Quest(4, "LOG A STUDY SESSION",      10)
    )
    val defaultWeeklyQuests get() = listOf(
        Quest(101, "20 MINUTES READING FOR 4 DAYS", 60),
        Quest(102, "COMPLETE 5 QUIZZES",            100),
        Quest(103, "COMPLETE 3 DAILY QUESTS",       200)
    )
    var quests:       List<Quest> by mutableStateOf(defaultDailyQuests)
    var weeklyQuests: List<Quest> by mutableStateOf(defaultWeeklyQuests)

    // ── Quizzes ───────────────────────────────────────────────────────────
    var myQuizzes:        List<Quiz> by mutableStateOf(emptyList())
    var communityQuizzes: List<Quiz> by mutableStateOf(buildCommunityQuizzes())

    val SHOP_ITEMS: List<ShopItem> = listOf(
        ShopItem(1, "Time Warp",       "Adds +30s to your quiz timer.",               100, ShopEffect.TIME_WARP),
        ShopItem(2, "50/50",           "Eliminates 2 wrong options on MC questions.",  200, ShopEffect.SECOND_CHANCE),
        ShopItem(3, "Hint",            "Reveals the correct answer once.",             500, ShopEffect.HINT),
        ShopItem(4, "Streak Band-aid", "Repairs a broken daily streak.",               400, ShopEffect.STREAK_BANDAID),
        ShopItem(5, "XP Boost",        "Instant +100 XP to your level.",               300, ShopEffect.XP_BOOST)
    )

    // ══════════════════════════════════════════════════════════════════════
    //  AUTH
    // ══════════════════════════════════════════════════════════════════════

    fun login(n: String, e: String, g: GradeLevel) {
        name = n; email = e; grade = g; loggedIn = true; saveAccount()
    }

    fun quickLogin(n: String = "", e: String = "") {
        if (n.isNotBlank()) name = n
        if (name.isBlank()) name = "Player"
        if (e.isNotBlank()) email = e
        loggedIn = true; saveAccount()
    }

    fun logout() {
        saveAccount()  // save progress before clearing
        prefs.edit().remove("lastEmail").remove("token").apply()
        resetState()
        ApiRepository.logout()
    }

    fun loginWithApi(response: LoginResponse) {
        val accountEmail = response.data.email
        token = response.token
        ApiRepository.setToken(token)

        // Switch to this account's own prefs and load its saved progress
        accountPrefs = accountPrefsFor(accountEmail)
        loadAccount()

        // Always trust server for identity
        name  = response.data.username
        email = accountEmail
        loggedIn = true

        prefs.edit()
            .putString("token",     token)
            .putString("lastEmail", accountEmail)
            .apply()

        saveAccount()
    }

    fun registerWithApi(response: RegisterResponse) {
        val accountEmail = response.data.email
        token = response.token
        ApiRepository.setToken(token)

        // New account — fresh prefs, start from zero
        accountPrefs = accountPrefsFor(accountEmail)
        resetState()
        name  = response.data.username
        email = accountEmail
        loggedIn = true

        prefs.edit()
            .putString("token",     token)
            .putString("lastEmail", accountEmail)
            .apply()

        saveAccount()
    }

    fun refreshUserData(onComplete: () -> Unit = {}) {
        if (!loggedIn || token.isEmpty()) { onComplete(); return }
        ApiRepository.getUserInfo(
            onSuccess = { response ->
                // Only sync identity — progress lives locally per account
                name  = response.data.username
                email = response.data.email
                saveAccount()
                onComplete()
            },
            onError = { onComplete() }
        )
    }

    fun updateProfile(newName: String, newEmail: String) {
        if (newName.isNotBlank())  name  = newName
        if (newEmail.isNotBlank()) email = newEmail
        saveAccount()
    }

    fun updateProfileWithApi(response: UpdateProfileResponse) {
        // Only update identity fields, never local progress
        name  = response.data.username
        email = response.data.email
        saveAccount()
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
        checkAchievements(); saveAccount(); return leveled
    }

    fun dismissLevelUp() { showLevelUp = false }

    // ══════════════════════════════════════════════════════════════════════
    //  COINS & SHOP
    // ══════════════════════════════════════════════════════════════════════

    fun addCoins(n: Int) { coins += n; saveAccount() }

    fun buyShopItem(item: ShopItem): Boolean {
        if (coins < item.cost) return false
        coins -= item.cost
        when (item.effect) {
            ShopEffect.TIME_WARP      -> timeWarpCount      = (timeWarpCount + 1).coerceAtMost(item.maxStack)
            ShopEffect.SECOND_CHANCE  -> secondChanceCount  = (secondChanceCount + 1).coerceAtMost(item.maxStack)
            ShopEffect.HINT           -> hintCount          = (hintCount + 1).coerceAtMost(item.maxStack)
            ShopEffect.STREAK_BANDAID -> streakBandaidCount = (streakBandaidCount + 1).coerceAtMost(item.maxStack)
            ShopEffect.XP_BOOST       -> addXP(100)
        }
        saveAccount(); return true
    }

    fun useStreakBandaid(): Boolean {
        if (streakBandaidCount <= 0) return false
        streakBandaidCount--; streakAtRisk = false
        if (streak == 0) streak = 1
        saveAccount(); return true
    }

    // ══════════════════════════════════════════════════════════════════════
    //  EQUIPMENT
    // ══════════════════════════════════════════════════════════════════════

    fun equip(item: Item) {
        equipment = when (item.slot) {
            EquipSlot.WEAPON -> equipment.copy(weapon = item)
            EquipSlot.ARMOR  -> equipment.copy(armor  = item)
            EquipSlot.ACC1   -> equipment.copy(acc1   = item)
            EquipSlot.ACC2   -> equipment.copy(acc2   = item)
            null             -> equipment
        }; saveAccount()
    }

    fun unequip(slot: EquipSlot) {
        equipment = when (slot) {
            EquipSlot.WEAPON -> equipment.copy(weapon = null)
            EquipSlot.ARMOR  -> equipment.copy(armor  = null)
            EquipSlot.ACC1   -> equipment.copy(acc1   = null)
            EquipSlot.ACC2   -> equipment.copy(acc2   = null)
        }; saveAccount()
    }

    // ══════════════════════════════════════════════════════════════════════
    //  QUESTS
    // ══════════════════════════════════════════════════════════════════════

    fun completeQuest(id: Int) {
        quests = quests.map { q ->
            if (q.id == id && !q.done) { addXP(q.exp); q.copy(done = true) } else q
        }
        weeklyQuests = weeklyQuests.map { q ->
            if (q.id == id && !q.done) { addXP(q.exp); q.copy(done = true) } else q
        }
        checkAchievements(); saveAccount()
    }

    fun claimBonus(): Int {
        if (!quests.all { it.done }) return 0
        addXP(50); addCoins(20); saveAccount(); return 50
    }

    fun claimWeeklyBonus(): Int {
        if (!weeklyQuests.all { it.done }) return 0
        addXP(100); addCoins(50); saveAccount(); return 100
    }

    fun isDailyBonusClaimed():  Boolean = false
    fun isWeeklyBonusClaimed(): Boolean = false
    fun questsDone() = quests.count { it.done }
    fun weeklyDone() = weeklyQuests.count { it.done }

    // ══════════════════════════════════════════════════════════════════════
    //  QUIZZES
    // ══════════════════════════════════════════════════════════════════════

    fun addQuiz(q: Quiz) {
        myQuizzes = myQuizzes + q; addXP(30); checkAchievements(4); saveAccount()
    }

    fun editQuiz(q: Quiz)   { myQuizzes = myQuizzes.map { if (it.id == q.id) q else it }; saveAccount() }
    fun deleteQuiz(id: Int) { myQuizzes = myQuizzes.filter { it.id != id }; saveAccount() }

    fun findByCode(code: String): Quiz? =
        (myQuizzes + communityQuizzes).firstOrNull { it.code.equals(code.trim(), ignoreCase = true) }

    fun recordQuizResult(quiz: Quiz, score: Int, answers: List<AnswerRecord>) {
        val entry = QuizHistoryEntry(quiz.id, quiz.title, quiz.code, todayString(), score, quiz.questions.size, answers)
        quizHistory = (listOf(entry) + quizHistory).take(50)
        quizzesCompleted++
        // NOTE: XP and coins are NOT applied here — they come from the API response
        // in PlayQuizScreen via AppState.addXP() and AppState.addCoins() directly.
        completeQuest(2)
        val isMine = myQuizzes.any { it.id == quiz.id }
        if (!isMine) ApiRepository.notifyQuizComplete(quiz.id)
        checkAchievements(); saveAccount()
    }

    // ══════════════════════════════════════════════════════════════════════
    //  STUDY SESSIONS
    // ══════════════════════════════════════════════════════════════════════

    fun addStudySession(mins: Int, expFromApi: Int = 0, coinsFromApi: Int = 0) {
        totalMins += mins; streak++; streakAtRisk = false
        // Apply rewards from API response (not computed locally)
        if (expFromApi > 0)   addXP(expFromApi)
        if (coinsFromApi > 0) addCoins(coinsFromApi)
        sessionHistory = (listOf(SessionEntry(shortDate(), mins, expFromApi)) + sessionHistory).take(10)
        completeQuest(1); completeQuest(4); checkAchievements(); saveAccount()
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ACHIEVEMENTS
    // ══════════════════════════════════════════════════════════════════════

    fun claimAchievement(id: Int): Int {
        var earned = 0
        achievements = achievements.map { a ->
            if (a.id == id && a.unlocked && !a.claimed) {
                earned = a.coinReward; addCoins(a.coinReward); a.copy(claimed = true)
            } else a
        }
        if (earned > 0) saveAccount()
        return earned
    }

    private fun checkAchievements(forceId: Int? = null) {
        achievements = achievements.map { a ->
            if (a.unlocked) return@map a
            val unlock = forceId == a.id || when (a.id) {
                1  -> totalMins > 0
                2  -> quizzesCompleted > 0
                3  -> quests.all { q -> q.done }
                4  -> myQuizzes.isNotEmpty()
                5  -> level >= 5
                6  -> level >= 10
                7  -> totalXP >= 1000
                8  -> totalMins >= 60
                9  -> quizzesCompleted >= 5
                else -> false
            }
            if (unlock) a.copy(unlocked = true) else a
        }
    }

    fun unlockSpeedDemon() {
        achievements = achievements.map { if (it.id == 10) it.copy(unlocked = true) else it }; saveAccount()
    }

    fun unlockPopularCreator() {
        achievements = achievements.map { if (it.id == 11) it.copy(unlocked = true) else it }
        addCoins(50); saveAccount()
    }

    fun getActivePerks(): List<Perk> = emptyList()

    // ══════════════════════════════════════════════════════════════════════
    //  DATE HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private fun todayDateKey(): String =
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

    private fun currentWeekKey(): String {
        val cal = java.util.Calendar.getInstance()
        cal.firstDayOfWeek = java.util.Calendar.MONDAY
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.time)
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════════════════

    /** Returns a SharedPreferences file unique to one account email. */
    private fun accountPrefsFor(accountEmail: String): SharedPreferences {
        val key = accountEmail.replace(Regex("[^a-zA-Z0-9_]"), "_")
        return appContext.getSharedPreferences("account_$key", Context.MODE_PRIVATE)
    }

    /** Resets all in-memory progress to fresh-account defaults. */
    private fun resetState() {
        name = ""; email = ""; token = ""; grade = null; loggedIn = false
        level = 1; xp = 0; totalXP = 0; maxXP = xpForNext(1); rank = Rank.E
        coins = 0; streak = 0; totalMins = 0; quizzesCompleted = 0
        streakAtRisk = false; showLevelUp = false; newLevelVal = 1
        timeWarpCount = 0; secondChanceCount = 0; hintCount = 0; streakBandaidCount = 0
        inventory    = DEFAULT_INVENTORY
        equipment    = DEFAULT_EQUIPMENT
        achievements = ALL_ACHIEVEMENTS
        sessionHistory = emptyList(); quizHistory = emptyList()
        quests       = defaultDailyQuests
        weeklyQuests = defaultWeeklyQuests
        myQuizzes    = emptyList()
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PERSIST  (per-account)
    // ══════════════════════════════════════════════════════════════════════

    fun saveAccount() {
        if (!::accountPrefs.isInitialized) return
        accountPrefs.edit().apply {
            putString("name",  name);  putString("email", email)
            putString("grade", grade?.name); putBoolean("loggedIn", loggedIn)
            putInt("level",   level);  putInt("xp",      xp);  putInt("totalXP", totalXP)
            putInt("streak",  streak); putInt("totalMins", totalMins)
            putInt("quizzesCompleted", quizzesCompleted)
            putBoolean("streakAtRisk", streakAtRisk)
            putInt("coins", coins)
            putInt("twc",   timeWarpCount);    putInt("scc",   secondChanceCount)
            putInt("hintc", hintCount);        putInt("sbc",   streakBandaidCount)
            putString("quests",       gson.toJson(quests))
            putString("wquests",      gson.toJson(weeklyQuests))
            putString("myQuizzes",    gson.toJson(myQuizzes))
            putString("achievements", gson.toJson(achievements))
            putString("sessions",     gson.toJson(sessionHistory))
            putString("quizHistory",  gson.toJson(quizHistory))
        }.apply()
    }

    // kept for compatibility — delegates to saveAccount
    fun save() = saveAccount()

    private fun loadAccount() {
        if (!::accountPrefs.isInitialized) return
        name     = accountPrefs.getString("name",  "") ?: ""
        email    = accountPrefs.getString("email", "") ?: ""
        grade    = accountPrefs.getString("grade", null)
            ?.let { try { GradeLevel.valueOf(it) } catch (_: Exception) { null } }
        loggedIn = accountPrefs.getBoolean("loggedIn", false)
        level    = accountPrefs.getInt("level",   1)
        xp       = accountPrefs.getInt("xp",      0)
        totalXP  = accountPrefs.getInt("totalXP", 0)
        streak   = accountPrefs.getInt("streak",  0)
        totalMins        = accountPrefs.getInt("totalMins",        0)
        quizzesCompleted = accountPrefs.getInt("quizzesCompleted", 0)
        streakAtRisk     = accountPrefs.getBoolean("streakAtRisk", false)
        coins            = accountPrefs.getInt("coins", 0)
        timeWarpCount      = accountPrefs.getInt("twc",   0)
        secondChanceCount  = accountPrefs.getInt("scc",   0)
        hintCount          = accountPrefs.getInt("hintc", 0)
        streakBandaidCount = accountPrefs.getInt("sbc",   0)
        maxXP = xpForNext(level); rank = getRank(level)

        val questType   = object : TypeToken<List<Quest>>()            {}.type
        val quizType    = object : TypeToken<List<Quiz>>()             {}.type
        val achType     = object : TypeToken<List<Achievement>>()      {}.type
        val sessionType = object : TypeToken<List<SessionEntry>>()     {}.type
        val historyType = object : TypeToken<List<QuizHistoryEntry>>() {}.type

        accountPrefs.getString("quests",       null)?.let { s -> try { quests         = gson.fromJson(s, questType)   } catch (_: Exception) {} }
        accountPrefs.getString("wquests",      null)?.let { s -> try { weeklyQuests   = gson.fromJson(s, questType)   } catch (_: Exception) {} }
        accountPrefs.getString("myQuizzes",    null)?.let { s -> try { myQuizzes      = gson.fromJson(s, quizType)    } catch (_: Exception) {} }
        accountPrefs.getString("achievements", null)?.let { s -> try { achievements   = gson.fromJson(s, achType)     } catch (_: Exception) {} }
        accountPrefs.getString("sessions",     null)?.let { s -> try { sessionHistory = gson.fromJson(s, sessionType) } catch (_: Exception) {} }
        accountPrefs.getString("quizHistory",  null)?.let { s -> try { quizHistory    = gson.fromJson(s, historyType) } catch (_: Exception) {} }

        // Reset daily quests if it's a new day
        val today = todayDateKey()
        val lastDailyReset = accountPrefs.getString("lastDailyReset", "") ?: ""
        if (lastDailyReset != today) {
            quests = quests.map { it.copy(done = false) }
            accountPrefs.edit().putString("lastDailyReset", today).apply()
        }

        // Reset weekly quests if it's a new week
        val thisWeek = currentWeekKey()
        val lastWeeklyReset = accountPrefs.getString("lastWeeklyReset", "") ?: ""
        if (lastWeeklyReset != thisWeek) {
            weeklyQuests = weeklyQuests.map { it.copy(done = false) }
            accountPrefs.edit().putString("lastWeeklyReset", thisWeek).apply()
        }
    }
}