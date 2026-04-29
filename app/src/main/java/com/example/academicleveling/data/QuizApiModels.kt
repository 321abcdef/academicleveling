package com.example.academicleveling.data

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

// ══════════════════════════════════════════════════════════════════════
//  LIST / COMMON
// ══════════════════════════════════════════════════════════════════════

data class QuizListResponse(
    val data: List<QuizApiData>,
    val links: PaginationLinks,
    val meta: PaginationMeta
)

data class QuizApiData(
    val id: Int,
    val user: QuizUser,
    @SerializedName("quiz_code") val quizCode: String,
    val title: String,
    val description: String?,
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
    val questions: List<QuestionApiData>? = null
)

data class QuizUser(
    val id: Int,
    val name: String
)

data class QuestionApiData(
    val id: Int? = null,
    @SerializedName("question_text") val questionText: String,
    val type: String,
    @SerializedName("correct_answer") val correctAnswer: String? = null,
    val points: Int,
    val order: Int,
    val choices: List<ChoiceApiData> = emptyList()
)

data class ChoiceApiData(
    val id: Int? = null,
    @SerializedName("choice_text") val choiceText: String,
    @SerializedName("is_correct") val isCorrect: Boolean
)

data class PaginationLinks(
    val first: String?,
    val last: String?,
    val prev: String?,
    val next: String?
)

data class PaginationMeta(
    @SerializedName("current_page") val currentPage: Int,
    val from: Int?,
    @SerializedName("last_page") val lastPage: Int,
    val links: List<MetaLink>,
    val path: String,
    @SerializedName("per_page") val perPage: Int,
    val to: Int?,
    val total: Int
)

data class MetaLink(
    val url: String?,
    val label: String,
    val active: Boolean
)

// ══════════════════════════════════════════════════════════════════════
//  DETAILS
// ══════════════════════════════════════════════════════════════════════

data class QuizDetailsResponse(
    val data: QuizApiData
)

data class StartQuizAttemptResponse(
    val message: String,
    @SerializedName("attempt_id") val attemptId: Int
)

data class SubmitAttemptAnswersRequest(
    val answers: List<Map<String, Any?>>
)

data class SubmitAttemptAnswersResponse(
    val message: String
)

data class SubmitAttemptResponse(
    val message: String
)

data class SubmitAllAttemptResponse(
    val message: String,
    val data: SubmitAllAttemptData
)

data class SubmitAllAttemptData(
    val score: Int,
    @SerializedName("total_questions") val totalQuestions: Int,
    @SerializedName("correct_answers") val correctAnswers: Int,
    val rewards: AttemptRewards
)

data class AttemptRewards(
    val exp: Int,
    val coins: Int
)

data class AttemptListResponse(
    val data: List<AttemptApiData>,
    val links: PaginationLinks,
    val meta: PaginationMeta
)

data class AttemptApiData(
    val id: Int,
    val score: Int,
    @SerializedName("started_at") val startedAt: String?,
    @SerializedName("completed_at") val completedAt: String?,
    val quiz: AttemptQuizApiData
)

data class AttemptQuizApiData(
    val id: Int,
    @SerializedName("quiz_code") val quizCode: String,
    val title: String,
    val description: String?,
    val subject: String,
    @SerializedName("grade_level") val gradeLevel: String,
    val type: String,
    val difficulty: String,
    @SerializedName("timer_mode") val timerMode: String,
    @SerializedName("is_question_shuffled") val isQuestionShuffled: Boolean,
    @SerializedName("is_choices_shuffled") val isChoicesShuffled: Boolean,
    @SerializedName("is_public") val isPublic: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class AttemptDetailsResponse(
    val data: JsonObject
)

data class StudySessionsResponse(
    val data: List<StudySessionApiData>
)

data class StudySessionApiData(
    val id: Int,
    @SerializedName("session_at") val sessionAt: String,
    val duration: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class CreateStudySessionRequest(
    val duration: Int,
    @SerializedName("session_at") val sessionAt: String
)

data class CreateStudySessionResponse(
    val data: CreateStudySessionData,
    val message: String
)

data class CreateStudySessionData(
    val id: Int,
    @SerializedName("session_at") val sessionAt: String,
    val duration: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val rewards: AttemptRewards
)

// ══════════════════════════════════════════════════════════════════════
//  CREATE
// ══════════════════════════════════════════════════════════════════════

data class CreateQuizRequest(
    val title: String,
    val description: String?,
    val subject: String,
    @SerializedName("grade_level") val gradeLevel: String,
    val type: String,
    val difficulty: String,
    @SerializedName("timer_mode") val timerMode: String,
    @SerializedName("is_question_shuffled") val isQuestionShuffled: Boolean,
    @SerializedName("is_choices_shuffled") val isChoicesShuffled: Boolean,
    @SerializedName("is_public") val isPublic: Boolean,
    val questions: List<CreateQuestionRequest>
)

data class CreateQuestionRequest(
    @SerializedName("question_text") val questionText: String,
    val type: String,
    val points: Int,
    val order: Int,
    val choices: List<CreateChoiceRequest>? = null,
    @SerializedName("correct_answer") val correctAnswer: String? = null
)

data class CreateChoiceRequest(
    @SerializedName("choice_text") val choiceText: String,
    @SerializedName("is_correct") val isCorrect: Boolean
)

data class CreateQuizResponse(
    val data: QuizApiData,
    val message: String
)
