package com.example.academicleveling.ui.timer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.academicleveling.data.ApiRepository
import com.example.academicleveling.data.AppState
import com.example.academicleveling.data.CreateStudySessionRequest
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

// Only POMODORO (focus) gives XP/coins. Breaks are just breaks.
enum class TimerMode(val label: String, val seconds: Int, val isFocus: Boolean) {
    POMODORO   ("Pomodoro",    25 * 60, true),
    SHORT_BREAK("Short Break",  5 * 60, false),
    LONG_BREAK ("Long Break",  15 * 60, false)
}

/**
 * TimerState — singleton that survives tab switches within the app.
 *
 * Rewards come from the API (CreateStudySessionResponse.data.rewards).
 * Short Break and Long Break do NOT give XP or coins.
 */
object TimerState {
    var mode          by mutableStateOf(TimerMode.POMODORO)
    var totalSecs     by mutableStateOf(TimerMode.POMODORO.seconds)
    var remaining     by mutableStateOf(TimerMode.POMODORO.seconds)
    var running       by mutableStateOf(false)
    var sessionsToday by mutableStateOf(0)
    var xpThisSession by mutableStateOf(0)
    var showDone      by mutableStateOf(false)

    private var job: Job? = null

    fun updateMode(m: TimerMode) {
        if (running) return
        mode = m; totalSecs = m.seconds; remaining = m.seconds
    }

    fun reset() {
        job?.cancel(); job = null
        running = false; remaining = totalSecs
    }

    fun toggleRunning() {
        if (running) pause() else start()
    }

    private fun pause() {
        job?.cancel(); job = null; running = false
    }

    private fun start() {
        if (remaining <= 0) remaining = totalSecs
        running = true
        job = GlobalScope.launch {
            while (remaining > 0 && running) {
                delay(1000L)
                if (running) remaining--
            }
            if (remaining == 0 && running) {
                running = false
                // Only reward focus (Pomodoro) sessions
                if (mode.isFocus) {
                    val mins      = totalSecs / 60
                    val sessionAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        .format(Date())

                    ApiRepository.createStudySession(
                        durationSeconds = totalSecs,
                        sessionAt       = sessionAt,
                        onSuccess       = { response ->
                            val rewards = response.data.rewards
                            val exp     = rewards?.exp   ?: 0
                            val coins   = rewards?.coins ?: 0
                            // Apply rewards from API response
                            AppState.addStudySession(
                                mins        = mins,
                                expFromApi   = exp,
                                coinsFromApi = coins
                            )
                            sessionsToday++
                            xpThisSession += exp
                        },
                        onError = {
                            // Fallback: compute locally if API fails
                            val fallbackExp   = mins * 2
                            val fallbackCoins = mins
                            AppState.addStudySession(
                                mins        = mins,
                                expFromApi   = fallbackExp,
                                coinsFromApi = fallbackCoins
                            )
                            sessionsToday++
                            xpThisSession += fallbackExp
                        }
                    )
                }
                showDone = true
            }
        }
    }
}