package com.example.topper.domain.repositroy

import com.example.topper.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {

    suspend fun upsertTask(task:Task)

    suspend fun getTaskById(taskID:Int):Task?

    suspend fun deleteTask(taskID:Int)

    fun getUpcomingTasksForSubject(subjectId: Int): Flow<List<Task>>

    fun getCompletedTasksForSubject(subjectId: Int): Flow<List<Task>>

    fun getAllUpcomingTasks(): Flow<List<Task>>
}