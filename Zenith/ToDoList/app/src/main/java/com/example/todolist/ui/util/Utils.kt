package com.example.todolist.ui.util

import java.util.*

/**
 * 1. UI Display: "14:30" (String) -> "02:30 PM" (String)
 */
fun formatToAmPm(timeStr: String?): String {
    if (timeStr.isNullOrEmpty() || !timeStr.contains(":")) return "No Alarm"

    return try {
        val parts = timeStr.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        val suffix = if (hour >= 12) "PM" else "AM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, suffix)
    } catch (e: Exception) {
        timeStr ?: "No Alarm"
    }
}

/**
 * 2. Hard Deadline Check: "14:30" (String) -> Future Milliseconds (Long)
 */
fun getMillisFromTimeString(timeStr: String): Long {
    val parts = timeStr.split(":")
    val hour = parts[0].toInt()
    val minute = parts[1].toInt()

    val now = Calendar.getInstance()
    val deadline = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    if (deadline.before(now)) {
        deadline.add(Calendar.DAY_OF_YEAR, 1)
    }

    return deadline.timeInMillis
}

/**
 * 3. Alarm Scheduler: Long (millis) -> Future Long (millis)
 */
fun normalizeAlarmTime(selectedMillis: Long): Long {
    val now = Calendar.getInstance()
    val alarm = Calendar.getInstance().apply {
        timeInMillis = selectedMillis
    }

    if (alarm.before(now)) {
        alarm.add(Calendar.DAY_OF_YEAR, 1)
    }

    return alarm.timeInMillis
}

/**
 * 4. Timer Display: Seconds -> "mm:ss"
 */
fun formatElapsed(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
}

/**
 * 5. Duration Display: Int -> "25 min"
 */
fun formatDurationMin(minutes: Int): String {
    return "$minutes min"
}


/**
 * Timer Display: Total Seconds -> "01h 15m 30s" or "25m 00s"
 */
fun formatDurationSec(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60

    return if (h > 0) {
        String.format("%02dh %02dm %02ds", h, m, s)
    } else {
        String.format("%02dm %02ds", m, s)
    }
}