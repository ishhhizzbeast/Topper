package com.example.topper.presentation.Subject

import androidx.compose.ui.graphics.Color
import com.example.topper.domain.model.Session
import com.example.topper.domain.model.Subject
import com.example.topper.domain.model.Task

data class SubjectState(
    val currentSubjectId : Int? = null,
    val subjectName : String = "",
    val goalStudyHours : String = "",
    val selectedCardColors : List<Color> = Subject.subjectcardcolors.random(),
    val studiedHours : Float = 0f,
    val progress : Float = 0f,
    val recentSession : List<Session> = emptyList(),
    val completedTask : List<Task> = emptyList(),
    val upcomingTask : List<Task> = emptyList(),
    val session : Session? = null
)
