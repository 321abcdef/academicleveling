package com.example.academicleveling.data

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val login: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val data: UserData
)

data class UserData(
    val id: Int,
    val username: String,
    val email: String,
    val progress: UserProgress,
    val coins: Int,
    @SerializedName("total_exp") val totalExp: Int?
)

data class UserProgress(
    val level: Int,
    @SerializedName("current_exp") val currentExp: Int,
    @SerializedName("exp_to_next_level") val expToNextLevel: Int,
    @SerializedName("progress_percent") val progressPercent: Double
)

data class LogoutResponse(
    val message: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String
)

data class RegisterResponse(
    val message: String,
    val token: String,
    val data: UserData
)

data class ApiErrorResponse(
    val message: String,
    val errors: Map<String, List<String>>? = null
)
