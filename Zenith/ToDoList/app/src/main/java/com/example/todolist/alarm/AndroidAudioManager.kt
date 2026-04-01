package com.example.todolist.alarm


import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager

object AlarmAudioManager {
    private var ringtone: Ringtone? = null

    fun playAlarm(context: Context) {
        // Prevent multiple instances from playing at once
        if (ringtone?.isPlaying == true) return

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Use applicationContext to avoid memory leaks
        ringtone = RingtoneManager.getRingtone(context.applicationContext, uri)
        ringtone?.play()
    }

    fun stopAlarm() {
        if (ringtone?.isPlaying == true) {
            ringtone?.stop()
        }
        ringtone = null
    }
}