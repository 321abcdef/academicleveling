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

data class UserResponse(
    val data: UserData
)

data class UpdateProfileRequest(
    val username: String,
    val email: String
)

data class UpdateProfileResponse(
    val data: UserData,
    val message: String
)

data class UserStatsResponse(
    @SerializedName("total_study_duration_minutes") val totalStudyDurationMinutes: Int,
    @SerializedName("total_quizzes_completed") val totalQuizzesCompleted: Int,
    val streak: Int
)

data class ChangePasswordRequest(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String,
    @SerializedName("new_password_confirmation") val newPasswordConfirmation: String
)

data class ChangePasswordResponse(
    val message: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ForgotPasswordResponse(
    val message: String
)

data class ResetPasswordRequest(
    val email: String,
    val password:  String,
    @SerializedName("password_confirmation") val passwordConfirmation: String,
    val token: String
)

data class ResetPasswordResponse(
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

// ── Quizzes ─────────────────────────────────────────────────────────────

data class QuizCreator(
    val id: Int,
    val name: String
)

data class QuizApiData(
    val id: Int,
    val user: QuizCreator,
    @SerializedName("quiz_code") val quizCode: String,
    val title: String,
    val description: String,
    val subject: String,
    @SerializedName("grade_level") val gradeLevel: String,
    val type: String,
    val difficulty: String,
    @SerializedName("timer_mode") val timerMode: String,
    @SerializedName("is_question_shuffled") val isQuestionShuffled: Boolean,
    @SerializedName("is_choices_shuffled") val isChoicesShuffled: Boolean,
    @SerializedName("is_public") val isPublic: Boolean,
    @SerializedName("questions_count") val questionsCount: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class PaginationLink(
    val url: String?,
    val label: String,
    val active: Boolean
)

data class PaginationMeta(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page") val lastPage: Int,
    val from: Int?,
    val to: Int?,
    val total: Int,
    val path: String,
    @SerializedName("per_page") val perPage: Int,
    val links: List<PaginationLink>
)

data class QuizListResponse(
    val data: List<QuizApiData>,
    val meta: PaginationMeta? = null
)

data class ChoiceApiData(
    val id: Int,
    @SerializedName("choice_text") val choiceText: String,
    @SerializedName("is_correct") val isCorrect: Boolean
)

data class QuestionApiData(
    val id: Int,
    @SerializedName("question_text") val questionText: String,
    val type: String,
    @SerializedName("correct_answer") val correctAnswer: String?,
    val points: Int,
    val order: Int,
    val choices: List<ChoiceApiData>
)

data class QuizFullData(
    val id: Int,
    val user: QuizCreator,
    @SerializedName("quiz_code") val quizCode: String,
    val title: String,
    val description: String,
    val subject: String,
    @SerializedName("grade_level") val gradeLevel: String,
    val type: String,
    val difficulty: String,
    @SerializedName("timer_mode") val timerMode: String,
    @SerializedName("is_question_shuffled") val isQuestionShuffled: Boolean,
    @SerializedName("is_choices_shuffled") val isChoicesShuffled: Boolean,
    @SerializedName("is_public") val isPublic: Boolean,
    @SerializedName("questions_count") val questionsCount: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val questions: List<QuestionApiData>
)

data class QuizFullResponse(
    val data: QuizFullData
)

// ── Quiz Attempts ───────────────────────────────────────────────────────

data class StartAttemptResponse(
    val message: String,
    @SerializedName("attempt_id") val attemptId: Int
)

data class SubmitAnswerItem(
    @SerializedName("question_id") val questionId: Int,
    @SerializedName("choice_id") val choiceId: Int? = null,
    @SerializedName("answer_text") val answerText: String? = null
)

data class SubmitQuizRequest(
    val answers: List<SubmitAnswerItem>
)

data class SubmitQuizResponse(
    val message: String,
    val data: SubmitQuizResultData
)

data class SubmitQuizResultData(
    val score: Int,
    @SerializedName("total_questions") val totalQuestions: Int,
    @SerializedName("correct_answers") val correctAnswers: Int,
    val rewards: SubmitQuizRewards
)

data class SubmitQuizRewards(
    val exp: Int,
    val coins: Int
)

// ── Attempts History ─────────────────────────────────────────────────────

data class AttemptQuizData(
    val id: Int,
    @SerializedName("quiz_code") val quizCode: String,
    val title: String,
    val description: String,
    val subject: String,
    @SerializedName("grade_level") val gradeLevel: String,
    val type: String,
    val difficulty: String,
    @SerializedName("timer_mode") val timerMode: String,
    @SerializedName("is_question_shuffled") val isQuestionShuffled: Boolean,
    @SerializedName("is_choices_shuffled") val isChoicesShuffled: Boolean,
    @SerializedName("is_public") val isPublic: Boolean,
    @SerializedName("questions_count") val questionsCount: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class AttemptAnswerData(
    @SerializedName("question_text") val questionText: String,
    @SerializedName("correct_answer") val correctAnswer: String,
    @SerializedName("answer_text") val answerText: String?,
    @SerializedName("is_correct") val isCorrect: Boolean
)

data class AttemptData(
    val id: Int,
    val score: Int,
    @SerializedName("started_at") val startedAt: String,
    @SerializedName("completed_at") val completedAt: String?,
    val quiz: AttemptQuizData,
    val answers: List<AttemptAnswerData>? = null
)

data class AttemptListResponse(
    val data: List<AttemptData>,
    val meta: PaginationMeta? = null
)

data class AttemptDetailResponse(
    val data: AttemptData
)
