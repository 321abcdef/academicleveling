package com.example.academicleveling.data

import com.google.gson.annotations.SerializedName

// ══════════════════════════════════════════════════════════════════════
//  AUTH
// ══════════════════════════════════════════════════════════════════════

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
    @SerializedName("current_exp")        val currentExp: Int,
    @SerializedName("exp_to_next_level")  val expToNextLevel: Int,
    @SerializedName("progress_percent")   val progressPercent: Double
)

data class LogoutResponse(val message: String)

data class UserResponse(val data: UserData)

data class UpdateProfileRequest(
    val username: String,
    val email: String
)

data class UpdateProfileResponse(
    val data: UserData,
    val message: String
)

data class ChangePasswordRequest(
    @SerializedName("current_password")          val currentPassword: String,
    @SerializedName("new_password")              val newPassword: String,
    @SerializedName("new_password_confirmation") val newPasswordConfirmation: String
)

data class ChangePasswordResponse(val message: String)

data class ForgotPasswordRequest(val email: String)

data class ForgotPasswordResponse(val message: String)

data class ResetPasswordRequest(
    val email: String,
    val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String,
    val token: String
)

data class ResetPasswordResponse(val message: String)

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

// ══════════════════════════════════════════════════════════════════════
//  PAGINATION
// ══════════════════════════════════════════════════════════════════════

data class PaginationMeta(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page")    val lastPage: Int,
    @SerializedName("per_page")     val perPage: Int,
    val total: Int?,
    val from: Int?,
    val to: Int?
)

data class PaginationLinks(
    val first: String?,
    val last: String?,
    val prev: String?,
    val next: String?
)

// ══════════════════════════════════════════════════════════════════════
//  QUIZZES
// ══════════════════════════════════════════════════════════════════════

data class QuizUser(
    val id: Int,
    val name: String
)

data class QuizSummary(
    val id: Int,
    val user: QuizUser?,
    @SerializedName("quiz_code")            val quizCode: String?,
    val title: String,
    val description: String?,
    val subject: String?,
    @SerializedName("grade_level")          val gradeLevel: String?,
    val type: String?,
    val difficulty: String?,
    @SerializedName("timer_mode")           val timerMode: String?,
    @SerializedName("is_question_shuffled") val isQuestionShuffled: Boolean?,
    @SerializedName("is_choices_shuffled")  val isChoicesShuffled: Boolean?,
    @SerializedName("is_public")            val isPublic: Boolean?,
    @SerializedName("questions_count")      val questionsCount: Int?,
    @SerializedName("created_at")          val createdAt: String?,
    @SerializedName("updated_at")          val updatedAt: String?
)

data class QuizChoice(
    val id: Int,
    @SerializedName("choice_text") val choiceText: String,
    @SerializedName("is_correct")  val isCorrect: Boolean
)

data class ApiQuizQuestion(
    val id: Int,
    @SerializedName("question_text")  val questionText: String,
    val type: String,
    @SerializedName("correct_answer") val correctAnswer: String?,
    val points: Int,
    val order: Int,
    val choices: List<QuizChoice>
)

data class QuizDetail(
    val id: Int,
    val user: QuizUser?,
    @SerializedName("quiz_code")            val quizCode: String?,
    val title: String,
    val description: String?,
    val subject: String?,
    @SerializedName("grade_level")          val gradeLevel: String?,
    val type: String?,
    val difficulty: String?,
    @SerializedName("timer_mode")           val timerMode: String?,
    @SerializedName("is_question_shuffled") val isQuestionShuffled: Boolean?,
    @SerializedName("is_choices_shuffled")  val isChoicesShuffled: Boolean?,
    @SerializedName("is_public")            val isPublic: Boolean?,
    @SerializedName("questions_count")      val questionsCount: Int?,
    @SerializedName("created_at")           val createdAt: String?,
    @SerializedName("updated_at")           val updatedAt: String?,
    val questions: List<ApiQuizQuestion>?
)

// GET /api/quizzes
data class GetAllQuizzesResponse(
    val data: List<QuizSummary>,
    val links: PaginationLinks?,
    val meta: PaginationMeta?
)

// GET /api/quizzes/{id}, POST /api/quizzes, PUT /api/quizzes/{id}
data class SingleQuizResponse(val data: QuizDetail)

// ══════════════════════════════════════════════════════════════════════
//  ATTEMPTS
// ══════════════════════════════════════════════════════════════════════

// POST /api/quizzes/{id}/attempts
data class StartAttemptResponse(
    val message: String,
    @SerializedName("attempt_id") val attemptId: Int
)

// POST /api/attempts/{id}/submit
data class AttemptRewards(
    val exp: Int,
    val coins: Int
)

data class SubmitAttemptData(
    val score: Double,
    @SerializedName("total_questions") val totalQuestions: Int,
    @SerializedName("correct_answers") val correctAnswers: Int,
    val rewards: AttemptRewards
)

data class SubmitAttemptResponse(
    val message: String,
    val data: SubmitAttemptData
)

// GET /api/attempts/{id}
data class AttemptQuizSnapshot(
    val id: Int,
    @SerializedName("quiz_code")            val quizCode: String?,
    val title: String,
    val description: String?,
    val subject: String?,
    @SerializedName("grade_level")          val gradeLevel: String?,
    val type: String?,
    val difficulty: String?,
    @SerializedName("timer_mode")           val timerMode: String?,
    @SerializedName("is_question_shuffled") val isQuestionShuffled: Boolean?,
    @SerializedName("is_choices_shuffled")  val isChoicesShuffled: Boolean?,
    @SerializedName("is_public")            val isPublic: Boolean?,
    @SerializedName("created_at")           val createdAt: String?,
    @SerializedName("updated_at")           val updatedAt: String?
)

data class AttemptAnswerDetail(
    @SerializedName("question_id") val questionId: Int,
    @SerializedName("choice_id")   val choiceId: Int?,
    @SerializedName("answer_text") val answerText: String?,
    @SerializedName("is_correct")  val isCorrect: Boolean?
)

data class AttemptDetail(
    val id: Int,
    val score: Double?,
    @SerializedName("started_at")   val startedAt: String?,
    @SerializedName("completed_at") val completedAt: String?,
    val quiz: AttemptQuizSnapshot?,
    val answers: List<AttemptAnswerDetail>
)

// GET /api/attempts/{id}
data class SingleAttemptResponse(val data: AttemptDetail)

// GET /api/attempts
data class GetAllAttemptsResponse(
    val data: List<AttemptDetail>,
    val links: PaginationLinks?,
    val meta: PaginationMeta?
)

// ══════════════════════════════════════════════════════════════════════
//  STUDY SESSIONS
// ══════════════════════════════════════════════════════════════════════

data class StudySessionRewards(
    val exp: Int,
    val coins: Int
)

data class StudySessionData(
    val id: Int,
    @SerializedName("session_at")  val sessionAt: String,
    val duration: Int,
    @SerializedName("created_at")  val createdAt: String?,
    @SerializedName("updated_at")  val updatedAt: String?,
    val rewards: StudySessionRewards?
)

// GET /api/study-sessions
data class GetAllStudySessionsResponse(val data: List<StudySessionData>)

// POST /api/study-sessions
data class CreateStudySessionResponse(
    val data: StudySessionData,
    val message: String
)

// ══════════════════════════════════════════════════════════════════════
//  QUESTS
// ══════════════════════════════════════════════════════════════════════

data class QuestRewards(
    val exp: Int,
    val coins: Int,
    @SerializedName("claimed_at") val claimedAt: String?
)

data class QuestData(
    val id: Int,
    val title: String,
    val description: String,
    val type: String,
    val progress: Int,
    val target: Int,
    @SerializedName("completed_at") val completedAt: String?,
    val percentage: Int,
    val rewards: QuestRewards
)

data class QuestListData(
    val daily: List<QuestData>,
    val weekly: List<QuestData>
)

// GET /api/quests
data class GetAllQuestsResponse(val data: QuestListData)

// POST /api/quests/{id}/claim
data class ClaimQuestData(
    @SerializedName("exp_gained")   val expGained: Int,
    @SerializedName("coins_gained") val coinsGained: Int
)

data class ClaimQuestResponse(
    val message: String,
    val data: ClaimQuestData
)

// ══════════════════════════════════════════════════════════════════════
//  GENERIC
// ══════════════════════════════════════════════════════════════════════

data class MessageResponse(val message: String)