package com.example.topper.presentation.task

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.topper.domain.model.Task
import com.example.topper.presentation.components.ButtonSheetTask
import com.example.topper.presentation.components.DeleteDialogue
import com.example.topper.presentation.components.TaskCheckBox
import com.example.topper.presentation.components.TaskDatePicker
import com.example.topper.subject
import com.example.topper.ui.theme.Red
import com.example.topper.util.Priority
import com.example.topper.util.SnackbarEvent
import com.example.topper.util.toChangeFromMilliToDateString
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant

data class TaskscreenNavArgs(
    val taskID:Int?,
    val subjectID:Int?
)
@Destination(
    navArgsDelegate = TaskscreenNavArgs::class
)
@Composable
fun TaskScreenRoute(navigator: DestinationsNavigator) {
    val taskViewmodel : TaskViewmodel = hiltViewModel()
    val state = taskViewmodel.taskState.collectAsStateWithLifecycle().value
    TaskScreen(
        state = state,
        snackbarEvent = taskViewmodel.snackbarEventFlow,
        onEvent = taskViewmodel::onEvent,
        onBackButtonClick = {
        navigator.navigateUp()
    })
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskScreen(
    snackbarEvent: SharedFlow<SnackbarEvent>,
    state:TaskState,
    onEvent:(TaskEvent) -> Unit,
    onBackButtonClick: () -> Unit
) {


    var titleError by rememberSaveable {
        mutableStateOf<String?>(null)
    }
    var isDeleteTaskOpen by rememberSaveable {
        mutableStateOf(false)
    }
    var isDatePickerIsOpen by rememberSaveable {
        mutableStateOf(false)
    }
    var isButtonSheetIsOpen by rememberSaveable {
        mutableStateOf(false)
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Instant.now().toEpochMilli()
    )
    val buttomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    titleError = when {
        state.title.isBlank() -> "please enter the title"
        state.title.length < 2 -> "title is too short"
        state.title.length > 20 -> "title is too long"
        else -> null
    }
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    LaunchedEffect(key1 = true) {
        snackbarEvent.collectLatest {event->
            when(event){
                is SnackbarEvent.showSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = event.duration
                    )
                }

                SnackbarEvent.NaviagateUp -> {
                    onBackButtonClick()
                }
            }
        }

    }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState)},
        topBar = {
            TaskTopBar(
                isTaskExist = state.currentTaskId != null,
                isComplete = state.isTaskCompleted,
                checkBoxBorderColor = state.priority.color,
                onBackButtonClick = onBackButtonClick,
                onDeleteButtonClick = {
                    isDeleteTaskOpen = true},
                onCheckBoxClick = {
                    onEvent(TaskEvent.onIsCompleteChange)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
                .verticalScroll(state = rememberScrollState())
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.title,
                onValueChange = {
                                onEvent(TaskEvent.onTitleChange(it))
                },
                label = {
                    Text(text = "title")
                },
                singleLine = true,
                isError = titleError != null && state.title.isBlank(),
                supportingText = {
                    Text(text = titleError.orEmpty())
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.description,
                onValueChange = {
                                onEvent(TaskEvent.onDescriptionChange(it))
                },
                label = {
                    Text(text = "Description")
                },
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Due Date",
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.dueDate.toChangeFromMilliToDateString(),
                    style = MaterialTheme.typography.bodyLarge
                )
                IconButton(onClick = {
                    isDatePickerIsOpen = true
                }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select Due Date"
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Priority",
                style = MaterialTheme.typography.bodySmall
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Priority.entries.forEach { priority ->
                    PriorityButton(
                        modifier = Modifier.weight(1f),
                        label = priority.title,
                        backgroundColor = priority.color,
                        borderColor = if (priority == state.priority) {
                            Color.White
                        } else Color.Transparent,
                        labelColor = if (priority == state.priority) {
                            Color.White
                        } else Color.White.copy(alpha = 0.7f),
                        onClick = {
                            onEvent(TaskEvent.onPriorityChange(priority))
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = "Related to subject",
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val firstSubject = state.subject.firstOrNull()?.name ?: ""
                Text(
                    text = state.relatedToSubject ?: firstSubject,
                    style = MaterialTheme.typography.bodyLarge
                )
                IconButton(onClick = {
                    isButtonSheetIsOpen = true
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Subject"
                    )
                }
            }
            Button(
                enabled = titleError == null,
                onClick = {
                          onEvent(TaskEvent.onSaveTask)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {
                Text(text = "Save")
            }
        }
        DeleteDialogue(
            isOpen = isDeleteTaskOpen,
            title = "Delete Session",
            onDismissRequest = {
                isDeleteTaskOpen = false
            },
            onConfirmRequest = {
                onEvent(TaskEvent.onDeleteTask)
                isDeleteTaskOpen = false
            },
            bodyText = "Do you sure want to delete this task?"+
                    "Remider: once you deleted can not be undone."
        )
        TaskDatePicker(
            state = datePickerState,
            isOpen = isDatePickerIsOpen,
            onDissmissClicked = {
                                isDatePickerIsOpen = false
            },
            onConfirmClicked = {
                onEvent(TaskEvent.onDueDateChange(datePickerState.selectedDateMillis))
                isDatePickerIsOpen = false
            }
        )
        ButtonSheetTask(
            isOpen = isButtonSheetIsOpen,
            SheetState = buttomSheetState,
            subjects = state.subject,
            onDismissCLick = { isButtonSheetIsOpen = false},
            onSubjectClick ={
                onEvent(TaskEvent.onRelatedSubjectSelect(it))
                            scope.launch {
                                buttomSheetState.hide()
                            }.invokeOnCompletion {
                                if (!buttomSheetState.isVisible) isButtonSheetIsOpen = false
                            }
            } ,
            bottomSheetTitle = "Related To Subject"
        )
    }

}

@Composable
fun PriorityButton(
    modifier: Modifier,
    label: String,
    backgroundColor: Color,
    borderColor: Color,
    labelColor: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(5.dp)
            .border(1.dp, borderColor, RoundedCornerShape(5.dp))
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = labelColor)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskTopBar(
    isTaskExist: Boolean,
    isComplete: Boolean,
    checkBoxBorderColor: Color,
    onBackButtonClick: () -> Unit,
    onDeleteButtonClick: () -> Unit,
    onCheckBoxClick: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBackButtonClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "back to main screen"
                )
            }
        },
        title = {
            Text(
                text = "Task",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        actions = {
            if (isTaskExist) {
                TaskCheckBox(
                    borderColor = checkBoxBorderColor,
                    onCheckBoxClick = onCheckBoxClick,
                    isComplete = isComplete
                )
                IconButton(onClick = onDeleteButtonClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete the Icon"
                    )
                }
            }
        },

        )
}