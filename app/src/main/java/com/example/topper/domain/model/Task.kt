package com.example.topper.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Task (
    val name:String,
    val description:String,
    val dueDate:Long,
    val priority : Int,
    val relatedTosubject:String,
    val isComplete: Boolean,
    @PrimaryKey(autoGenerate = true)
    val taskId:Int? = null,
    val taskSubjectId:Int
)