package com.example.todolist.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    private const val ALARM_CHANNEL_ID = "task_alarm_channel"
    private const val PROGRESS_CHANNEL_ID = "progress_channel"

    // YAHAN FIXED ID DECLARE KI HAI TAKI OVERLAP NA HO
    const val PROGRESS_NOTIF_ID = 5000

    fun showAlarmNotification(context: Context, taskId: Int, title: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(ALARM_CHANNEL_ID, "Task Alarms", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val stopIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "STOP_ALARM"
            putExtra("TASK_ID", taskId)
        }

        val stopPendingIntent = PendingIntent.getBroadcast(
            context, taskId, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Time's Up!")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .build()

        manager.notify(taskId, notification) // Alarm ki ID alag hi rahegi
    }

    fun buildProgressNotification(context: Context, taskId: Int, title: String, progress: Int, isRunning: Boolean): android.app.Notification {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(PROGRESS_CHANNEL_ID, "Task Progress", NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(channel)
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = launchIntent?.let {
            PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val actionIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = if (isRunning) "ACTION_PAUSE_TIMER" else "ACTION_RESUME_TIMER"
            putExtra("TASK_ID", taskId)
            putExtra("TASK_TITLE", title)
            putExtra("TASK_PROGRESS", progress)
        }
        val actionPendingIntent = PendingIntent.getBroadcast(
            context, taskId + 20000, actionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val actionIcon = if (isRunning) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val actionTitle = if (isRunning) "Pause" else "Resume"
        val statusText = if (isRunning) "$progress% completed" else "$progress% completed (Paused)"

        return NotificationCompat.Builder(context, PROGRESS_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("Working on: $title")
            .setContentText(statusText)
            .setProgress(100, progress, false)
            .setOngoing(isRunning)
            .setOnlyAlertOnce(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(actionIcon, actionTitle, actionPendingIntent)
            .build()
    }

    fun showProgressNotification(context: Context, taskId: Int, title: String, progress: Int, isRunning: Boolean = true) {
        val notification = buildProgressNotification(context, taskId, title, progress, isRunning)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // HAMESHA FIXED ID USE HOGI
        manager.notify(PROGRESS_NOTIF_ID, notification)
    }
}