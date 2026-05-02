package com.example.academicleveling.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @GET("quizzes")
    fun quizzesBySearch(@Query("search") search: String): Call<JsonObject>

    @GET("quizzes/{id}")
    fun getQuizById(@Path("id") quizId: Int): Call<JsonObject>
}

object ApiRepository {

    private val BASE_URL = com.example.academicleveling.BuildConfig.API_BASE_URL // Replace with your actual API URL

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
        val request = LoginRequest(email, password)
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
        val request = RegisterRequest(username, email, password, passwordConfirmation)
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
        api.quizzesBySearch(code.trim()).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (!response.isSuccessful) {
                    val errorMsg = try {
                        val errorBody = response.errorBody()?.string()
                        val apiError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                        apiError.message
                    } catch (e: Exception) {
                        "Failed to fetch quiz: ${response.code()}"
                    }
                    onError(errorMsg)
                    return
                }

                val body = response.body()
                val items = body?.getAsJsonArray("data")
                val quizSummary = if (items != null) mapQuizSummary(items) else null
                if (quizSummary == null) {
                    onError("Quiz not found")
                    return
                }

                api.getQuizById(quizSummary.id).enqueue(object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        if (!response.isSuccessful) {
                            onSuccess(quizSummary)
                            return
                        }
                        val detailed = response.body()?.getAsJsonObject("data")?.let { mapQuizDetail(it) }
                        onSuccess(detailed ?: quizSummary)
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        onSuccess(quizSummary)
                    }
                })
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
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
        getUserInfo(
            onSuccess = { response ->
                onSuccess(
                    mapOf(
                        "username" to response.data.username,
                        "email" to response.data.email,
                        "level" to response.data.progress.level,
                        "coins" to response.data.coins
                    )
                )
            },
            onError = onError
        )
    }

    fun updateProfile(
        name: String, email: String,
        onSuccess: (UpdateProfileResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = UpdateProfileRequest(name, email)
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

    fun changePassword(
        current: String, newPw: String, confirmPw: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val request = ChangePasswordRequest(current, newPw, confirmPw)
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
        val request = ForgotPasswordRequest(email)
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
        api.resetPassword(request).enqueue(object : Callback<ResetPasswordResponse> {
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

    private fun mapQuizSummary(items: JsonArray): Quiz? {
        val first = items.firstOrNull() ?: return null
        if (!first.isJsonObject) return null
        val o = first.asJsonObject
        return Quiz(
            id = o.get("id")?.asInt ?: 0,
            title = o.get("title")?.asString ?: "Untitled",
            creator = o.getAsJsonObject("user")?.get("name")?.asString
                ?: o.getAsJsonObject("user")?.get("username")?.asString
                ?: "Unknown",
            creatorName = o.getAsJsonObject("user")?.get("name")?.asString
                ?: o.getAsJsonObject("user")?.get("username")?.asString
                ?: "Unknown",
            questions = emptyList(),
            subject = o.get("subject")?.asString ?: "General",
            gradeLevel = o.get("grade_level")?.asString ?: "all",
            code = o.get("quiz_code")?.asString ?: "",
            difficulty = parseDifficulty(o.get("difficulty")?.asString),
            quizType = parseQuizType(o.get("type")?.asString)
        )
    }

    private fun mapQuizDetail(o: JsonObject): Quiz {
        val questions = o.getAsJsonArray("questions")
            ?.mapNotNull { questionEl ->
                if (!questionEl.isJsonObject) return@mapNotNull null
                val questionObj = questionEl.asJsonObject
                val type = parseQuizType(questionObj.get("type")?.asString)
                val choices = questionObj.getAsJsonArray("choices")
                    ?.mapNotNull { choiceEl ->
                        if (!choiceEl.isJsonObject) return@mapNotNull null
                        val choiceObj = choiceEl.asJsonObject
                        choiceObj.get("choice_text")?.asString
                    }
                    ?: emptyList()
                val correctIndex = questionObj.getAsJsonArray("choices")
                    ?.indexOfFirst { c ->
                        c.isJsonObject &&
                            c.asJsonObject.get("is_correct")?.asBoolean == true
                    }
                    ?.takeIf { it >= 0 } ?: 0

                QuizQuestion(
                    q = questionObj.get("question_text")?.asString ?: "",
                    opts = choices,
                    correct = correctIndex,
                    exp = questionObj.get("explanation")?.asString ?: "",
                    type = type,
                    identAnswer = questionObj.get("correct_answer")?.asString ?: ""
                )
            } ?: emptyList()

        return Quiz(
            id = o.get("id")?.asInt ?: 0,
            title = o.get("title")?.asString ?: "Untitled",
            creator = o.getAsJsonObject("user")?.get("name")?.asString
                ?: o.getAsJsonObject("user")?.get("username")?.asString
                ?: "Unknown",
            creatorName = o.getAsJsonObject("user")?.get("name")?.asString
                ?: o.getAsJsonObject("user")?.get("username")?.asString
                ?: "Unknown",
            questions = questions,
            subject = o.get("subject")?.asString ?: "General",
            gradeLevel = o.get("grade_level")?.asString ?: "all",
            difficulty = parseDifficulty(o.get("difficulty")?.asString),
            code = o.get("quiz_code")?.asString ?: "",
            quizType = parseQuizType(o.get("type")?.asString),
            timerMode = parseTimerMode(o.get("timer_mode")?.asString),
            shuffleQuestions = o.get("is_question_shuffled")?.asBoolean ?: false,
            shuffleOptions = o.get("is_choices_shuffled")?.asBoolean ?: false
        )
    }

    private fun parseQuizType(value: String?): QuizType = when (value?.lowercase()) {
        "true_false" -> QuizType.TRUE_FALSE
        "identification" -> QuizType.IDENTIFICATION
        "mixed" -> QuizType.MIX
        else -> QuizType.MULTIPLE_CHOICE
    }

    private fun parseDifficulty(value: String?): Difficulty = when (value?.lowercase()) {
        "easy" -> Difficulty.EASY
        "hard" -> Difficulty.HARD
        else -> Difficulty.MEDIUM
    }

    private fun parseTimerMode(value: String?): QuizTimerMode = when (value?.lowercase()) {
        "quiz", "whole_quiz" -> QuizTimerMode.WHOLE_QUIZ
        "question", "per_question" -> QuizTimerMode.PER_QUESTION
        else -> QuizTimerMode.NONE
    }
}
