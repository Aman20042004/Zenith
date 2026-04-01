package com.example.todolist.timer

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.todolist.alarm.NotificationHelper
import com.example.todolist.data.TaskDatabase
import com.example.todolist.data.TaskState
import kotlinx.coroutines.*

class TimerForegroundService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var timerJob: Job? = null
    private var currentTaskId: Int = -1

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            "ACTION_START" -> {
                val taskId = intent.getIntExtra("TASK_ID", -1)
                val title = intent.getStringExtra("TASK_TITLE") ?: ""
                val durationSec = intent.getIntExtra("DURATION_SEC", 0)
                val elapsedSec = intent.getIntExtra("ELAPSED_SEC", 0)

                if (taskId != -1) {
                    startTimer(taskId, title, durationSec, elapsedSec)
                }
            }
            "ACTION_PAUSE" -> {
                stopTimer()
                stopForeground(STOP_FOREGROUND_DETACH)
                stopSelf()
            }
            "ACTION_STOP" -> {
                stopTimer()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startTimer(taskId: Int, title: String, durationSec: Int, startElapsed: Int) {
        if (currentTaskId == taskId && timerJob?.isActive == true) return
        timerJob?.cancel()
        currentTaskId = taskId

        timerJob = serviceScope.launch {
            var elapsed = startElapsed
            val dao = TaskDatabase.get(applicationContext).dao()

            while (isActive) {
                val progress = if (durationSec > 0) (elapsed * 100 / durationSec).coerceIn(0, 100) else 0

                val notification = NotificationHelper.buildProgressNotification(
                    applicationContext, taskId, title, progress, isRunning = true
                )


                startForeground(NotificationHelper.PROGRESS_NOTIF_ID, notification)

                val task = dao.getTaskById(taskId)
                if (task != null && task.state == TaskState.RUNNING) {
                    dao.update(task.copy(
                        elapsedSec = elapsed,
                        lastStartTimeMillis = System.currentTimeMillis()
                    ))
                }

                if (durationSec in 1..elapsed) {
                    stopForeground(STOP_FOREGROUND_DETACH)
                    stopSelf()
                    break
                }

                delay(1000)
                elapsed++
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        currentTaskId = -1
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}