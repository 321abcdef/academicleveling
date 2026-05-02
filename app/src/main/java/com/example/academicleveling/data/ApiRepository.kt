package com.example.academicleveling.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AcademicApi {

    // ── Auth ──────────────────────────────────────────────────────────────

    @POST("login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("logout")
    fun logout(): Call<LogoutResponse>

    @POST("change-password")
    fun changePassword(@Body request: ChangePasswordRequest): Call<ChangePasswordResponse>

    @POST("forgot-password")
    fun forgotPassword(@Body request: ForgotPasswordRequest): Call<ForgotPasswordResponse>

    @POST("reset-password")
    fun resetPassword(@Body request: ResetPasswordRequest): Call<ResetPasswordResponse>

    @GET("user")
    fun getUser(): Call<UserResponse>

    @PUT("user")
    fun updateProfile(@Body request: UpdateProfileRequest): Call<UpdateProfileResponse>

    // ── Quizzes ───────────────────────────────────────────────────────────

    @GET("quizzes")
    fun getAllQuizzes(
        @Query("search") search: String? = null,
        @Query("difficulty") difficulty: String? = null,
        @Query("grade_level") gradeLevel: String? = null,
        @Query("page") page: Int? = null
    ): Call<GetAllQuizzesResponse>

    @GET("quizzes/mine")
    fun getMyQuizzes(@Query("page") page: Int? = null): Call<GetAllQuizzesResponse>

    @GET("quizzes/{id}")
    fun getQuiz(@Path("id") id: Int): Call<SingleQuizResponse>

    @POST("quizzes")
    fun createQuiz(@Body request: CreateQuizRequest): Call<SingleQuizResponse>

    @PUT("quizzes/{id}")
    fun updateQuiz(@Path("id") id: Int, @Body request: CreateQuizRequest): Call<SingleQuizResponse>

    @DELETE("quizzes/{id}")
    fun deleteQuiz(@Path("id") id: Int): Call<MessageResponse>

    // ── Attempts ──────────────────────────────────────────────────────────

    @GET("attempts")
    fun getAllAttempts(@Query("page") page: Int? = null): Call<GetAllAttemptsResponse>

    @GET("attempts/{id}")
    fun getAttempt(@Path("id") id: Int): Call<SingleAttemptResponse>

    @POST("quizzes/{quizId}/attempts")
    fun startAttempt(@Path("quizId") quizId: Int): Call<StartAttemptResponse>

    @POST("attempts/{attemptId}/answers")
    fun saveAnswer(@Path("attemptId") attemptId: Int, @Body request: AnswerRequest): Call<MessageResponse>

    @POST("attempts/{attemptId}/submit")
    fun submitAttempt(@Path("attemptId") attemptId: Int): Call<SubmitAttemptResponse>

    @POST("attempts/{attemptId}/submit-all")
    fun submitAll(@Path("attemptId") attemptId: Int, @Body request: SubmitAllRequest): Call<SubmitAttemptResponse>

    // ── Study Sessions ────────────────────────────────────────────────────

    @GET("study-sessions")
    fun getAllStudySessions(): Call<GetAllStudySessionsResponse>

    @POST("study-sessions")
    fun createStudySession(@Body request: CreateStudySessionRequest): Call<CreateStudySessionResponse>

    // ── Quests ────────────────────────────────────────────────────────────

    @GET("quests")
    fun getAllQuests(): Call<GetAllQuestsResponse>

    @POST("quests/{id}/claim")
    fun claimQuestReward(@Path("id") id: Int): Call<ClaimQuestResponse>
}

object ApiRepository {

    private const val BASE_URL = "https://academic-leveling-api.onrender.com/api/"

    private val api: AcademicApi
    private val gson = com.google.gson.Gson()

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                request.addHeader("Accept", "application/json")
                authToken?.let {
                    request.addHeader("Authorization", "Bearer $it")
                }
                chain.proceed(request.build())
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        api = retrofit.create(AcademicApi::class.java)
    }

    var authToken: String? = null
        private set

    fun setToken(token: String) {
        authToken = token
    }

    // ══════════════════════════════════════════════════════════════════════
    //  AUTH
    // ══════════════════════════════════════════════════════════════════════

    fun login(
        email: String, password: String,
        onSuccess: (LoginResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.login(LoginRequest(email, password)).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) { authToken = body.token; onSuccess(body) }
                    else onError("Empty response body")
                } else onError(parseError(response, "Login failed"))
            }
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    fun register(
        username: String, email: String, password: String, passwordConfirmation: String,
        onSuccess: (RegisterResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.register(RegisterRequest(username, email, password, passwordConfirmation)).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) { authToken = body.token; onSuccess(body) }
                    else onError("Empty response body")
                } else onError(parseError(response, "Registration failed"))
            }
            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    fun logout(onComplete: () -> Unit = {}) {
        api.logout().enqueue(object : Callback<LogoutResponse> {
            override fun onResponse(call: Call<LogoutResponse>, response: Response<LogoutResponse>) { authToken = null; onComplete() }
            override fun onFailure(call: Call<LogoutResponse>, t: Throwable) { authToken = null; onComplete() }
        })
    }

    fun getUserInfo(
        onSuccess: (UserResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getUser().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) { val body = response.body(); if (body != null) onSuccess(body) else onError("Empty response body") }
                else onError(parseError(response, "Failed to fetch user"))
            }
            override fun onFailure(call: Call<UserResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    fun getProfile(onSuccess: (Map<String, Any>) -> Unit, onError: (String) -> Unit) {
        android.util.Log.d("ApiRepository", "[STUB] getProfile()")
    }

    fun updateProfile(
        name: String, email: String,
        onSuccess: (UpdateProfileResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.updateProfile(UpdateProfileRequest(name, email)).enqueue(object : Callback<UpdateProfileResponse> {
            override fun onResponse(call: Call<UpdateProfileResponse>, response: Response<UpdateProfileResponse>) {
                if (response.isSuccessful) { val body = response.body(); if (body != null) onSuccess(body) else onError("Empty response body") }
                else onError(parseError(response, "Failed to update profile"))
            }
            override fun onFailure(call: Call<UpdateProfileResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    fun changePassword(
        current: String, newPw: String, confirmPw: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        api.changePassword(ChangePasswordRequest(current, newPw, confirmPw)).enqueue(object : Callback<ChangePasswordResponse> {
            override fun onResponse(call: Call<ChangePasswordResponse>, response: Response<ChangePasswordResponse>) {
                if (response.isSuccessful) onSuccess() else onError(parseError(response, "Failed to change password"))
            }
            override fun onFailure(call: Call<ChangePasswordResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    fun forgotPassword(
        email: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        api.forgotPassword(ForgotPasswordRequest(email)).enqueue(object : Callback<ForgotPasswordResponse> {
            override fun onResponse(call: Call<ForgotPasswordResponse>, response: Response<ForgotPasswordResponse>) {
                if (response.isSuccessful) onSuccess(response.body()?.message ?: "Reset link sent")
                else onError(parseError(response, "Failed"))
            }
            override fun onFailure(call: Call<ForgotPasswordResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    fun resetPassword(
        request: ResetPasswordRequest,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        api.resetPassword(request).enqueue(object : Callback<ResetPasswordResponse> {
            override fun onResponse(call: Call<ResetPasswordResponse>, response: Response<ResetPasswordResponse>) {
                if (response.isSuccessful) onSuccess(response.body()?.message ?: "Password reset successful")
                else onError(parseError(response, "Reset failed"))
            }
            override fun onFailure(call: Call<ResetPasswordResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    // ══════════════════════════════════════════════════════════════════════
    //  QUIZ
    // ══════════════════════════════════════════════════════════════════════

    /** Legacy stub — kept for compatibility */
    fun getQuizByCode(code: String, onSuccess: (Quiz) -> Unit, onError: (String) -> Unit) {
        android.util.Log.d("ApiRepository", "[STUB] getQuizByCode($code)")
    }

    /** Legacy stub — kept for compatibility */
    fun createQuiz(quiz: Quiz, onSuccess: (Quiz) -> Unit, onError: (String) -> Unit) {
        android.util.Log.d("ApiRepository", "[STUB] createQuiz(${quiz.title})")
    }

    fun notifyQuizComplete(quizId: Int) {
        android.util.Log.d("ApiRepository", "[STUB] notifyQuizComplete(quizId=$quizId)")
    }

    fun getAllQuizzes(
        search: String? = null,
        difficulty: String? = null,
        gradeLevel: String? = null,
        page: Int? = null,
        onSuccess: (GetAllQuizzesResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getAllQuizzes(search, difficulty, gradeLevel, page).enqueue(object : Callback<GetAllQuizzesResponse> {
            override fun onResponse(call: Call<GetAllQuizzesResponse>, response: Response<GetAllQuizzesResponse>) {
                if (response.isSuccessful) { val body = response.body(); if (body != null) onSuccess(body) else onError("Empty response body") }
                else onError(parseError(response, "Failed to fetch quizzes"))
            }
            override fun onFailure(call: Call<GetAllQuizzesResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    fun getMyQuizzes(
        page: Int? = null,
        onSuccess: (GetAllQuizzesResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getMyQuizzes(page).enqueue(object : Callback<GetAllQuizzesResponse> {
            override fun onResponse(call: Call<GetAllQuizzesResponse>, response: Response<GetAllQuizzesResponse>) {
                if (response.isSuccessful) { val body = response.body(); if (body != null) onSuccess(body) else onError("Empty response body") }
                else onError(parseError(response, "Failed to fetch my quizzes"))
            }
            override fun onFailure(call: Call<GetAllQuizzesResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    fun getQuiz(
        id: Int,
        onSuccess: (SingleQuizResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getQuiz(id).enqueue(object : Callback<SingleQuizResponse> {
            override fun onResponse(call: Call<SingleQuizResponse>, response: Response<SingleQuizResponse>) {
                if (response.isSuccessful) { val body = response.body(); if (body != null) onSuccess(body) else onError("Empty response body") }
                else onError(parseError(response, "Failed to fetch quiz"))
            }
            override fun onFailure(call: Call<SingleQuizResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    fun createQuiz(
        request: CreateQuizRequest,
        onSuccess: (SingleQuizResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.createQuiz(request).enqueue(object : Callback<SingleQuizResponse> {
            override fun onResponse(call: Call<SingleQuizResponse>, response: Response<SingleQuizResponse>) {
                if (response.isSuccessful) { val body = response.body(); if (body != null) onSuccess(body) else onError("Empty response body") }
                else onError(parseError(response, "Failed to create quiz"))
            }
            override fun onFailure(call: Call<SingleQuizResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    fun updateQuiz(
        id: Int,
        request: CreateQuizRequest,
        onSuccess: (SingleQuizResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.updateQuiz(id, request).enqueue(object : Callback<SingleQuizResponse> {
            override fun onResponse(call: Call<SingleQuizResponse>, response: Response<SingleQuizResponse>) {
                if (response.isSuccessful) { val body = response.body(); if (body != null) onSuccess(body) else onError("Empty response body") }
                else onError(parseError(response, "Failed to update quiz"))
            }
            override fun onFailure(call: Call<SingleQuizResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    fun deleteQuiz(
        id: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        api.deleteQuiz(id).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (response.isSuccessful) onSuccess() else onError(parseError(response, "Failed to delete quiz"))
            }
            override fun onFailure(call: Call<MessageResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ATTEMPTS
    // ══════════════════════════════════════════════════════════════════════

    fun getAllAttempts(
        page: Int? = null,
        onSuccess: (GetAllAttemptsResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getAllAttempts(page).enqueue(object : Callback<GetAllAttemptsResponse> {
            override fun onResponse(call: Call<GetAllAttemptsResponse>, response: Response<GetAllAttemptsResponse>) {
                if (response.isSuccessful) { val body = response.body(); if (body != null) onSuccess(body) else onError("Empty response body") }
                else onError(parseError(response, "Failed to fetch attempts"))
            }
            override fun onFailure(call: Call<GetAllAttemptsResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    fun getAttempt(
        id: Int,
        onSuccess: (SingleAttemptResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getAttempt(id).enqueue(object : Callback<SingleAttemptResponse> {
            override fun onResponse(call: Call<SingleAttemptResponse>, response: Response<SingleAttemptResponse>) {
                if (response.isSuccessful) { val body = response.body(); if (body != null) onSuccess(body) else onError("Empty response body") }
                else onError(parseError(response, "Failed to fetch attempt"))
            }
            override fun onFailure(call: Call<SingleAttemptResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    fun startAttempt(
        quizId: Int,
        onSuccess: (StartAttemptResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.startAttempt(quizId).enqueue(object : Callback<StartAttemptResponse> {
            override fun onResponse(call: Call<StartAttemptResponse>, response: Response<StartAttemptResponse>) {
                if (response.isSuccessful) { val body = response.body(); if (body != null) onSuccess(body) else onError("Empty response body") }
                else onError(parseError(response, "Failed to start attempt"))
            }
            override fun onFailure(call: Call<StartAttemptResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    fun saveAnswer(
        attemptId: Int,
        request: AnswerRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        api.saveAnswer(attemptId, request).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (response.isSuccessful) onSuccess() else onError(parseError(response, "Failed to save answer"))
            }
            override fun onFailure(call: Call<MessageResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    fun submitAttempt(
        attemptId: Int,
        onSuccess: (SubmitAttemptResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.submitAttempt(attemptId).enqueue(object : Callback<SubmitAttemptResponse> {
            override fun onResponse(call: Call<SubmitAttemptResponse>, response: Response<SubmitAttemptResponse>) {
                if (response.isSuccessful) { val body = response.body(); if (body != null) onSuccess(body) else onError("Empty response body") }
                else onError(parseError(response, "Failed to submit attempt"))
            }
            override fun onFailure(call: Call<SubmitAttemptResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    fun submitAll(
        attemptId: Int,
        answers: List<AnswerRequest>,
        onSuccess: (SubmitAttemptResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.submitAll(attemptId, SubmitAllRequest(answers)).enqueue(object : Callback<SubmitAttemptResponse> {
            override fun onResponse(call: Call<SubmitAttemptResponse>, response: Response<SubmitAttemptResponse>) {
                if (response.isSuccessful) { val body = response.body(); if (body != null) onSuccess(body) else onError("Empty response body") }
                else onError(parseError(response, "Failed to submit"))
            }
            override fun onFailure(call: Call<SubmitAttemptResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    // ══════════════════════════════════════════════════════════════════════
    //  STUDY SESSIONS
    // ══════════════════════════════════════════════════════════════════════

    fun getAllStudySessions(
        onSuccess: (GetAllStudySessionsResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getAllStudySessions().enqueue(object : Callback<GetAllStudySessionsResponse> {
            override fun onResponse(call: Call<GetAllStudySessionsResponse>, response: Response<GetAllStudySessionsResponse>) {
                if (response.isSuccessful) { val body = response.body(); if (body != null) onSuccess(body) else onError("Empty response body") }
                else onError(parseError(response, "Failed to fetch study sessions"))
            }
            override fun onFailure(call: Call<GetAllStudySessionsResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    fun createStudySession(
        durationSeconds: Int,
        sessionAt: String,
        onSuccess: (CreateStudySessionResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.createStudySession(CreateStudySessionRequest(durationSeconds, sessionAt)).enqueue(object : Callback<CreateStudySessionResponse> {
            override fun onResponse(call: Call<CreateStudySessionResponse>, response: Response<CreateStudySessionResponse>) {
                if (response.isSuccessful) { val body = response.body(); if (body != null) onSuccess(body) else onError("Empty response body") }
                else onError(parseError(response, "Failed to create study session"))
            }
            override fun onFailure(call: Call<CreateStudySessionResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    // ══════════════════════════════════════════════════════════════════════
    //  QUESTS
    // ══════════════════════════════════════════════════════════════════════

    fun getAllQuests(
        onSuccess: (GetAllQuestsResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getAllQuests().enqueue(object : Callback<GetAllQuestsResponse> {
            override fun onResponse(call: Call<GetAllQuestsResponse>, response: Response<GetAllQuestsResponse>) {
                if (response.isSuccessful) { val body = response.body(); if (body != null) onSuccess(body) else onError("Empty response body") }
                else onError(parseError(response, "Failed to fetch quests"))
            }
            override fun onFailure(call: Call<GetAllQuestsResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    fun claimQuestReward(
        questId: Int,
        onSuccess: (ClaimQuestResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.claimQuestReward(questId).enqueue(object : Callback<ClaimQuestResponse> {
            override fun onResponse(call: Call<ClaimQuestResponse>, response: Response<ClaimQuestResponse>) {
                if (response.isSuccessful) { val body = response.body(); if (body != null) onSuccess(body) else onError("Empty response body") }
                else onError(parseError(response, "Failed to claim quest reward"))
            }
            override fun onFailure(call: Call<ClaimQuestResponse>, t: Throwable) = onError(t.message ?: "Unknown error")
        })
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ACHIEVEMENTS
    // ══════════════════════════════════════════════════════════════════════

    fun claimAchievement(achievementId: Int, onSuccess: (coins: Int) -> Unit, onError: (String) -> Unit) {
        android.util.Log.d("ApiRepository", "[STUB] claimAchievement(id=$achievementId)")
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private fun <T> parseError(response: Response<T>, fallback: String): String {
        return try {
            val errorBody = response.errorBody()?.string()
            val apiError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
            val details = apiError.errors?.values?.flatten()?.joinToString("\n")
            if (!details.isNullOrBlank()) details else apiError.message
        } catch (e: Exception) {
            "$fallback: ${response.code()}"
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  REQUEST BODIES
// ══════════════════════════════════════════════════════════════════════

data class CreateQuizRequest(
    val title: String,
    val description: String? = null,
    val subject: String? = null,
    val grade_level: String = "all",          // "all" | "g7-g12" | "college"
    val type: String = "multiple_choice",     // "multiple_choice" | "true_false" | "identification" | "mixed"
    val difficulty: String = "easy",          // "easy" | "medium" | "hard"
    val timer_mode: String = "none",          // "none" | "quiz" | "question"
    val is_question_shuffled: Boolean = false,
    val is_choices_shuffled: Boolean = false,
    val is_public: Boolean = true,
    val questions: List<QuestionRequest> = emptyList()
)

data class QuestionRequest(
    val question_text: String,
    val type: String,
    val points: Int = 1,
    val order: Int,
    val choices: List<ChoiceRequest>? = null,
    val correct_answer: String? = null        // for identification questions
)

data class ChoiceRequest(
    val choice_text: String,
    val is_correct: Boolean
)

data class AnswerRequest(
    val question_id: Int,
    val choice_id: Int? = null,
    val answer_text: String? = null
)

data class SubmitAllRequest(val answers: List<AnswerRequest>)

data class CreateStudySessionRequest(
    val duration: Int,       // seconds, minimum 300
    val session_at: String   // ISO 8601 e.g. "2026-03-24T14:35:00"
)