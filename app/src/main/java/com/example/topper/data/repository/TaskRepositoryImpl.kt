package com.example.topper.data.repository

import com.example.topper.data.local.TaskDao
import com.example.topper.domain.model.Task
import com.example.topper.domain.repositroy.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskdao:TaskDao
) : TaskRepository {
    override suspend fun upsertTask(task: Task) {
        taskdao.upsertTask(task)
    }

    override suspend fun getTaskById(taskID: Int): Task? {
        return taskdao.getTaskById(taskID)
    }

    override suspend fun deleteTask(taskID: Int) {
        taskdao.deleteTask(taskID)
    }

    override fun getUpcomingTasksForSubject(subjectId: Int): Flow<List<Task>>{
        return taskdao.getTasksForSubject(subjectId)
            .map {tasks->
                tasks.filter {task ->
                    !task.isComplete
                }
            }
            .map { tasks->
                sortTask(tasks)
            }
    }

    override fun getCompletedTasksForSubject(subjectId: Int): Flow<List<Task>> {
        return taskdao.getTasksForSubject(subjectId)
            .map {tasks->
                tasks.filter {task ->
                    task.isComplete
                }
            }
            .map { tasks->
                sortTask(tasks)
            }
    }

    override fun getAllUpcomingTasks(): Flow<List<Task>> {
        return taskdao.getAllTasks()
            .map { tasks->
                tasks.filter {
                    !it.isComplete
                }
            }
            .map { tasks->
                sortTask(tasks)
            }
    }
    private fun sortTask(tasks:List<Task>):List<Task>{
        return tasks.sortedWith(compareBy<Task>{
            it.dueDate
        }.thenByDescending {
            it.priority
        })
    }

}