package com.example.todolist.data
// In TaskEntity.kt
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val durationSec: Int? = null, // stores total seconds instead of minutes
    val alarmTime: String? = null,
    val elapsedSec: Int = 0,
    val state: TaskState = TaskState.IDLE,
    val lastStartTimeMillis: Long = 0L
)