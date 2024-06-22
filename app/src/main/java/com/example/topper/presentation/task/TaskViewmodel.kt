package com.example.topper.presentation.task

import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topper.domain.model.Task
import com.example.topper.domain.repositroy.SubjectRepository
import com.example.topper.domain.repositroy.TaskRepository
import com.example.topper.navArgs
import com.example.topper.util.Priority
import com.example.topper.util.SnackbarEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import kotlin.math.absoluteValue

@HiltViewModel
class TaskViewmodel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val subjectRepository: SubjectRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _taskState = MutableStateFlow(TaskState())

    private val navArgs : TaskscreenNavArgs = savedStateHandle.navArgs()
    val taskState = combine(
        _taskState,
        subjectRepository.getAllSubjects()
    ){taskState,subject->
        taskState.copy(
            subject = subject
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = TaskState()
    )
    init {
        fetchTask()
        fetchSubject()
    }

    fun onEvent(event:TaskEvent){
        when(event){
            TaskEvent.onDeleteTask -> deleteTask()
            is TaskEvent.onDescriptionChange -> {
                _taskState.update {
                    it.copy(
                        description = event.description
                    )
                }
            }
            is TaskEvent.onDueDateChange -> {
                _taskState.update {
                    it.copy(
                        dueDate = event.millis
                    )
                }
            }
            TaskEvent.onIsCompleteChange -> {
                _taskState.update {
                    it.copy(
                      isTaskCompleted = !_taskState.value.isTaskCompleted
                    )
                }
            }

            is TaskEvent.onPriorityChange -> {
                _taskState.update {
                    it.copy(
                        priority = event.priority
                    )
                }
            }
            is TaskEvent.onRelatedSubjectSelect -> {
                _taskState.update {
                    it.copy(
                        relatedToSubject = event.subject.name,
                        subjectId = event.subject.subjectId
                    )
                }
            }
            TaskEvent.onSaveTask -> upsertTask()
            is TaskEvent.onTitleChange -> {
                _taskState.update {
                    it.copy(
                        title = event.title
                    )
                }
            }

        }
    }

    private fun deleteTask() {
        viewModelScope.launch {
            try{
            val currentTaskId = taskState.value.currentTaskId
            if (currentTaskId != null){
                withContext(Dispatchers.IO) {
                    taskRepository.deleteTask(currentTaskId)
                }
                _snackBarEventFlow.emit(
                    SnackbarEvent.showSnackbar(
                        "Task Deleted Successfully"
                    )

                )
                _snackBarEventFlow.emit(
                    SnackbarEvent.NaviagateUp
                )
            }else{
                _snackBarEventFlow.emit(
                    SnackbarEvent.showSnackbar(
                        message = "No task to delete"
                    )
                )
            }

        }catch (e:Exception){
            _snackBarEventFlow.emit(
                SnackbarEvent.showSnackbar(
                    "Some error occured while deleting the task",
                    duration = SnackbarDuration.Long
                )
            )
        }
        }
    }

    private val _snackBarEventFlow = MutableSharedFlow<SnackbarEvent>()
    val snackbarEventFlow = _snackBarEventFlow.asSharedFlow()
    private fun upsertTask() {
        viewModelScope.launch {
                val state = taskState.value
                if(state.relatedToSubject == null || state.subjectId == null){
                    _snackBarEventFlow.emit(
                        SnackbarEvent.showSnackbar(
                            message = "Please select subject related to the task"
                        )
                    )
                    return@launch
                }
            try {
                taskRepository.upsertTask(
                    task = Task(
                        name = state.title,
                        description = state.description,
                        dueDate = state.dueDate ?: Instant.now().toEpochMilli(),
                        priority = state.priority.value,
                        isComplete = state.isTaskCompleted,
                        relatedTosubject = state.relatedToSubject,
                        taskId = state.currentTaskId,
                        taskSubjectId = state.subjectId
                    )
                )
                _snackBarEventFlow.emit(
                    SnackbarEvent.showSnackbar(
                        message = "Task Saved Successfully"
                    )

                )
                _snackBarEventFlow.emit(SnackbarEvent.NaviagateUp)
            }catch (e:Exception){
                _snackBarEventFlow.emit(
                    SnackbarEvent.showSnackbar(
                        "Couldn't save task.${e.message}",
                        SnackbarDuration.Long
                    )
                )
            }
        }
    }
    private fun fetchTask(){
        viewModelScope.launch {
            navArgs.taskID?.let { id->
                taskRepository.getTaskById(id)?.let { task ->
                    _taskState.update {
                        it.copy(
                            title = task.name,
                            description = task.description,
                            dueDate = task.dueDate,
                            isTaskCompleted = task.isComplete,
                            relatedToSubject = task.relatedTosubject,
                            priority = Priority.fromInt(task.priority),
                            subjectId = task.taskSubjectId,
                            currentTaskId = task.taskId
                        )
                    }
                }
            }
        }
    }

    private fun fetchSubject(){
        viewModelScope.launch {
            navArgs.subjectID?.let { subjectID->
                subjectRepository.getSubjectById(subjectID)?.let { subject ->
                    _taskState.update {
                        it.copy(
                            subjectId = subjectID,
                            relatedToSubject = subject.name
                        )
                    }
                }
            }
        }
    }

}