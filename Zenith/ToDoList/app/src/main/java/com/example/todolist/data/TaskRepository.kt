package com.example.todolist.data

class TaskRepository(private val dao: TaskDao) {

    val tasks = dao.getAll()

    suspend fun insert(task: TaskEntity): Long {
        return dao.insert(task)
    }

    suspend fun update(task: TaskEntity) {
        dao.update(task)
    }

    suspend fun delete(task: TaskEntity) {
        dao.delete(task)
    }
    suspend fun getAllTasksOnce() = dao.getAllTasksOnce()
}