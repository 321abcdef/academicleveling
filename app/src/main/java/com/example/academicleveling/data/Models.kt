package com.example.academicleveling.data

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
//  ENUMS
// ─────────────────────────────────────────────────────────────────────────────

enum class GradeLevel(val display: String) {
    G1("Grade 1"),   G2("Grade 2"),   G3("Grade 3"),   G4("Grade 4"),
    G5("Grade 5"),   G6("Grade 6"),   G7("Grade 7"),   G8("Grade 8"),
    G9("Grade 9"),   G10("Grade 10"), G11("Grade 11"), G12("Grade 12"),
    C1("College 1"), C2("College 2"), C3("College 3"), C4("College 4")
}

enum class Rank { E, D, C, B, A, S }
enum class ItemRarity { COMMON, RARE, EPIC, LEGENDARY, MYTHIC }
enum class EquipSlot { WEAPON, ARMOR, ACC1, ACC2 }
enum class Difficulty { EASY, MEDIUM, HARD }
enum class QuizTimerMode { NONE, WHOLE_QUIZ, PER_QUESTION }
enum class QuizType { MULTIPLE_CHOICE, TRUE_FALSE, IDENTIFICATION, MIX }
enum class ShopEffect { TIME_WARP, SECOND_CHANCE, HINT, STREAK_BANDAID, XP_BOOST }

// ─────────────────────────────────────────────────────────────────────────────
//  CORE MODELS
// ─────────────────────────────────────────────────────────────────────────────

data class Stats(val intStat: Int = 10, val wis: Int = 10, val foc: Int = 10, val sta: Int = 10)
data class Perk(val label: String, val description: String)

data class Quest(val id: Int, val title: String, val exp: Int, var done: Boolean = false)

data class QuizQuestion(
    val q:           String,
    val opts:        List<String> = emptyList(),
    val correct:     Int          = 0,
    val exp:         String       = "",
    val type:        QuizType     = QuizType.MULTIPLE_CHOICE,
    val identAnswer: String       = ""
)

data class Quiz(
    val id:               Int,
    val title:            String,
    val creator:          String,
    val creatorName:      String        = "",
    val questions:        List<QuizQuestion>,
    val exp:              Int           = 50,
    val quizType:         QuizType      = QuizType.MULTIPLE_CHOICE,
    val timerMode:        QuizTimerMode = QuizTimerMode.NONE,
    val timerSeconds:     Int           = 0,
    val subject:          String        = "General",
    val gradeLevel:       String        = "All",
    val difficulty:       Difficulty    = Difficulty.MEDIUM,
    val code:             String        = "",
    val dateCreated:      String        = "",
    val shuffleQuestions: Boolean       = false,
    val shuffleOptions:   Boolean       = false
)

data class Item(
    val id:          Int,
    val name:        String,
    val rarity:      ItemRarity,
    val slot:        EquipSlot? = null,
    val xpBonus:     Int    = 0,
    val statBonus:   String = "",
    val description: String = "",
    val qty:         Int    = 1,
    val levelReq:    Int    = 1
)

data class Equipment(
    val weapon: Item? = null,
    val armor:  Item? = null,
    val acc1:   Item? = null,
    val acc2:   Item? = null
)

data class Achievement(
    val id:          Int,
    val title:       String,
    val description: String,
    val coinReward:  Int     = 0,
    val unlocked:    Boolean = false,
    val claimed:     Boolean = false
)

data class SessionEntry(val date: String, val minutes: Int, val xpGained: Int)

data class AnswerRecord(
    val question:    String,
    val chosen:      Int,
    val correct:     Int,
    val wasRight:    Boolean,
    val type:        QuizType = QuizType.MULTIPLE_CHOICE,
    val identAnswer: String   = "",
    val chosenText:  String   = ""
)

data class QuizHistoryEntry(
    val quizId:    Int,
    val quizTitle: String,
    val quizCode:  String,
    val date:      String,
    val score:     Int,
    val total:     Int,
    val answers:   List<AnswerRecord>
)

// ─────────────────────────────────────────────────────────────────────────────
//  SHOP
// ─────────────────────────────────────────────────────────────────────────────

data class ShopItem(
    val id:          Int,
    val name:        String,
    val description: String,
    val cost:        Int,
    val effect:      ShopEffect,
    val maxStack:    Int = 5
)

// ─────────────────────────────────────────────────────────────────────────────
//  HELPERS
// ─────────────────────────────────────────────────────────────────────────────

fun getRank(lvl: Int): Rank = when {
    lvl >= 100 -> Rank.S; lvl >= 70 -> Rank.A; lvl >= 50 -> Rank.B
    lvl >= 30  -> Rank.C; lvl >= 15 -> Rank.D; else       -> Rank.E
}

fun xpForNext(lvl: Int) = lvl * 500 + 1000

fun generateCode(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return (1..6).map { chars.random() }.joinToString("")
}

fun todayString(): String =
    java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())

fun shortDate(): String =
    java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(java.util.Date())

fun getPerks(stats: Stats): List<Perk> = buildList {
    if (stats.intStat >= 10) add(Perk("INT 10", "Faster Study: +10% bonus XP"))
    if (stats.intStat >= 20) add(Perk("INT 20", "Speed Reader: Quiz answers count double"))
    if (stats.wis >= 10)     add(Perk("WIS 10", "Insight: Hints available before answering"))
    if (stats.wis >= 20)     add(Perk("WIS 20", "Wisdom: +20% XP from all quizzes"))
    if (stats.foc >= 10)     add(Perk("FOC 10", "Extended Focus: Timer +5 min bonus"))
    if (stats.foc >= 20)     add(Perk("FOC 20", "Iron Focus: Pause timer without penalty"))
    if (stats.sta >= 10)     add(Perk("STA 10", "Iron Will: Daily quest XP doubled"))
    if (stats.sta >= 20)     add(Perk("STA 20", "Endurance: Weekly quest XP +50%"))
}

fun difficultyColor(d: Difficulty) = when (d) {
    Difficulty.EASY   -> Color(0xFF2DBF7A)
    Difficulty.MEDIUM -> Color(0xFFFFB700)
    Difficulty.HARD   -> Color(0xFFE53935)
}

fun difficultyLabel(d: Difficulty) = when (d) {
    Difficulty.EASY -> "EASY"; Difficulty.MEDIUM -> "MEDIUM"; Difficulty.HARD -> "HARD"
}

fun quizTypeLabel(t: QuizType) = when (t) {
    QuizType.MULTIPLE_CHOICE -> "Multiple Choice"
    QuizType.TRUE_FALSE      -> "True / False"
    QuizType.IDENTIFICATION  -> "Identification"
    QuizType.MIX             -> "Mixed"
}

fun rarityColor(r: ItemRarity) = when (r) {
    ItemRarity.COMMON    -> Color(0xFF9E9E9E)
    ItemRarity.RARE      -> Color(0xFF2196F3)
    ItemRarity.EPIC      -> Color(0xFF9C27B0)
    ItemRarity.LEGENDARY -> Color(0xFFFFB700)
    ItemRarity.MYTHIC    -> Color(0xFFE91E63)
}

fun rarityLabel(r: ItemRarity) = when (r) {
    ItemRarity.COMMON -> "COMMON"; ItemRarity.RARE -> "RARE"
    ItemRarity.EPIC   -> "EPIC";   ItemRarity.LEGENDARY -> "LEGENDARY"
    ItemRarity.MYTHIC -> "MYTHIC"
}

// ─────────────────────────────────────────────────────────────────────────────
//  DEFAULT DATA
// ─────────────────────────────────────────────────────────────────────────────

val ALL_ACHIEVEMENTS: List<Achievement> = listOf(
    Achievement(1,  "First Steps",       "Log your first study session",        coinReward = 10),
    Achievement(2,  "Quiz Taker",        "Complete your first quiz",             coinReward = 10),
    Achievement(3,  "Quest Master",      "Complete all daily quests in one day", coinReward = 25),
    Achievement(4,  "Quiz Creator",      "Create your first quiz",               coinReward = 20),
    Achievement(5,  "Level 5",           "Reach Level 5",                        coinReward = 30),
    Achievement(6,  "Level 10",          "Reach Level 10",                       coinReward = 60),
    Achievement(7,  "1000 XP",           "Earn 1000 total XP",                   coinReward = 50),
    Achievement(8,  "Study Hour",        "Study for 60 minutes total",           coinReward = 40),
    Achievement(9,  "Quiz Veteran",      "Complete 5 quizzes",                   coinReward = 75),
    Achievement(10, "Speed Demon",       "Finish a quiz in under 60 seconds",    coinReward = 50),
    Achievement(11, "Popular Creator",   "Your quiz was taken by someone else",  coinReward = 100)
)

val DEFAULT_INVENTORY: List<Item> = listOf(
    Item(1, "Scholar's Pen", ItemRarity.COMMON,    EquipSlot.WEAPON, xpBonus = 5,  levelReq = 1,   description = "+5% XP on quizzes"),
    Item(5, "Ancient Tome",  ItemRarity.LEGENDARY, EquipSlot.WEAPON, xpBonus = 25, levelReq = 15,  description = "+25% quiz XP"),

    // Rank C (Level 30+)
    Item(6, "Sage's Glasses",      ItemRarity.RARE,      EquipSlot.WEAPON, xpBonus = 20, levelReq = 30,  description = "+20% study focus"),

    // Rank B (Level 50+)
    Item(8, "Master's Compass",    ItemRarity.EPIC,      EquipSlot.WEAPON, xpBonus = 30, levelReq = 50,  description = "+30% XP from quizzes"),

    // Rank A (Level 70+)
    Item(10, "Dragon Quill",       ItemRarity.LEGENDARY, EquipSlot.WEAPON, xpBonus = 50, levelReq = 70,  description = "+50% quiz XP"),

    // Rank S (Level 100+)
    Item(13, "Ethereal Grimoire",  ItemRarity.MYTHIC,    EquipSlot.WEAPON, xpBonus = 75, levelReq = 110, description = "+75% quiz XP bonus")
)

val DEFAULT_EQUIPMENT: Equipment = Equipment()

// SA LOOB NG AppState
fun buildCommunityQuizzes(): List<Quiz> = (9001..9033).map { id ->
    val o = CommunityData.getOwnerFor(id)
    val qList = CommunityData.getQuestionsFor(id)
    Quiz(
        id               = id,
        title            = o.title,
        creator          = o.name,
        creatorName      = o.name,
        code             = o.code,
        subject          = o.subject,
        gradeLevel       = o.grade,
        difficulty       = o.diff,
        timerMode        = o.timerMode,
        timerSeconds     = o.timerSecs,
        exp              = qList.size * 20,
        dateCreated      = CommunityData.getDateFor(id),
        quizType         = QuizType.MIX,
        questions        = qList
    )
}

private fun createQuiz(
    id: Int, title: String, creator: String, code: String, subject: String,
    grade: String, diff: Difficulty, tMode: QuizTimerMode, tSecs: Int
): Quiz {
    val qList = CommunityData.getQuestionsFor(id)
    return Quiz(
        id = id, title = title, creator = creator, creatorName = creator, code = code,
        subject = subject, gradeLevel = grade, difficulty = diff, timerMode = tMode,
        timerSeconds = tSecs, exp = qList.size * 20,
        dateCreated = CommunityData.getDateFor(id), // ITO YUNG UNIQUE DATE
        quizType = QuizType.MIX, questions = qList
    )
}

