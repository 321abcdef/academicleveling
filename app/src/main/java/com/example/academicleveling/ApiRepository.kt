package com.example.academicleveling.data

import com.example.academicleveling.BuildConfig
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

object ApiRepository {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(ApiService::class.java)

    var authToken: String? = null
        private set

    private interface ApiService {
        @POST("login")
        fun login(@Body body: Map<String, String>): Call<JsonObject>

        @POST("register")
        fun register(@Body body: Map<String, String>): Call<JsonObject>

        @POST("logout")
        fun logout(@Header("Authorization") bearer: String): Call<JsonObject>

        @GET("user")
        fun getProfile(@Header("Authorization") bearer: String): Call<JsonObject>

        @POST("change-password")
        fun changePassword(
            @Header("Authorization") bearer: String,
            @Body body: Map<String, String>
        ): Call<JsonObject>

        @GET("quizzes")
        fun quizzesBySearch(
            @Header("Authorization") bearer: String,
            @Query("search") search: String
        ): Call<JsonObject>
    }

    private fun bearer(): String? = authToken?.let { "Bearer $it" }

    private fun parseError(response: Response<*>): String {
        return response.errorBody()?.string()?.takeIf { it.isNotBlank() }
            ?: "Request failed (${response.code()})"
    }

    // ══════════════════════════════════════════════════════════════════════
    //  AUTH
    // ══════════════════════════════════════════════════════════════════════

    fun login(
        email: String, password: String,
        onSuccess: (token: String) -> Unit,
        onError: (String) -> Unit
    ) {
        api.login(
            mapOf(
                "login" to email,
                "password" to password
            )
        ).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (!response.isSuccessful || response.body() == null) {
                    onError(parseError(response))
                    return
                }

                val token = response.body()!!.get("token")?.asString
                if (token.isNullOrBlank()) {
                    onError("Login succeeded but token is missing.")
                    return
                }

                authToken = token
                onSuccess(token)
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                onError(t.message ?: "Network error")
            }
        })
    }

    fun register(
        name: String, email: String, password: String, grade: String,
        onSuccess: (token: String) -> Unit,
        onError: (String) -> Unit
    ) {
        api.register(
            mapOf(
                "username" to name,
                "email" to email,
                "password" to password,
                "password_confirmation" to password
            )
        ).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (!response.isSuccessful || response.body() == null) {
                    onError(parseError(response))
                    return
                }

                val token = response.body()!!.get("token")?.asString
                if (token.isNullOrBlank()) {
                    onError("Register succeeded but token is missing.")
                    return
                }

                authToken = token
                onSuccess(token)
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                onError(t.message ?: "Network error")
            }
        })
    }

    fun logout() {
        val bearer = bearer()
        if (bearer == null) {
            authToken = null
            return
        }

        api.logout(bearer).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                authToken = null
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                authToken = null
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
        val bearer = bearer()
        if (bearer == null) {
            onError("Not authenticated")
            return
        }

        api.quizzesBySearch(bearer, code).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (!response.isSuccessful || response.body() == null) {
                    onError(parseError(response))
                    return
                }

                val data = response.body()!!.getAsJsonArray("data")
                if (data == null || data.size() == 0) {
                    onError("Quiz not found")
                    return
                }

                val quiz = mapQuizSummary(data)
                if (quiz == null) {
                    onError("Unable to parse quiz")
                    return
                }
                onSuccess(quiz)
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                onError(t.message ?: "Network error")
            }
        })
    }

    fun createQuiz(
        quiz: Quiz,
        onSuccess: (Quiz) -> Unit,
        onError: (String) -> Unit
    ) {
        onError("Create quiz API wiring is not implemented in this app yet.")
    }

    fun notifyQuizComplete(quizId: Int) {
        // TODO: Connect to attempts API when quiz-play flow is fully API-backed.
    }

    // ══════════════════════════════════════════════════════════════════════
    //  USER PROFILE
    // ══════════════════════════════════════════════════════════════════════

    fun getProfile(
        onSuccess: (Map<String, Any>) -> Unit,
        onError: (String) -> Unit
    ) {
        val bearer = bearer()
        if (bearer == null) {
            onError("Not authenticated")
            return
        }

        api.getProfile(bearer).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (!response.isSuccessful || response.body() == null) {
                    onError(parseError(response))
                    return
                }

                val body = response.body()!!
                val result = mutableMapOf<String, Any>()
                body.entrySet().forEach { (key, value) ->
                    if (value.isJsonPrimitive) {
                        val primitive = value.asJsonPrimitive
                        when {
                            primitive.isString -> result[key] = primitive.asString
                            primitive.isBoolean -> result[key] = primitive.asBoolean
                            primitive.isNumber -> result[key] = primitive.asNumber
                        }
                    }
                }
                onSuccess(result)
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                onError(t.message ?: "Network error")
            }
        })
    }

    fun updateProfile(
        name: String, email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        onError("Update profile endpoint is not available in backend routes.")
    }

    fun changePassword(
        current: String, newPw: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val bearer = bearer()
        if (bearer == null) {
            onError("Not authenticated")
            return
        }

        api.changePassword(
            bearer,
            mapOf(
                "current_password" to current,
                "new_password" to newPw,
                "new_password_confirmation" to newPw
            )
        ).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (!response.isSuccessful) {
                    onError(parseError(response))
                    return
                }
                onSuccess()
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                onError(t.message ?: "Network error")
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
            creator = o.getAsJsonObject("user")?.get("name")?.asString ?: "Unknown",
            creatorName = o.getAsJsonObject("user")?.get("name")?.asString ?: "Unknown",
            questions = emptyList(),
            subject = o.get("subject")?.asString ?: "General",
            gradeLevel = o.get("grade_level")?.asString ?: "all",
            code = o.get("quiz_code")?.asString ?: ""
        )
    }
}
