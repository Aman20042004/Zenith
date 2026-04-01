package com.example.todolist.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.todolist.data.TaskDatabase
import com.example.todolist.data.TaskState
import com.example.todolist.timer.SingleTimerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val taskId = intent.getIntExtra("TASK_ID", 0)
        val title = intent.getStringExtra("TASK_TITLE") ?: "Task Alert"
        val progress = intent.getIntExtra("TASK_PROGRESS", 0)

        if (action == "STOP_ALARM") {
            AlarmAudioManager.stopAlarm()
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(5000)
            return
        }

        if (action == "ACTION_PAUSE_TIMER") {
            SingleTimerManager.pause(context, taskId)
            AlarmScheduler.cancel(context, taskId + 10000)

            NotificationHelper.showProgressNotification(context, taskId, title, progress, isRunning = false)

            CoroutineScope(Dispatchers.IO).launch {
                val dao = TaskDatabase.get(context).dao()
                val task = dao.getTaskById(taskId)
                if (task != null) {
                    dao.update(task.copy(state = TaskState.PAUSED))
                }
            }
            return
        }

        if (action == "ACTION_RESUME_TIMER") {
            NotificationHelper.showProgressNotification(context, taskId, title, progress, isRunning = true)

            CoroutineScope(Dispatchers.IO).launch {
                val dao = TaskDatabase.get(context).dao()
                val task = dao.getTaskById(taskId)

                if (task != null) {
                    val now = System.currentTimeMillis()
                    val updatedTask = task.copy(state = TaskState.RUNNING, lastStartTimeMillis = now)
                    dao.update(updatedTask)

                    if (updatedTask.durationSec != null) {
                        val remainingSec = updatedTask.durationSec - updatedTask.elapsedSec
                        if (remainingSec > 0) {
                            AlarmScheduler.schedule(
                                context,
                                taskId = updatedTask.id + 10000,
                                title = "${updatedTask.title} - Timer Complete!",
                                timeMillis = now + (remainingSec * 1000L)
                            )
                        }
                    }

                    SingleTimerManager.start(
                        context,
                        updatedTask.id,
                        updatedTask.title,
                        updatedTask.durationSec ?: 0,
                        updatedTask.elapsedSec
                    )
                }
            }
            return
        }

        // --- HARD DEADLINE & NORMAL ALARM LOGIC ---

        val isTimerCompletion = taskId >= 10000
        val realTaskId = if (isTimerCompletion) taskId - 10000 else taskId

        CoroutineScope(Dispatchers.IO).launch {
            val dao = TaskDatabase.get(context).dao()
            val task = dao.getTaskById(realTaskId)

            if (task != null && task.state != TaskState.DONE) {
                SingleTimerManager.stop(context, realTaskId)

                var finalElapsed = task.elapsedSec
                if (task.state == TaskState.RUNNING) {
                    val now = System.currentTimeMillis()
                    val diff = ((now - task.lastStartTimeMillis) / 1000).toInt()
                    finalElapsed += diff

                    val maxSec = task.durationSec ?: 0
                    if (maxSec > 0 && finalElapsed > maxSec) {
                        finalElapsed = maxSec
                    }
                }

                dao.update(task.copy(state = TaskState.DONE, elapsedSec = finalElapsed))

                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(5000)

                //If timer is completed early, than cancel the main deadline alarm
                // If deadline reached early, than cancel the remaining alarm
                if (isTimerCompletion) {
                    AlarmScheduler.cancel(context, realTaskId)
                } else {
                    AlarmScheduler.cancel(context, realTaskId + 10000)
                }

                // Runs only when task is pending
                AlarmAudioManager.playAlarm(context)
                NotificationHelper.showAlarmNotification(context, taskId, title)
            }
        }
    }
}