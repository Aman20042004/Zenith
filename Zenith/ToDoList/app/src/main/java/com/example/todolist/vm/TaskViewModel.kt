package com.example.todolist.vm

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.*
import com.example.todolist.timer.SingleTimerManager
import com.example.todolist.alarm.NotificationHelper
import com.example.todolist.alarm.AlarmScheduler
import com.example.todolist.ui.util.getMillisFromTimeString
import com.example.todolist.ui.util.normalizeAlarmTime
import kotlinx.coroutines.launch
import java.util.Calendar

class TaskViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = TaskRepository(
        TaskDatabase.get(app).dao()
    )

    val tasks = repo.tasks

    init {
        syncTimersOnLaunch()
    }

    private fun syncTimersOnLaunch() {
        viewModelScope.launch {
            repo.getAllTasksOnce().forEach { task ->
                if (task.state == TaskState.RUNNING) {
                    val now = System.currentTimeMillis()
                    val deadlineMillis = task.alarmTime?.let { getMillisFromTimeString(it) }

                    if (deadlineMillis != null && now >= deadlineMillis) {
                        val diffToDeadline = ((deadlineMillis - task.lastStartTimeMillis) / 1000).toInt()
                        val finalElapsed = (task.elapsedSec + diffToDeadline).coerceAtLeast(task.elapsedSec)

                        repo.update(task.copy(
                            elapsedSec = finalElapsed,
                            state = TaskState.DONE,
                            lastStartTimeMillis = now
                        ))
                    } else {
                        val secondsPassed = ((now - task.lastStartTimeMillis) / 1000).toInt()
                        val newElapsed = task.elapsedSec + secondsPassed
                        val maxSec = task.durationSec ?: 0
                        val isFinished = task.durationSec != null && newElapsed >= maxSec

                        val syncedTask = task.copy(
                            elapsedSec = if (isFinished) maxSec else newElapsed,
                            state = if (isFinished) TaskState.DONE else TaskState.RUNNING,
                            lastStartTimeMillis = now
                        )
                        repo.update(syncedTask)

                        if (!isFinished) {
                            startTimerManager(syncedTask)
                        }
                    }
                }
            }
        }
    }

    fun addTask(title: String, durationSec: Int?, alarmTime: Long?) = viewModelScope.launch {
        val alarmStringForDb = alarmTime?.let {
            val calendar = Calendar.getInstance().apply { timeInMillis = it }
            String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        }

        val normalizedAlarmLong = alarmTime?.let { normalizeAlarmTime(it) }

        val task = TaskEntity(
            title = title,
            durationSec = durationSec,
            alarmTime = alarmStringForDb,
            lastStartTimeMillis = 0L
        )

        val id = repo.insert(task).toInt()

        if (normalizedAlarmLong != null) {
            AlarmScheduler.schedule(getApplication(), id, title, normalizedAlarmLong)
        }
    }

    fun start(task: TaskEntity) {
        viewModelScope.launch {
            repo.getAllTasksOnce().forEach { existingTask ->
                if (existingTask.state == TaskState.RUNNING && existingTask.id != task.id) {
                    pause(existingTask)
                }
            }

            if (task.durationSec != null) {
                val remainingSec = task.durationSec - task.elapsedSec
                if (remainingSec > 0) {
                    AlarmScheduler.schedule(
                        getApplication(),
                        taskId = task.id + 10000,
                        title = "${task.title} - Timer Complete!",
                        timeMillis = System.currentTimeMillis() + (remainingSec * 1000L)
                    )
                }
            }

            val now = System.currentTimeMillis()
            val updatedTask = task.copy(state = TaskState.RUNNING, lastStartTimeMillis = now)
            repo.update(updatedTask)
            startTimerManager(updatedTask)
        }
    }

    private fun startTimerManager(task: TaskEntity) {
        SingleTimerManager.start(
            getApplication(),
            task.id,
            task.title,
            task.durationSec ?: 0,
            task.elapsedSec
        )
    }

    fun pause(task: TaskEntity) {
        // getApplication() as the context parameter
        SingleTimerManager.pause(getApplication(), task.id)
        AlarmScheduler.cancel(getApplication(), task.id + 10000)
        val manager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(5000)

        viewModelScope.launch {
            repo.update(task.copy(state = TaskState.PAUSED))
        }
    }

    fun done(task: TaskEntity) {

        SingleTimerManager.stop(getApplication(), task.id)
        AlarmScheduler.cancel(getApplication(), task.id)
        AlarmScheduler.cancel(getApplication(), task.id + 10000)

        val manager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(5000)
        viewModelScope.launch {
            var finalElapsed = task.elapsedSec
            if (task.state == TaskState.RUNNING) {
                val now = System.currentTimeMillis()
                val secondsPassed = ((now - task.lastStartTimeMillis) / 1000).toInt()
                finalElapsed += secondsPassed

                val maxSec = task.durationSec ?: 0
                if (maxSec > 0 && finalElapsed > maxSec) {
                    finalElapsed = maxSec
                }
            }

            repo.update(task.copy(
                state = TaskState.DONE,
                elapsedSec = finalElapsed
            ))
        }
    }

    fun delete(task: TaskEntity) {

        SingleTimerManager.stop(getApplication(), task.id)
        AlarmScheduler.cancel(getApplication(), task.id)
        AlarmScheduler.cancel(getApplication(), task.id + 10000)

        val manager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(5000)

        viewModelScope.launch {
            repo.delete(task)
        }
    }
}