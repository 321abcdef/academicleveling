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

    @GET("user/stats")
    fun getUserStats(): Call<UserStatsResponse>

    @GET("quizzes")
    fun getQuizzes(
        @Query("search") search: String? = null,
        @Query("difficulty") difficulty: String? = null,
        @Query("grade_level") gradeLevel: String? = null,
        @Query("page") page: Int? = null
    ): Call<QuizListResponse>

    @GET("quizzes/{id}")
    fun getQuiz(@Path("id") id: Int): Call<QuizFullResponse>

    @GET("quizzes/mine")
    fun getMyQuizzes(@Query("page") page: Int? = null): Call<QuizListResponse>

    @DELETE("quizzes/{id}")
    fun deleteQuiz(@Path("id") id: Int): Call<Void>

    @POST("quizzes/{id}/attempts")
    fun startAttempt(@Path("id") id: Int): Call<StartAttemptResponse>

    @POST("quizzes")
    fun createQuiz(@Body request: CreateQuizRequest): Call<QuizFullResponse>

    @PUT("quizzes/{id}")
    fun updateQuiz(@Path("id") id: Int, @Body request: CreateQuizRequest): Call<QuizFullResponse>

    @POST("attempts/{id}/submit-all")
    fun submitAttempt(@Path("id") id: Int, @Body request: SubmitQuizRequest): Call<SubmitQuizResponse>

    @POST("study-sessions")
    fun createStudySession(@Body request: CreateStudySessionRequest): Call<CreateStudySessionResponse>

    @GET("study-sessions")
    fun getStudySessions(): Call<StudySessionListResponse>

    @GET("attempts")
    fun getAttempts(@Query("page") page: Int? = null): Call<AttemptListResponse>

    @GET("attempts/{id}")
    fun getAttemptDetails(@Path("id") id: Int): Call<AttemptDetailResponse>

    @GET("quests")
    fun getQuests(): Call<QuestListResponse>

    @POST("quests/{id}/claim")
    fun claimQuestReward(@Path("id") id: Int): Call<ClaimQuestResponse>
}

object ApiRepository {

    private const val BASE_URL = "https://academic-leveling-api.onrender.com/api/" // Replace with your actual API URL

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
        val request = LoginRequest(email.trim(), password.trim())
        api.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        authToken = body.token
                        onSuccess(body)
                    } else {
                        onError("Empty response body")
                    }
                } else {
                    val errorMsg: String = try {
                        val errorBody = response.errorBody()?.string()
                        val apiError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                        
                        // Extract detailed validation errors if they exist
                        val details = apiError.errors?.values?.flatten()?.joinToString("\n")
                        if (!details.isNullOrBlank()) details else apiError.message
                    } catch (e: Exception) {
                        "Login failed: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun register(
        username: String, email: String, password: String, passwordConfirmation: String,
        onSuccess: (RegisterResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = RegisterRequest(username.trim(), email.trim(), password.trim(), passwordConfirmation.trim())
        api.register(request).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        authToken = body.token
                        onSuccess(body)
                    } else {
                        onError("Empty response body")
                    }
                } else {
                    val errorMsg: String = try {
                        val errorBody = response.errorBody()?.string()
                        val apiError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                        
                        // Extract detailed validation errors if they exist
                        val details = apiError.errors?.values?.flatten()?.joinToString("\n")
                        if (!details.isNullOrBlank()) details else apiError.message
                    } catch (e: Exception) {
                        "Registration failed: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun logout(onComplete: () -> Unit = {}) {
        api.logout().enqueue(object : Callback<LogoutResponse> {
            override fun onResponse(call: Call<LogoutResponse>, response: Response<LogoutResponse>) {
                authToken = null
                onComplete()
            }

            override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                authToken = null
                onComplete()
            }
        })
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

    fun notifyQuizComplete(quizId: Int) {
        android.util.Log.d("ApiRepository", "[STUB] notifyQuizComplete(quizId=$quizId)")
    }

    // ══════════════════════════════════════════════════════════════════════
    //  USER PROFILE
    // ══════════════════════════════════════════════════════════════════════

    fun getUserInfo(
        onSuccess: (UserResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getUser().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        onSuccess(body)
                    } else {
                        onError("Empty response body")
                    }
                } else {
                    val errorMsg: String = try {
                        val errorBody = response.errorBody()?.string()
                        val apiError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                        apiError.message
                    } catch (e: Exception) {
                        "Failed to fetch user: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun getProfile(
        onSuccess: (Map<String, Any>) -> Unit,
        onError: (String) -> Unit
    ) {
        android.util.Log.d("ApiRepository", "[STUB] getProfile()")
    }

    fun updateProfile(
        name: String, email: String,
        onSuccess: (UpdateProfileResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = UpdateProfileRequest(name.trim(), email.trim())
        api.updateProfile(request).enqueue(object : Callback<UpdateProfileResponse> {
            override fun onResponse(call: Call<UpdateProfileResponse>, response: Response<UpdateProfileResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        onSuccess(body)
                    } else {
                        onError("Empty response body")
                    }
                } else {
                    val errorMsg: String = try {
                        val errorBody = response.errorBody()?.string()
                        val apiError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                        
                        val details = apiError.errors?.values?.flatten()?.joinToString("\n")
                        if (!details.isNullOrBlank()) details else apiError.message
                    } catch (e: Exception) {
                        "Failed to update profile: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            }

            override fun onFailure(call: Call<UpdateProfileResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun getUserStats(
        onSuccess: (UserStatsResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getUserStats().enqueue(object : Callback<UserStatsResponse> {
            override fun onResponse(call: Call<UserStatsResponse>, response: Response<UserStatsResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: onError("Empty response")
                } else {
                    onError("Failed to fetch stats: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<UserStatsResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun getQuizzes(
        search: String? = null,
        difficulty: String? = null,
        gradeLevel: String? = null,
        page: Int? = null,
        onSuccess: (QuizListResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getQuizzes(search?.trim(), difficulty, gradeLevel, page).enqueue(object : Callback<QuizListResponse> {
            override fun onResponse(call: Call<QuizListResponse>, response: Response<QuizListResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: onError("Empty response body")
                } else {
                    val errorMsg: String = try {
                        val errorBody = response.errorBody()?.string()
                        val apiError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                        apiError.message
                    } catch (e: Exception) {
                        "Failed to fetch quizzes: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            }

            override fun onFailure(call: Call<QuizListResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun getMyQuizzes(
        page: Int? = null,
        onSuccess: (QuizListResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getMyQuizzes(page).enqueue(object : Callback<QuizListResponse> {
            override fun onResponse(call: Call<QuizListResponse>, response: Response<QuizListResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: onError("Empty response")
                } else {
                    onError("Failed to fetch your quizzes: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<QuizListResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun deleteQuiz(
        quizId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        api.deleteQuiz(quizId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Failed to delete quiz: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun getQuizFullInfo(
        quizId: Int,
        onSuccess: (QuizFullResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getQuiz(quizId).enqueue(object : Callback<QuizFullResponse> {
            override fun onResponse(call: Call<QuizFullResponse>, response: Response<QuizFullResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: onError("Empty response body")
                } else {
                    val errorMsg: String = try {
                        val errorBody = response.errorBody()?.string()
                        val apiError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                        apiError.message
                    } catch (e: Exception) {
                        "Failed to fetch quiz details: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            }

            override fun onFailure(call: Call<QuizFullResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun createQuiz(
        request: CreateQuizRequest,
        onSuccess: (QuizFullResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.createQuiz(request).enqueue(object : Callback<QuizFullResponse> {
            override fun onResponse(call: Call<QuizFullResponse>, response: Response<QuizFullResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: onError("Empty response")
                } else {
                    val errorMsg: String = try {
                        val errorBody = response.errorBody()?.string()
                        val apiError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                        apiError.message
                    } catch (e: Exception) {
                        "Failed to create quiz: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            }
            override fun onFailure(call: Call<QuizFullResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun updateQuiz(
        quizId: Int,
        request: CreateQuizRequest,
        onSuccess: (QuizFullResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.updateQuiz(quizId, request).enqueue(object : Callback<QuizFullResponse> {
            override fun onResponse(call: Call<QuizFullResponse>, response: Response<QuizFullResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: onError("Empty response")
                } else {
                    val errorMsg: String = try {
                        val errorBody = response.errorBody()?.string()
                        val apiError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                        apiError.message
                    } catch (e: Exception) {
                        "Failed to update quiz: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            }
            override fun onFailure(call: Call<QuizFullResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun changePassword(
        current: String, newPw: String, confirmPw: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val request = ChangePasswordRequest(current.trim(), newPw.trim(), confirmPw.trim())
        api.changePassword(request).enqueue(object : Callback<ChangePasswordResponse> {
            override fun onResponse(call: Call<ChangePasswordResponse>, response: Response<ChangePasswordResponse>) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val errorMsg: String = try {
                        val errorBody = response.errorBody()?.string()
                        val apiError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                        
                        val details = apiError.errors?.values?.flatten()?.joinToString("\n")
                        if (!details.isNullOrBlank()) details else apiError.message
                    } catch (e: Exception) {
                        "Failed to change password: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            }

            override fun onFailure(call: Call<ChangePasswordResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun forgotPassword(
        email: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = ForgotPasswordRequest(email.trim())
        api.forgotPassword(request).enqueue(object : Callback<ForgotPasswordResponse> {
            override fun onResponse(call: Call<ForgotPasswordResponse>, response: Response<ForgotPasswordResponse>) {
                if (response.isSuccessful) {
                    onSuccess(response.body()?.message ?: "Reset link sent")
                } else {
                    val errorMsg: String = try {
                        val errorBody = response.errorBody()?.string()
                        val apiError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                        apiError.message
                    } catch (e: Exception) {
                        "Failed: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            }
            override fun onFailure(call: Call<ForgotPasswordResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun resetPassword(
        request: ResetPasswordRequest,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val trimmedRequest = request.copy(
            email = request.email.trim(),
            password = request.password.trim(),
            passwordConfirmation = request.passwordConfirmation.trim()
        )
        api.resetPassword(trimmedRequest).enqueue(object : Callback<ResetPasswordResponse> {
            override fun onResponse(call: Call<ResetPasswordResponse>, response: Response<ResetPasswordResponse>) {
                if (response.isSuccessful) {
                    onSuccess(response.body()?.message ?: "Password reset successful")
                } else {
                    val errorMsg: String = try {
                        val errorBody = response.errorBody()?.string()
                        val apiError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                        val details = apiError.errors?.values?.flatten()?.joinToString("\n")
                        if (!details.isNullOrBlank()) details else apiError.message
                    } catch (e: Exception) {
                        "Reset failed: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            }
            override fun onFailure(call: Call<ResetPasswordResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
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

    // ══════════════════════════════════════════════════════════════════════
    //  QUIZ ATTEMPTS
    // ══════════════════════════════════════════════════════════════════════

    fun startQuizAttempt(
        quizId: Int,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        api.startAttempt(quizId).enqueue(object : Callback<StartAttemptResponse> {
            override fun onResponse(call: Call<StartAttemptResponse>, response: Response<StartAttemptResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it.attemptId) } ?: onError("Empty response")
                } else {
                    onError("Failed to start attempt: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<StartAttemptResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun submitQuizAttempt(
        attemptId: Int,
        answers: List<SubmitAnswerItem>,
        onSuccess: (SubmitQuizResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = SubmitQuizRequest(answers)
        api.submitAttempt(attemptId, request).enqueue(object : Callback<SubmitQuizResponse> {
            override fun onResponse(call: Call<SubmitQuizResponse>, response: Response<SubmitQuizResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: onError("Empty response")
                } else {
                    val errorMsg: String = try {
                        val errorBody = response.errorBody()?.string()
                        val apiError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                        apiError.message
                    } catch (e: Exception) {
                        "Failed to submit quiz: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            }
            override fun onFailure(call: Call<SubmitQuizResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun createStudySession(
        durationSeconds: Int,
        onSuccess: (CreateStudySessionResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        // Format: 2026-04-27T14:35:00
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
        val now = sdf.format(java.util.Date())
        val request = CreateStudySessionRequest(durationSeconds, now)

        api.createStudySession(request).enqueue(object : Callback<CreateStudySessionResponse> {
            override fun onResponse(call: Call<CreateStudySessionResponse>, response: Response<CreateStudySessionResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: onError("Empty response")
                } else {
                    val errorMsg: String = try {
                        val errorBody = response.errorBody()?.string()
                        val apiError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                        apiError.message
                    } catch (e: Exception) {
                        "Failed: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            }
            override fun onFailure(call: Call<CreateStudySessionResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun getStudySessions(
        onSuccess: (StudySessionListResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getStudySessions().enqueue(object : Callback<StudySessionListResponse> {
            override fun onResponse(call: Call<StudySessionListResponse>, response: Response<StudySessionListResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: onError("Empty response")
                } else {
                    onError("Failed: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<StudySessionListResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun getAttempts(
        page: Int? = null,
        onSuccess: (AttemptListResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getAttempts(page).enqueue(object : Callback<AttemptListResponse> {
            override fun onResponse(call: Call<AttemptListResponse>, response: Response<AttemptListResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: onError("Empty response")
                } else {
                    onError("Failed to fetch history: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<AttemptListResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun getAttemptDetails(
        attemptId: Int,
        onSuccess: (AttemptDetailResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getAttemptDetails(attemptId).enqueue(object : Callback<AttemptDetailResponse> {
            override fun onResponse(call: Call<AttemptDetailResponse>, response: Response<AttemptDetailResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: onError("Empty response")
                } else {
                    onError("Failed to fetch details: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<AttemptDetailResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    // ══════════════════════════════════════════════════════════════════════
    //  QUESTS
    // ══════════════════════════════════════════════════════════════════════

    fun getQuests(
        onSuccess: (QuestListResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getQuests().enqueue(object : Callback<QuestListResponse> {
            override fun onResponse(call: Call<QuestListResponse>, response: Response<QuestListResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: onError("Empty response")
                } else {
                    onError("Failed: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<QuestListResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun claimQuestReward(
        questId: Int,
        onSuccess: (ClaimQuestResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.claimQuestReward(questId).enqueue(object : Callback<ClaimQuestResponse> {
            override fun onResponse(call: Call<ClaimQuestResponse>, response: Response<ClaimQuestResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: onError("Empty response")
                } else {
                    onError("Failed to claim: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ClaimQuestResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }
}
