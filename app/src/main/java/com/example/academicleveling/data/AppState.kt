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
        prefs = context.getSharedPreferences("academic_leveling_v7", Context.MODE_PRIVATE)
        load()
    }

    // ── Auth ──────────────────────────────────────────────────────────────
    var name     by mutableStateOf("")
    var email    by mutableStateOf("")
    var grade    by mutableStateOf<GradeLevel?>(null)
    var loggedIn by mutableStateOf(false)

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
    var inventory: List<Item>     by mutableStateOf(DEFAULT_INVENTORY)
    var equipment: Equipment      by mutableStateOf(DEFAULT_EQUIPMENT)

    // ── Achievements / Sessions ───────────────────────────────────────────
    var achievements:   List<Achievement>      by mutableStateOf(ALL_ACHIEVEMENTS)
    var sessionHistory: List<SessionEntry>     by mutableStateOf(emptyList())
    var quizHistory:    List<QuizHistoryEntry> by mutableStateOf(emptyList())

    // ── Quests ────────────────────────────────────────────────────────────
    var quests: List<Quest> by mutableStateOf(listOf(
        Quest(1, "STUDY FOR 30 MINUTES",     10),
        Quest(2, "ANSWER A QUIZ",            15),
        Quest(3, "COMPLETE 1 HOMEWORK TASK", 15),
        Quest(4, "LOG A STUDY SESSION",      10)
    ))
    var weeklyQuests: List<Quest> by mutableStateOf(listOf(
        Quest(101, "20 MINUTES READING FOR 4 DAYS", 60),
        Quest(102, "COMPLETE 5 QUIZZES",            100),
        Quest(103, "COMPLETE 3 DAILY QUESTS",       200)
    ))

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
        name = n; email = e; grade = g; loggedIn = true; save()
    }

    fun quickLogin(n: String = "", e: String = "") {
        if (n.isNotBlank()) name = n
        if (name.isBlank()) name = "Player"
        if (e.isNotBlank()) email = e
        loggedIn = true; save()
    }

    fun logout() { loggedIn = false; name = ""; save() }

    fun updateProfile(newName: String, newEmail: String) {
        if (newName.isNotBlank()) name = newName
        if (newEmail.isNotBlank()) email = newEmail
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
            ShopEffect.STREAK_BANDAID -> streakBandaidCount = (streakBandaidCount + 1).coerceAtMost(item.maxStack)
            ShopEffect.XP_BOOST       -> addXP(100)
        }
        save(); return true
    }

    fun useStreakBandaid(): Boolean {
        if (streakBandaidCount <= 0) return false
        streakBandaidCount--; streakAtRisk = false
        if (streak == 0) streak = 1
        save(); return true
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
        }; save()
    }

    fun unequip(slot: EquipSlot) {
        equipment = when (slot) {
            EquipSlot.WEAPON -> equipment.copy(weapon = null)
            EquipSlot.ARMOR  -> equipment.copy(armor  = null)
            EquipSlot.ACC1   -> equipment.copy(acc1   = null)
            EquipSlot.ACC2   -> equipment.copy(acc2   = null)
        }; save()
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
        checkAchievements(); save()
    }

    fun claimBonus(): Int       = if (quests.all { it.done }) { addXP(50); addCoins(20); 50 } else 0
    fun claimWeeklyBonus(): Int = if (weeklyQuests.all { it.done }) { addXP(100); addCoins(50); 100 } else 0
    fun questsDone()  = quests.count { it.done }
    fun weeklyDone()  = weeklyQuests.count { it.done }

    // ══════════════════════════════════════════════════════════════════════
    //  QUIZZES
    // ══════════════════════════════════════════════════════════════════════

    fun addQuiz(q: Quiz) {
        myQuizzes = myQuizzes + q
        addXP(30)
        checkAchievements(4)
        save()
    }

    fun editQuiz(q: Quiz)   { myQuizzes = myQuizzes.map { if (it.id == q.id) q else it }; save() }
    fun deleteQuiz(id: Int) { myQuizzes = myQuizzes.filter { it.id != id }; save() }

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
        addCoins(score * 5)
        completeQuest(2)
        val isMine = myQuizzes.any { it.id == quiz.id }
        if (!isMine) ApiRepository.notifyQuizComplete(quiz.id)
        checkAchievements(); save()
    }

    // ══════════════════════════════════════════════════════════════════════
    //  STUDY SESSIONS
    // ══════════════════════════════════════════════════════════════════════

    fun addStudySession(mins: Int) {
        totalMins += mins; streak++; streakAtRisk = false
        val earned = mins * 2; addXP(earned); addCoins(mins)
        sessionHistory = (listOf(SessionEntry(shortDate(), mins, earned)) + sessionHistory).take(10)
        completeQuest(1); completeQuest(4); checkAchievements(); save()
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
        if (earned > 0) save()
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
            putString("grade", grade?.name); putBoolean("loggedIn", loggedIn)
            putInt("level", level); putInt("xp", xp); putInt("totalXP", totalXP)
            putInt("streak", streak); putInt("totalMins", totalMins)
            putInt("quizzesCompleted", quizzesCompleted)
            putBoolean("streakAtRisk", streakAtRisk)
            putInt("coins", coins)
            putInt("twc",   timeWarpCount);    putInt("scc", secondChanceCount)
            putInt("hintc", hintCount);        putInt("sbc", streakBandaidCount)
            putString("quests",       gson.toJson(quests))
            putString("wquests",      gson.toJson(weeklyQuests))
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
        streakBandaidCount = prefs.getInt("sbc",   0)
        maxXP = xpForNext(level); rank = getRank(level)

        val questType   = object : TypeToken<List<Quest>>()            {}.type
        val quizType    = object : TypeToken<List<Quiz>>()             {}.type
        val achType     = object : TypeToken<List<Achievement>>()      {}.type
        val sessionType = object : TypeToken<List<SessionEntry>>()     {}.type
        val historyType = object : TypeToken<List<QuizHistoryEntry>>() {}.type

        prefs.getString("quests",       null)?.let { s -> try { quests         = gson.fromJson(s, questType)   } catch (_: Exception) {} }
        prefs.getString("wquests",      null)?.let { s -> try { weeklyQuests   = gson.fromJson(s, questType)   } catch (_: Exception) {} }
        prefs.getString("myQuizzes",    null)?.let { s -> try { myQuizzes      = gson.fromJson(s, quizType)    } catch (_: Exception) {} }
        prefs.getString("achievements", null)?.let { s -> try { achievements   = gson.fromJson(s, achType)     } catch (_: Exception) {} }
        prefs.getString("sessions",     null)?.let { s -> try { sessionHistory = gson.fromJson(s, sessionType) } catch (_: Exception) {} }
        prefs.getString("quizHistory",  null)?.let { s -> try { quizHistory    = gson.fromJson(s, historyType) } catch (_: Exception) {} }
    }
}