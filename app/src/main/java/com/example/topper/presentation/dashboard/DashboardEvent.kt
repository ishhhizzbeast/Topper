package com.example.topper.presentation.dashboard

import androidx.compose.ui.graphics.Color
import com.example.topper.domain.model.Session
import com.example.topper.domain.model.Task

sealed class DashboardEvent {
    data object saveSubject : DashboardEvent()
    data object deleteSession:DashboardEvent()
    data class  onDeleteSessionButtonClick(val session: Session):DashboardEvent()
    data class onTaskIsCompletedChange(val task:Task):DashboardEvent()
    data class onSubjectCardColorChange(val colors :List<Color>):DashboardEvent()
    data class onSubjectNameChange(val name : String):DashboardEvent()
    data class onGoalHoursChange(val hours : String):DashboardEvent()
}