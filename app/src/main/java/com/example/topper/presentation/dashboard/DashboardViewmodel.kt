package com.example.topper.presentation.dashboard

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topper.domain.model.Session
import com.example.topper.domain.model.Subject
import com.example.topper.domain.model.Task
import com.example.topper.domain.repositroy.SessionRepository
import com.example.topper.domain.repositroy.SubjectRepository
import com.example.topper.domain.repositroy.TaskRepository
import com.example.topper.util.SnackbarEvent
import com.example.topper.util.SnackbarEvent.*
import com.example.topper.util.toHours
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewmodel @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val sessionRepository: SessionRepository,
    private val taskRepository:TaskRepository
): ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state = combine(
        _state,
        subjectRepository.getTotalSubjectCount(),
        subjectRepository.getTotalGoalHours(),
        subjectRepository.getAllSubjects(),
        sessionRepository.getTotalSessionsDuration()
    ) { state, subjectCount, goalHours, subjects, totalSessionDuration ->
        state.copy(
            totalSubjectCount = subjectCount,
            totalGoalHours = goalHours,
            subjects = subjects,
            totalStudiedHours = totalSessionDuration.toHours()
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardState()
    )

    val task : Flow<List<Task>> = taskRepository.getAllUpcomingTasks()
        .stateIn(
            scope = viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val session : Flow<List<Session>> = sessionRepository.getRecentFiveSessions()
        .stateIn(
            scope = viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.onDeleteSessionButtonClick -> {
                _state.update {
                    it.copy(
                        session = event.session
                    )
                }
            }
            DashboardEvent.deleteSession -> deleteSession()
            is DashboardEvent.onGoalHoursChange -> {
                _state.update {
                    it.copy(
                        goalStudyHours =event.hours
                    )
                }
            }
            is DashboardEvent.onSubjectCardColorChange -> {
                _state.update {
                    it.copy(
                        subjectCardColors = event.colors
                    )
                }
            }
            is DashboardEvent.onSubjectNameChange -> {
                _state.update {
                    it.copy(
                        subjectName = event.name
                    )
                }
            }
            is DashboardEvent.onTaskIsCompletedChange -> {
                updateTask(event.task)
            }
            DashboardEvent.saveSubject -> saveSubject()
        }
    }

    private val _snackBarEventFlow = MutableSharedFlow<SnackbarEvent>()
    val snackbarEventFlow = _snackBarEventFlow.asSharedFlow()

    private fun saveSubject() {
        viewModelScope.launch {
            try {
                subjectRepository.upsertSubject(
                    subject = Subject(
                        name = state.value.subjectName,
                        goalHours = state.value.goalStudyHours.toFloatOrNull() ?: 1f,
                        colors = state.value.subjectCardColors.map {
                            it.toArgb()
                        }
                    )
                )
                _state.update {
                    it.copy(
                        subjectName = "",
                        goalStudyHours = "",
                        subjectCardColors = Subject.subjectcardcolors.random()
                    )
                }
                _snackBarEventFlow.emit(
                    showSnackbar("Subject Saved Successfully")
                )
            }catch (e:Exception){
                _snackBarEventFlow.emit(
                    showSnackbar(
                        "Couldn't save subject.${e.message}",
                        SnackbarDuration.Long
                    )
                )

            }

        }
    }
    private fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.upsertTask(
                    task = task.copy(isComplete = !task.isComplete)
                )
                _snackBarEventFlow.emit(
                        showSnackbar(message = "Saved in completed tasks.")
                )
            } catch (e: Exception) {
                _snackBarEventFlow.emit(
                   showSnackbar(
                        "Couldn't update task. ${e.message}",
                        SnackbarDuration.Long
                    )
                )
            }
        }
    }
    private fun deleteSession() {
        viewModelScope.launch {
            try{
                state.value.session?.let {
                    sessionRepository.deleteSession(it)
                    _snackBarEventFlow.emit(
                        showSnackbar(
                            "Session Deleted Successfully!")

                    )
                }
            }catch (e:Exception){
                _snackBarEventFlow.emit(
                    showSnackbar(
                        "Session Can not be Deleted!",
                        SnackbarDuration.Long
                    )
                )
            }
        }
    }
}
