package com.example.topper.presentation.Subject

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topper.domain.model.Subject
import com.example.topper.domain.model.Task
import com.example.topper.domain.repositroy.SessionRepository
import com.example.topper.domain.repositroy.SubjectRepository
import com.example.topper.domain.repositroy.TaskRepository
import com.example.topper.navArgs
import com.example.topper.util.SnackbarEvent
import com.example.topper.util.toHours
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SubjectViewmodel @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val taskRepository: TaskRepository,
    private val sessionRepository: SessionRepository,
    savedStateHandle: SavedStateHandle
):ViewModel() {
    private val navArgs : SubjectScreenNavArgs = savedStateHandle.navArgs()
    private val _state = MutableStateFlow(SubjectState())
    val state = combine(
        _state,
        sessionRepository.getTotalSessionsDurationBySubject(navArgs.subjectID),
        taskRepository.getUpcomingTasksForSubject(navArgs.subjectID),
        taskRepository.getCompletedTasksForSubject(navArgs.subjectID),
        sessionRepository.getRecentTenSessionsForSubject(navArgs.subjectID)
    ){state,totalDuration,upcomingTask,completedTask,recentSession->
        state.copy(
            studiedHours = totalDuration.toHours(),
            upcomingTask = upcomingTask,
            completedTask = completedTask,
            recentSession = recentSession
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        SubjectState()
    )
    init {
        fetchSubject()
    }
    private val _snackbarEventFlow = MutableSharedFlow<SnackbarEvent>()
    val snackbarEventFlow = _snackbarEventFlow.asSharedFlow()
    fun onEvent(event:SubjectEvent){
        when(event){
            SubjectEvent.DeleteSubject -> deleteSubject()
            SubjectEvent.UpdateSubject -> updateSubject()
            is SubjectEvent.onDeleteSessionButtonClick ->{
                _state.update {
                    it.copy(
                        session = event.session
                    )
                }
            }

            SubjectEvent.deleteSession -> deleteSession()
            is SubjectEvent.onGoalHourChange -> {
                _state.update {
                    it.copy(
                        goalStudyHours = event.hour
                    )
                }
            }
            is SubjectEvent.onSubjectCardColorChange -> {
                _state.update {
                    it.copy(
                        selectedCardColors = event.color
                    )
                }
            }
            is SubjectEvent.onSubjectNameChange -> {
                _state.update {
                    it.copy(
                        subjectName = event.name
                    )
                }
            }
            is SubjectEvent.onTaskCompletedChange -> {
                updateTask(event.task)
            }
            SubjectEvent.updateProgress -> {
                val goalHours = state.value.goalStudyHours.toFloatOrNull() ?: 1f
                _state.update {
                    it.copy(
                        progress = (state.value.studiedHours / goalHours).coerceIn(0f,1f)
                    )
                }
            }
        }
    }

    private fun updateTask(task:Task) {
            viewModelScope.launch {
                try {
                    taskRepository.upsertTask(
                        task = task.copy(isComplete = !task.isComplete)
                    )
                    if (task.isComplete){
                    _snackbarEventFlow.emit(
                        SnackbarEvent.showSnackbar(message = "Saved in upcoming tasks.")
                    )}else{
                        SnackbarEvent.showSnackbar(message = "Saved in completed tasks.")

                    }
                } catch (e: Exception) {
                    _snackbarEventFlow.emit(
                        SnackbarEvent.showSnackbar(
                            "Couldn't update task. ${e.message}",
                            SnackbarDuration.Long
                        )
                    )
                }
            }
        }


    private fun fetchSubject(){
        viewModelScope.launch {
            subjectRepository.getSubjectById(
                navArgs.subjectID
            )?.let { subject ->
                _state.update {
                    it.copy(
                        subjectName = subject.name,
                        goalStudyHours = subject.goalHours.toString(),
                        currentSubjectId = subject.subjectId,
                        selectedCardColors = subject.colors.map {
                            Color(it)
                        }
                    )
                }
            }
        }
    }
    private fun updateSubject() {
        viewModelScope.launch {
            try {
                subjectRepository.upsertSubject(
                    subject = Subject(
                        name = state.value.subjectName,
                        goalHours = state.value.goalStudyHours.toFloatOrNull() ?: 1f,
                        colors = state.value.selectedCardColors.map {
                            it.toArgb()
                        }
                    )
                )
                _snackbarEventFlow.emit(
                    SnackbarEvent.showSnackbar(
                        "Subject Updated Successfully"
                    )
                )
            }catch (e:Exception){
                SnackbarEvent.showSnackbar(
                    "Some error occurred while updating subject ${e.message}",
                    SnackbarDuration.Long
                )
            }
        }
    }
    private fun deleteSubject() {
        viewModelScope.launch {
            try {
                val currentSubjectId = state.value.currentSubjectId
                if (currentSubjectId != null){
                    withContext(Dispatchers.IO) {
                        subjectRepository.deleteSubject(currentSubjectId)
                    }
                    _snackbarEventFlow.emit(
                        SnackbarEvent.showSnackbar(
                            "Subject Deleted Successfully"
                        )

                    )
                    _snackbarEventFlow.emit(
                        SnackbarEvent.NaviagateUp
                    )
                }else{
                    _snackbarEventFlow.emit(
                        SnackbarEvent.showSnackbar(
                           message = "No subject to delete"
                        )
                    )
                }

            }catch (e:Exception){
                _snackbarEventFlow.emit(
                    SnackbarEvent.showSnackbar(
                        "Some error occurred while deleting the subject",
                        duration = SnackbarDuration.Long
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
                    _snackbarEventFlow.emit(
                        SnackbarEvent.showSnackbar(
                            "Session Deleted Successfully!"
                        )

                    )
                }
            }catch (e:Exception){
                _snackbarEventFlow.emit(
                    SnackbarEvent.showSnackbar(
                        "Session Can not be Deleted!",
                        SnackbarDuration.Long
                    )
                )
            }
        }
    }
}