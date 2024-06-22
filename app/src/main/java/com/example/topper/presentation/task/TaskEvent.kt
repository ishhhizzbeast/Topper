package com.example.topper.presentation.task

import com.example.topper.domain.model.Subject
import com.example.topper.util.Priority

sealed class TaskEvent {
    data class onTitleChange(val title : String) : TaskEvent()

    data class onDescriptionChange(val description:String) : TaskEvent()

    data class onDueDateChange(val millis :Long?):TaskEvent()

    data class onPriorityChange(val priority:Priority) : TaskEvent()

    data class onRelatedSubjectSelect(val subject:Subject) : TaskEvent()

    data object onIsCompleteChange : TaskEvent()

    data object onSaveTask : TaskEvent()

    data object onDeleteTask : TaskEvent()
}