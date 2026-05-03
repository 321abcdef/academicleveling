package com.example.academicleveling.ui.timer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.academicleveling.data.AppState
import kotlinx.coroutines.*

// Only POMODORO (focus) gives XP/coins. Breaks are just breaks.
enum class TimerMode(val label: String, val seconds: Int, val isFocus: Boolean) {
    POMODORO   ("Pomodoro",    1 * 60, true),
    SHORT_BREAK("Short Break",  5 * 60, false),
    LONG_BREAK ("Long Break",  15 * 60, false)
}

/**
 * TimerState — singleton that survives tab switches within the app.
 *
 * Rewards are ONLY given for POMODORO (focus) sessions.
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
                showDone = true
                
                // Only reward focus sessions (runs in background)
                if (mode.isFocus) {
                    val mins = totalSecs / 60
                    val estExp = mins
                    
                    // Optimistically update local session count
                    sessionsToday++
                    xpThisSession += estExp
                    
                    AppState.addStudySession(totalSecs) { earnedExp, _ ->
                        // Sync with actual rewards if they differ
                        // xpThisSession = (xpThisSession - estExp) + earnedExp
                    }
                }
            }
        }
    }
}