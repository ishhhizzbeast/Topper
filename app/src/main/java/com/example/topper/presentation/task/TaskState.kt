package com.example.topper.presentation.task

import com.example.topper.domain.model.Subject
import com.example.topper.util.Priority

data class TaskState (
    val title : String ="",
    val description:String="",
    val dueDate : Long? = null,
    val isTaskCompleted : Boolean = false,
    val priority: Priority = Priority.LOW,
    val relatedToSubject:String? = null,
    val subject:List<Subject> = emptyList(),
    val subjectId:Int? = null,
    val currentTaskId :Int? = null
)