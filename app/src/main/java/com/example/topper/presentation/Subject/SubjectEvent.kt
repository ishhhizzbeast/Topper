package com.example.topper.presentation.Subject

import androidx.compose.ui.graphics.Color
import com.example.topper.domain.model.Session
import com.example.topper.domain.model.Task

sealed class SubjectEvent {
    data object UpdateSubject : SubjectEvent()
    data object DeleteSubject : SubjectEvent()
    data object deleteSession : SubjectEvent()
    data object updateProgress :SubjectEvent()
    data class onTaskCompletedChange(val task:Task):SubjectEvent()
    data class onSubjectCardColorChange(val color: List<Color>) : SubjectEvent()
    data class onSubjectNameChange(val name: String):SubjectEvent()
    data class onGoalHourChange(val hour:String):SubjectEvent()
    data class onDeleteSessionButtonClick(val session:Session):SubjectEvent()
}