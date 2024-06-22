package com.example.topper.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Session (
    val sessionSubjectId: Int,
    val relatedToSubject:String,
    @PrimaryKey(autoGenerate = true)
    val sessionID:Int? = null,
    val date:Long,
    val duration: Long,
)