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

    @GET("quizzes")
    fun getQuizzes(@Query("page") page: Int? = null): Call<QuizListResponse>

    @GET("quizzes/mine")
    fun getMyQuizzes(@Query("page") page: Int? = null): Call<QuizListResponse>

    @POST("quizzes")
    fun createQuiz(@Body request: CreateQuizRequest): Call<CreateQuizResponse>

    @GET("quizzes/{id}")
    fun getQuizDetails(@Path("id") id: Int): Call<QuizDetailsResponse>

    @POST("quizzes/{id}/attempts")
    fun startQuizAttempt(@Path("id") id: Int): Call<StartQuizAttemptResponse>

    @POST("attempts/{id}/answers")
    fun submitAttemptAnswers(
        @Path("id") id: Int,
        @Body request: SubmitAttemptAnswersRequest
    ): Call<SubmitAttemptAnswersResponse>

    @POST("attempts/{id}/submit")
    fun submitAttempt(@Path("id") id: Int): Call<SubmitAttemptResponse>

    @POST("attempts/{id}/submit-all")
    fun submitAllAttempt(@Path("id") id: Int): Call<SubmitAllAttemptResponse>

    @PUT("quizzes/{id}")
    fun updateQuiz(@Path("id") id: Int, @Body request: CreateQuizRequest): Call<QuizApiData>

    @DELETE("quizzes/{id}")
    fun deleteQuiz(@Path("id") id: Int): Call<Void>
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

    fun getQuizzes(
        page: Int? = null,
        onSuccess: (QuizListResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getQuizzes(page).enqueue(object : Callback<QuizListResponse> {
            override fun onResponse(call: Call<QuizListResponse>, response: Response<QuizListResponse>) {
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
        onSuccess: (List<Quiz>) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getMyQuizzes(page).enqueue(object : Callback<QuizListResponse> {
            override fun onResponse(call: Call<QuizListResponse>, response: Response<QuizListResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        onSuccess(body.data.map { it.toLocalQuiz() })
                    } else {
                        onError("Empty response body")
                    }
                } else {
                    val errorMsg: String = try {
                        val errorBody = response.errorBody()?.string()
                        val apiError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                        apiError.message
                    } catch (e: Exception) {
                        "Failed to fetch my quizzes: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            }

            override fun onFailure(call: Call<QuizListResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun createQuiz(
        request: CreateQuizRequest,
        onSuccess: (CreateQuizResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.createQuiz(request).enqueue(object : Callback<CreateQuizResponse> {
            override fun onResponse(call: Call<CreateQuizResponse>, response: Response<CreateQuizResponse>) {
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
                        "Failed to create quiz: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            }

            override fun onFailure(call: Call<CreateQuizResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun getQuizDetails(
        id: Int,
        onSuccess: (QuizDetailsResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getQuizDetails(id).enqueue(object : Callback<QuizDetailsResponse> {
            override fun onResponse(call: Call<QuizDetailsResponse>, response: Response<QuizDetailsResponse>) {
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
                        "Failed to fetch quiz details: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            }

            override fun onFailure(call: Call<QuizDetailsResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun startQuizAttempt(
        quizId: Int,
        onSuccess: (StartQuizAttemptResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.startQuizAttempt(quizId).enqueue(object : Callback<StartQuizAttemptResponse> {
            override fun onResponse(
                call: Call<StartQuizAttemptResponse>,
                response: Response<StartQuizAttemptResponse>
            ) {
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
                        "Failed to start quiz attempt: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            }

            override fun onFailure(call: Call<StartQuizAttemptResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun submitAttemptAnswers(
        attemptId: Int,
        answers: List<Map<String, Any?>>,
        onSuccess: (SubmitAttemptAnswersResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = SubmitAttemptAnswersRequest(answers = answers)
        api.submitAttemptAnswers(attemptId, request).enqueue(object : Callback<SubmitAttemptAnswersResponse> {
            override fun onResponse(
                call: Call<SubmitAttemptAnswersResponse>,
                response: Response<SubmitAttemptAnswersResponse>
            ) {
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
                        "Failed to submit attempt answers: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            }

            override fun onFailure(call: Call<SubmitAttemptAnswersResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun submitAttempt(
        attemptId: Int,
        onSuccess: (SubmitAttemptResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.submitAttempt(attemptId).enqueue(object : Callback<SubmitAttemptResponse> {
            override fun onResponse(
                call: Call<SubmitAttemptResponse>,
                response: Response<SubmitAttemptResponse>
            ) {
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
                        "Failed to submit attempt: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            }

            override fun onFailure(call: Call<SubmitAttemptResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun submitAllAttempt(
        attemptId: Int,
        onSuccess: (SubmitAllAttemptResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.submitAllAttempt(attemptId).enqueue(object : Callback<SubmitAllAttemptResponse> {
            override fun onResponse(
                call: Call<SubmitAllAttemptResponse>,
                response: Response<SubmitAllAttemptResponse>
            ) {
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
                        "Failed to submit all attempt answers: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            }

            override fun onFailure(call: Call<SubmitAllAttemptResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun updateQuiz(
        id: Int,
        request: CreateQuizRequest,
        onSuccess: (QuizApiData) -> Unit,
        onError: (String) -> Unit
    ) {
        api.updateQuiz(id, request).enqueue(object : Callback<QuizApiData> {
            override fun onResponse(call: Call<QuizApiData>, response: Response<QuizApiData>) {
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
                        "Failed to update quiz: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            }

            override fun onFailure(call: Call<QuizApiData>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

    fun deleteQuiz(
        id: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        api.deleteQuiz(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val errorMsg: String = try {
                        val errorBody = response.errorBody()?.string()
                        val apiError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                        apiError.message
                    } catch (e: Exception) {
                        "Failed to delete quiz: ${response.code()}"
                    }
                    onError(errorMsg)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
    }

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

    private fun QuizApiData.toLocalQuiz(): Quiz {
        return Quiz(
            id = id,
            title = title,
            creator = user.name,
            creatorName = user.name,
            questions = questions?.map { it.toLocalQuestion() } ?: emptyList(),
            exp = (questions?.size ?: questionsCount) * 20,
            quizType = type.toQuizType(),
            timerMode = timerMode.toTimerMode(),
            timerSeconds = 0,
            subject = subject,
            gradeLevel = gradeLevel,
            difficulty = difficulty.toDifficulty(),
            code = quizCode,
            dateCreated = createdAt,
            shuffleQuestions = isQuestionShuffled,
            shuffleOptions = isChoicesShuffled
        )
    }

    private fun QuestionApiData.toLocalQuestion(): QuizQuestion {
        return when (type.lowercase()) {
            "true_false", "truefalse", "true-false" -> {
                val answer = correctAnswer?.trim()?.lowercase()
                QuizQuestion(
                    q = questionText,
                    opts = listOf("True", "False"),
                    correct = if (answer == "false") 1 else 0,
                    exp = "",
                    type = QuizType.TRUE_FALSE,
                    identAnswer = ""
                )
            }
            "identification", "ident" -> {
                QuizQuestion(
                    q = questionText,
                    opts = emptyList(),
                    correct = 0,
                    exp = "",
                    type = QuizType.IDENTIFICATION,
                    identAnswer = correctAnswer ?: ""
                )
            }
            else -> {
                val localChoices = choices.map { it.choiceText }
                val correctIndex = choices.indexOfFirst { it.isCorrect }.coerceAtLeast(0)
                QuizQuestion(
                    q = questionText,
                    opts = localChoices,
                    correct = correctIndex,
                    exp = "",
                    type = QuizType.MULTIPLE_CHOICE,
                    identAnswer = ""
                )
            }
        }
    }

    private fun String.toDifficulty(): Difficulty = when (lowercase()) {
        "easy" -> Difficulty.EASY
        "hard" -> Difficulty.HARD
        else -> Difficulty.MEDIUM
    }

    private fun String.toQuizType(): QuizType = when (lowercase()) {
        "multiple_choice", "multiple-choice", "mcq" -> QuizType.MULTIPLE_CHOICE
        "true_false", "truefalse", "true-false" -> QuizType.TRUE_FALSE
        "identification", "ident" -> QuizType.IDENTIFICATION
        else -> QuizType.MIX
    }

    private fun String.toTimerMode(): QuizTimerMode = when (lowercase()) {
        "quiz", "whole_quiz", "whole-quiz" -> QuizTimerMode.WHOLE_QUIZ
        "question", "per_question", "per-question" -> QuizTimerMode.PER_QUESTION
        else -> QuizTimerMode.NONE
    }
}
