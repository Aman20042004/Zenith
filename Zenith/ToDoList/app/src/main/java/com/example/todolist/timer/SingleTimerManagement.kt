package com.example.todolist.timer

import android.content.Context
import android.content.Intent
import android.os.Build

object SingleTimerManager {

    fun start(context: Context, taskId: Int, title: String, durationSec: Int, currentElapsed: Int) {
        val intent = Intent(context, TimerForegroundService::class.java).apply {
            action = "ACTION_START"
            putExtra("TASK_ID", taskId)
            putExtra("TASK_TITLE", title)
            putExtra("DURATION_SEC", durationSec)
            putExtra("ELAPSED_SEC", currentElapsed)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun pause(context: Context, taskId: Int) {
        val intent = Intent(context, TimerForegroundService::class.java).apply {
            action = "ACTION_PAUSE"
            putExtra("TASK_ID", taskId)
        }
        context.startService(intent)
    }

    // NAYA FUNCTION: Service aur notification ko completely kill karne ke liye
    fun stop(context: Context, taskId: Int) {
        val intent = Intent(context, TimerForegroundService::class.java).apply {
            action = "ACTION_STOP"
            putExtra("TASK_ID", taskId)
        }
        context.startService(intent)
    }
}