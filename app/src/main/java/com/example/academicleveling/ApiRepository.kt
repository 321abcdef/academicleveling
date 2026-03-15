package com.example.academicleveling.data

@Suppress("UNUSED_PARAMETER", "unused")
object ApiRepository {

    private const val BASE_URL = "https://your-laravel-api.com/api"

    var authToken: String? = null
        private set

    // ══════════════════════════════════════════════════════════════════════
    //  AUTH
    // ══════════════════════════════════════════════════════════════════════

    fun login(
        email: String, password: String,
        onSuccess: (token: String) -> Unit,
        onError: (String) -> Unit
    ) {
        android.util.Log.d("ApiRepository", "[STUB] login($email)")
    }

    fun register(
        name: String, email: String, password: String, grade: String,
        onSuccess: (token: String) -> Unit,
        onError: (String) -> Unit
    ) {
        android.util.Log.d("ApiRepository", "[STUB] register($name, $email)")
    }

    fun logout() {
        authToken = null
        android.util.Log.d("ApiRepository", "[STUB] logout()")
    }

    // ══════════════════════════════════════════════════════════════════════
    //  QUIZ
    // ══════════════════════════════════════════════════════════════════════

    fun getQuizByCode(
        code: String,
        onSuccess: (Quiz) -> Unit,
        onError: (String) -> Unit
    ) {
        android.util.Log.d("ApiRepository", "[STUB] getQuizByCode($code)")
    }

    fun createQuiz(
        quiz: Quiz,
        onSuccess: (Quiz) -> Unit,
        onError: (String) -> Unit
    ) {
        android.util.Log.d("ApiRepository", "[STUB] createQuiz(${quiz.title})")
    }

    fun notifyQuizComplete(quizId: Int) {
        android.util.Log.d("ApiRepository", "[STUB] notifyQuizComplete(quizId=$quizId)")
    }

    // ══════════════════════════════════════════════════════════════════════
    //  USER PROFILE
    // ══════════════════════════════════════════════════════════════════════

    fun getProfile(
        onSuccess: (Map<String, Any>) -> Unit,
        onError: (String) -> Unit
    ) {
        android.util.Log.d("ApiRepository", "[STUB] getProfile()")
    }

    fun updateProfile(
        name: String, email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        android.util.Log.d("ApiRepository", "[STUB] updateProfile($name, $email)")
    }

    fun changePassword(
        current: String, newPw: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        android.util.Log.d("ApiRepository", "[STUB] changePassword()")
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ACHIEVEMENTS
    // ══════════════════════════════════════════════════════════════════════

    fun claimAchievement(
        achievementId: Int,
        onSuccess: (coins: Int) -> Unit,
        onError: (String) -> Unit
    ) {
        android.util.Log.d("ApiRepository", "[STUB] claimAchievement(id=$achievementId)")
    }
}